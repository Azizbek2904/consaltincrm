package com.crm.leadassign.dto;

import lombok.Data;

import java.util.List;

@Data
public class LeadAssignRequest {
    private Long salesManagerId;       // kimga assign qilinadi
    private List<Long> leadIds;        // qaysi leadlar
    private Long assignedById;         // kim bog‘ladi
}
