package com.vetclinic.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JwtUtil محسّن:
 * - يقبل jwt.expiration-ms كـ String ويتعامل مع صيغ متعددة (رقم، "24h", "30m", "7d").
 * - يتجاهل تعليقات بعد '#' لو أحد كتب قيمة خاطئة مثل "86400000#24hours".
 * - يدعم secret سواء كان base64 أو نص عادي؛ لو كان قصيرة نحسب SHA-256 للحصول على 32 بايت.
 * - يحول كل شيء لlong (millis) بشكل آمن.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final Key key;
    private final long expirationMs;

    /**
     * نأخذ الـ secret و expiration كـ String (أكثر مرونة).
     * application.properties:
     *   jwt.secret=<base64-or-plaintext>
     *   jwt.expiration-ms=86400000   <-- أو 24h أو 30m أو 7d
     */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") String expirationConfig) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret must be provided and non-empty");
        }

        this.expirationMs = parseExpirationToMillis(expirationConfig);
        this.key = buildKeyFromSecret(secret);
        log.info("JwtUtil initialized (expirationMs={} ms)", this.expirationMs);
    }

    /**
     * ينشئ توكن HS256 مع اسم المستخدم، id و role.
     */
    public String generateToken(String username, Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("uid", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * يتحقق من صحة التوكن (signature + expiry).
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * يستخرج Claims من التوكن (يفرمي استثناء إذا غير صالح).
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ---------------------- مساعدة داخلية ------------------------

    /**
     * يبني مفتاح HMAC-SHA من secret:
     * - يحاول يفك base64 أولاً.
     * - لو النتيجة أقل من 32 بايت، يحسب SHA-256 من النص الأصلي ليتأكد طول 32 بايت.
     * هذا يضمن Keys.hmacShaKeyFor يعمل بدون خطأ.
     */
    private Key buildKeyFromSecret(String secret) {
        byte[] raw;
        // Trim and ignore accidental comments after '#'
        secret = stripComment(secret);

        // حاول فك base64 أولًا
        try {
            byte[] decoded = Base64.getDecoder().decode(secret);
            if (decoded.length >= 32) {
                raw = decoded;
                log.debug("Using provided secret as Base64 (len={})", raw.length);
            } else {
                // لو decoded قصير استخدم SHA-256 على الـ plain secret
                raw = sha256(secret);
                log.debug("Provided Base64 decoded too short — using SHA-256(secret) as key");
            }
        } catch (IllegalArgumentException e) {
            // ليس base64 -> استخدم النص مباشرة، ولكن تأكد الطول عبر SHA-256
            if (secret.getBytes(StandardCharsets.UTF_8).length >= 32) {
                raw = secret.getBytes(StandardCharsets.UTF_8);
                log.debug("Using plain secret bytes (len={})", raw.length);
            } else {
                raw = sha256(secret);
                log.debug("Plain secret too short — using SHA-256(secret) as key");
            }
        }

        return Keys.hmacShaKeyFor(raw);
    }

    /**
     * يحول عدد/سلسلة زمنية إلى millis بطريقة مرنة.
     * يدعم أمثلة:
     *  - "86400000"  -> 86400000 millis
     *  - "24h" / "24hours" -> 24 * 3600000
     *  - "30m" -> 30 * 60000
     *  - "15s" -> 15 * 1000
     *  - "7d"  -> 7 * 24 * 3600000
     *  - "86400000#24hours" -> يتجاهل الجزء بعد '#'
     */
    private long parseExpirationToMillis(String raw) {
        if (raw == null || raw.isBlank()) {
            // قيمة افتراضية آمنة: 24 ساعة
            log.warn("jwt.expiration-ms not provided; defaulting to 24h");
            return ChronoUnit.HOURS.getDuration().toMillis() * 24;
        }

        raw = stripComment(raw).replace(",", "").trim().toLowerCase();

        // لو كله أرقام
        if (raw.matches("^\\d+$")) {
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException e) {
                log.warn("Numeric jwt.expiration-ms parse failed, falling back to 24h", e);
                return 24 * 3600_000L;
            }
        }

        // pattern: digits + optional whitespace + unit (ms/s/m/h/d)
        Pattern p = Pattern.compile("^(\\d+)\\s*(ms|s|m|h|d|minutes|hours|days)?$");
        Matcher m = p.matcher(raw);
        if (m.matches()) {
            long value = Long.parseLong(m.group(1));
            String unit = m.group(2);
            if (unit == null || unit.isBlank()) {
                // لو ما فيش وحدة افترض millis
                return value;
            }
            switch (unit) {
                case "ms":
                    return value;
                case "s":
                    return value * 1000L;
                case "m":
                case "minutes":
                    return value * 60_000L;
                case "h":
                case "hours":
                    return value * 3_600_000L;
                case "d":
                case "days":
                    return value * 86_400_000L;
                default:
                    return value;
            }
        }

        // fallback آمن: 24 ساعة
        log.warn("Unable to parse jwt.expiration-ms='{}' — defaulting to 24h", raw);
        return 24 * 3600_000L;
    }

    private String stripComment(String s) {
        int idx = s.indexOf('#');
        return idx >= 0 ? s.substring(0, idx) : s;
    }

    private byte[] sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // لا يفترض الوصول هنا لكن نضمن قيمة بديلة
            log.error("SHA-256 unavailable — this should not happen", e);
            return input.getBytes(StandardCharsets.UTF_8);
 }
}
}
