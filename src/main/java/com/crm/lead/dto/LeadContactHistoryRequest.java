package com.crm.lead.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadContactHistoryRequest {
    private Long leadId;
    private Long statusId;
    private String note;
}
