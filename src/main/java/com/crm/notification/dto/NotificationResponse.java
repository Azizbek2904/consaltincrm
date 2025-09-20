package com.crm.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private String username;
}
