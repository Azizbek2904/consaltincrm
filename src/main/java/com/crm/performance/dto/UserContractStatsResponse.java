package com.crm.performance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserContractStatsResponse {
    private Long employeeId;
    private String fullName;
    private long contracts;
    private long paid;
    private long unpaid;
    private double successRate;
}
