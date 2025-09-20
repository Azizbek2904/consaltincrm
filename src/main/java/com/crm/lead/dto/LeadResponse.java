package com.crm.lead.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LeadResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String region;
    private String targetCountry;
    private String status;
    private LocalDate lastContactDate;
    private String assignedTo;
    private boolean convertedToClient;

    private List<LeadContactHistoryResponse> contactHistory;
    private LocalDateTime nextVisitDate;

}
