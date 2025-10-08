package com.crm.auth.service;

import com.crm.auth.dto.LoginRequest;
import com.crm.auth.dto.TokenResponse;
import com.crm.auth.security.JwtProvider;
import com.crm.common.exception.CustomException;
import com.crm.user.entity.Permission;
import com.crm.user.entity.Role;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        if (!user.isActive()) {
            throw new CustomException("User is blocked", HttpStatus.FORBIDDEN);
        }

        // 🔐 JWT token yaratamiz
        String token = jwtProvider.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getPermissions()
        );

        // 🔙 Token bilan birga user ma'lumotini qaytaramiz
        return TokenResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .permissions(user.getPermissions())
                .build();
    }



    public User initSuperAdmin(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException("Super admin already exists", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .fullName("Super Admin")
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.SUPER_ADMIN)
                .permissions(Set.of(Permission.values())) // 🔑 barcha permissions
                .active(true)
                .deleted(false)
                .archived(false)
                .build();

        return userRepository.save(user);
    }

}
