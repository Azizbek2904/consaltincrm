package com.crm.client.dto;

import com.crm.client.dto.PaymentStatus;
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

    private List<String> files;

    // ✅ Operator gaplashuvlari
    private List<LeadContactHistoryResponse> contactHistory;

    private LocalDateTime nextVisitDate;


}
