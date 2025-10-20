package com.crm.leadassign.dto;

import com.crm.lead.dto.LeadResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LeadAssignDetailedResponse {
    private Long assignmentId;
    private String salesManager;
    private String assignedBy;
    private Integer totalLeads;
    private LocalDateTime assignedAt;
    private LocalDateTime lastUpdatedAt;
    private List<LeadResponse> leads;
    private List<LeadActivityResponse> activities; // 🧩 tarixlar
}
