package com.crm.audit.entity;

import com.crm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kim o‘zgartirgan
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Qaysi modul: Lead, Client, File, Payment, User
    private String module;

    // Amal turi: CREATE, UPDATE, DELETE, CONVERT
    private String action;

    // O‘zgartirilgan obyekt ID
    private String entityId;

    // Oldingi qiymat
    @Column(columnDefinition = "TEXT")
    private String oldValue;

    // Yangi qiymat
    @Column(columnDefinition = "TEXT")
    private String newValue;

    // Qachon bo‘lgan
    private LocalDateTime timestamp;
}
