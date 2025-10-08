package com.crm.leadassign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeadAssignSalesManagerResponse {
    private Long id;
    private String fullName;
    private String email;
}
