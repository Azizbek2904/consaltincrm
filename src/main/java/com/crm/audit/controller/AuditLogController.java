package com.crm.audit.controller;

import com.crm.audit.entity.AuditLog;
import com.crm.audit.repository.AuditLogRepository;
import com.crm.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogRepository auditLogRepository;
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAllLogs() {
        return ResponseEntity.ok(ApiResponse.ok("Audit logs", auditLogRepository.findAll()));
    }

}
