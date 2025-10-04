package com.crm.client.dto;

import com.crm.lead.dto.LeadContactHistoryResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ClientResponse {
    private Long id;
    private String fullName;
    private String phone1;
    private String phone2;
    private String region;
    private String targetCountry;

    private Double initialPayment;
    private LocalDate initialPaymentDate;

    private Double totalPayment;
    private LocalDate totalPaymentDate;

    private PaymentStatus paymentStatus;
    private String convertedBy;

    // ✅ qo‘shildi
    private String contractNumber;


    private List<ClientFileResponse> files; // ✅ endi to‘g‘ri DTO
    private List<ClientPaymentHistoryResponse> payments;
    private List<LeadContactHistoryResponse> contactHistory;
    private List<String> comments;

    private LocalDateTime nextVisitDate;


}
