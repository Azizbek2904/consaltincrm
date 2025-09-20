package com.crm.lead.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LeadRequest {
    private String fullName;
    private String phone;
    private String region;
    private String targetCountry;
    private Long statusId; // lead status id
    private LocalDate lastContactDate;
}
