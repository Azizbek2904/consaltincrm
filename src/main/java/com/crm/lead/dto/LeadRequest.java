package com.crm.lead.dto;

import com.crm.lead.entity.MeetingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LeadRequest {
    private String fullName;
    private String phone;
    private String region;
    private String targetCountry;
    private LocalDate lastContactDate;
    private Long statusId; // lead status id
    private Long assignedToId;  // optional
    private LocalDateTime meetingDateTime;
    private MeetingStatus meetingStatus;

}
