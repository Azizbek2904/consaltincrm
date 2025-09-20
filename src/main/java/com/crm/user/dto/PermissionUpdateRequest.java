package com.crm.user.dto;

import com.crm.user.entity.Permission;
import lombok.Data;

import java.util.Set;

@Data
public class PermissionUpdateRequest {
    private Set<Permission> permissions;
}
