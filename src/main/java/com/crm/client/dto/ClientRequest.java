package com.crm.client.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientRequest {
    @NotBlank
    private String fullName;

    @Pattern(regexp = "^\\+998\\d{9}$", message = "Telefon raqam noto‘g‘ri")
    private String phone1;

    private String phone2;
    private String region;
    private String targetCountry;

    @PositiveOrZero
    private Double initialPayment;

    private LocalDate initialPaymentDate;

    @PositiveOrZero
    private Double totalPayment;

    private LocalDate totalPaymentDate;

    private Long leadId;
    private PaymentStatus paymentStatus;

    private String comment;

    // ✅ Yangi qo‘shildi
    private String contractNumber;

}
