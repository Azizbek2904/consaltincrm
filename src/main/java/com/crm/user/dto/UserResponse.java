package com.crm.user.dto;

import com.crm.user.entity.Permission;
import com.crm.user.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private Set<Permission> permissions;
    private boolean active;
    private String department;
}
