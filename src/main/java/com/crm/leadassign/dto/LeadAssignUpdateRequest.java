package com.crm.leadassign.dto;

import lombok.Data;

import java.util.List;

@Data
public class LeadAssignUpdateRequest {
    private Long salesManagerId;    // yangi Sales Manager
    private List<Long> leadIds;     // yangi leadlar ro‘yxati
}
