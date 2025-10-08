package com.crm.auth.dto;

import com.crm.user.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponse {
    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private Set<Permission> permissions;
}
