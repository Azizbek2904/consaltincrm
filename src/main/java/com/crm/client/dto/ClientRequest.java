package com.crm.client.dto;

import com.crm.client.dto.PaymentStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientRequest {
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
}
