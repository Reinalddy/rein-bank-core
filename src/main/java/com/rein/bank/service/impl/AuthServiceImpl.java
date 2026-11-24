package com.rein.bank.service.impl;

import com.rein.bank.dto.AuthResponse;
import com.rein.bank.dto.LoginRequest;
import com.rein.bank.dto.RegisterRequest;
import com.rein.bank.entity.User;
import com.rein.bank.repository.UserRepository;
import com.rein.bank.security.JwtUtil;
import com.rein.bank.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email " + request.email() + " sudah terdaftar");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return new AuthResponse(token, "Bearer", savedUser.getEmail(), savedUser.getFullName());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email atau password salah"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Email atau password salah");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, "Bearer", user.getEmail(), user.getFullName());
    }
}