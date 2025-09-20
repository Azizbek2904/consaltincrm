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

        String token = jwtProvider.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getPermissions()
        );

        return new TokenResponse(token, user.getRole().name(), user.getPermissions());
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
                .permissions(Set.of(Permission.MANAGE_PERMISSIONS, Permission.CREATE_USERS, Permission.ASSIGN_ROLES))
                .active(true)
                .build();

        return userRepository.save(user);
    }
}
