package com.crm.leadassign.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LeadActivityResponse {
    private Long id;
    private String salesManager;
    private String leadName;
    private String action;
    private String note;
    private LocalDateTime createdAt;
}
