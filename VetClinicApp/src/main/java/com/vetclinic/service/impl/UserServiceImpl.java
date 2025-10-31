package com.vetclinic.service.impl;

import com.vetclinic.dto.RegisterRequest;
import com.vetclinic.dto.UserResponseDTO;
import com.vetclinic.entity.User;
import com.vetclinic.repository.UserRepository;
import com.vetclinic.security.JwtUtil;
import com.vetclinic.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * تنفيذ خدمة المستخدمين
 *
 * قواعد تصميمية وملاحظات أمنية:
 * - لا نُخبر عن وجود/عدم وجود المستخدم بتفاصيل حساسة؛ لكن حالياً نرمي استثناءات بسيطة لتطوير الAPI.
 * - كلمات المرور تُشفَّر قبل الحفظ باستخدام PasswordEncoder من Spring Security.
 * - التأكد من تفرد username و email قبل التسجيل.
 * - كل عملية كتابة على DB داخل @Transactional لضمان الاتساق.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder");
        this.jwtUtil = Objects.requireNonNull(jwtUtil, "jwtUtil");
    }

    /**
     * تسجيل مستخدم جديد باستخدام DTO خاص (مستخدم لواجهات التسجيل).
     * - يتحقق من صلاحية الحقول الأساسية.
     * - يضمن تفرد username و email.
     * - يشفر كلمة المرور قبل الحفظ.
     * - يُرجع DTO آمن لا يحتوي على كلمة المرور.
     */
    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequest req) {
        // تحقق أساسي للمدخلات
        if (req == null) throw new IllegalArgumentException("RegisterRequest cannot be null");
        String username = (req.getUsername() == null) ? "" : req.getUsername().trim();
        String password = req.getPassword();
        if (username.isEmpty()) throw new IllegalArgumentException("username is required");
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("password is required and must be at least 8 characters");

        // تحقق من التفرد
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("username already taken");
        }
        if (req.getEmail() != null && userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("email already used");
        }

        // بناء كيان المستخدم
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // تشفير كلمة المرور
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole(req.getRole() == null ? "OWNER" : req.getRole());
        user.setActive(true);

        User saved = userRepository.save(user);
        log.info("New user registered: username={} id={}", saved.getUsername(), saved.getId());

        return toResponseDTO(saved);
    }

    /**
     * تسجيل مستخدم عبر كائن User (مفيد داخليًا).
     * - يشفر كلمة المرور إذا لم تكن مشفرة بعد.
     * - لا يقوم بتحقق كامل هنا (نفترض أن الاتصال الداخلي تعامل مدقق).
     */
    @Override
    @Transactional
    public User registerUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new IllegalArgumentException("password is required and must be at least 8 characters");
        }

        // دائمًا شيفرة كلمة المرور قبل الحفظ
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User saved = userRepository.save(user);
        log.info("User created (internal): username={} id={}", saved.getUsername(), saved.getId());
        return saved;
    }

    /**
     * جلب مستخدم بحسب اسم المستخدم.
     * - يعيد null لو غير موجود (مطابق للتوقيع السابق في الـ interface).
     */
    @Override
    public User getUserByUsername(String username) {
        if (username == null) return null;
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * جلب كل المستخدمين.
     * - لا نُزيل كلمات المرور من الكيانات هنا (يفضل تحويل للكائنات الآمنة عند إرسالها عبر API).
     */
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * (مساعد) جلب مستخدم بالـ id — يُرمى استثناء لو غير موجود.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
    }

    /**
     * حذف مستخدم بالـ id.
     * - نتحقق من الوجود قبل الحذف لتقديم رسالة خطأ واضحة.
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("Cannot delete — user not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user id={}", id);
    }

    /**
     * توليد JWT لمستخدم — لا يعتمد على كلمة المرور أو بيانات حساسة
     */
    public String generateTokenForUser(User user) {
        return jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
    }

    /* ======== Helpers ======== */

    private UserResponseDTO toResponseDTO(User u) {
        return UserResponseDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole())
                .build();
}
}
