package com.vetclinic.controller;

import com.vetclinic.dto.*;
import com.vetclinic.entity.User;
import com.vetclinic.repository.UserRepository;
import com.vetclinic.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository; // to load user after login

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    // تسجيل مستخدم جديد (owner عادةً)
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequest req) {
        UserResponseDTO created = userService.register(req);
        return ResponseEntity.ok(created);
    }

    // تسجيل دخول -> يعيد JWT
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        // عند نجاح المصادقة نحمل المستخدم من DB للحصول على id/role الحقيقيين
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after auth"));

        // توليد توكن
        String token = ((com.vetclinic.service.impl.UserServiceImpl) userService).generateTokenForUser(user);

        AuthResponse resp = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(resp);
}
}
