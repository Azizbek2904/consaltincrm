package com.crm.auth.dto;

import com.crm.user.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String token;
    private String role;
    private Set<Permission> permissions;
}
