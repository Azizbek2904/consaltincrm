package com.crm.lead.dto;

import com.crm.lead.entity.MeetingStatus;
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
    private Long statusId;     // ✅ qo‘shildi
    private String statusName; // ✅ nomi (oldingi status field o‘rniga)

    private LocalDate lastContactDate;
    private String assignedTo;
    private boolean convertedToClient;
    private List<LeadContactHistoryResponse> contactHistory;
    private LocalDateTime nextVisitDate;
    private LocalDateTime meetingDateTime;

    private MeetingStatus meetingStatus;


}
