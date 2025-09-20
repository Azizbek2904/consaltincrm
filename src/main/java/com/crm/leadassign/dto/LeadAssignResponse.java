package com.crm.leadassign.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LeadAssignResponse {
    private Long id;
    private String salesManager;
    private String assignedBy;
    private List<Long> leadIds;
    private LocalDateTime assignedAt;
}
