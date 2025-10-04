package com.crm.client.dto;

import jakarta.websocket.OnError;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ClientPaymentHistoryResponse {
    private Long id;
    private Double amount;
    private LocalDate paymentDate; // faqat sana qismi qaytariladi
    private String method;
    private String note;
    private String status;
    private String receivedBy;
}
