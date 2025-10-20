package com.crm.auth.controller;

import com.crm.auth.dto.LoginRequest;
import com.crm.auth.dto.TokenResponse;
import com.crm.auth.security.JwtProvider;
import com.crm.auth.service.AuthService;
import com.crm.common.util.ApiResponse;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String oldToken = request.get("token");
        if (jwtTokenProvider.validateToken(oldToken)) {
            String email = jwtTokenProvider.getEmailFromToken(oldToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
            String newToken = jwtTokenProvider.generateToken(user);
            return ResponseEntity.ok(Map.of("token", newToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
    }


    @PostMapping("/init-super-admin")
    public ResponseEntity<ApiResponse<User>> initSuperAdmin(@RequestParam String email,
                                                            @RequestParam String password) {
        return ResponseEntity.ok(ApiResponse.ok("Super Admin created", authService.initSuperAdmin(email, password)));
    }
}
