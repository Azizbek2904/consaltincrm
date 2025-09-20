package com.crm.audit.service;

import com.crm.audit.entity.AuditLog;
import com.crm.audit.repository.AuditLogRepository;
import com.crm.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(User user, String module, String action, String entityId, String oldValue, String newValue) {
        AuditLog log = AuditLog.builder()
                .user(user) // foydalanuvchi null bo‘lishi mumkin
                .module(module)
                .action(action)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    /**
     * Agar user null bo‘lsa, "SYSTEM" foydalanuvchisini qo‘yish
     */
    public void logSystem(String module, String action, String entityId, String oldValue, String newValue) {
        AuditLog log = AuditLog.builder()
                .user(null) // DB’da null saqlanadi
                .module(module)
                .action(action + " (SYSTEM)") // actionga SYSTEM deb qo‘shib qo‘yamiz
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }
}
