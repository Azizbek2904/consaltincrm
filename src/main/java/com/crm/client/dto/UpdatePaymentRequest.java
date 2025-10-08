package com.crm.client.dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePaymentRequest {
    private Double totalPayment;
    private LocalDate totalPaymentDate;
    private PaymentStatus paymentStatus;
    private String comment;
}
