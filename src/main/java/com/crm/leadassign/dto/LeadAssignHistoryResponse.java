package com.crm.leadassign.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LeadAssignHistoryResponse {
    private Long assignmentId;
    private String salesManager;
    private String assignedBy;
    private List<String> leads;
    private LocalDateTime assignedAt;
}
