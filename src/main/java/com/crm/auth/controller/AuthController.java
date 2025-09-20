package com.crm.auth.controller;

import com.crm.auth.dto.LoginRequest;
import com.crm.auth.dto.TokenResponse;
import com.crm.auth.service.AuthService;
import com.crm.common.util.ApiResponse;
import com.crm.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(request)));
    }

    @PostMapping("/init-super-admin")
    public ResponseEntity<ApiResponse<User>> initSuperAdmin(@RequestParam String email,
                                                            @RequestParam String password) {
        return ResponseEntity.ok(ApiResponse.ok("Super Admin created", authService.initSuperAdmin(email, password)));
    }
}
