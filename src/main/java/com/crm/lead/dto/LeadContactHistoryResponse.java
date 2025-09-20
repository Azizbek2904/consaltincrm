package com.crm.lead.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadContactHistoryResponse {

    private Long id;

    // 👤 Operator haqida
    private String operatorName;       // Operator FIO
    private String operatorRole;       // Operator roli
    private String operatorDepartment; // Operator bo‘limi

    // 📌 Lead haqida
    private String leadName;

    // 📊 Gaplashuv tafsilotlari
    private String status;
    private String note;
    private LocalDateTime contactDate;
}
