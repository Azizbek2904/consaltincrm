package com.crm.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientRequest {
    @NotBlank
    private String fullName;

    private String phone1;

    private String phone2;
    private String region;
    private String targetCountry;

    @PositiveOrZero
    private Double initialPayment;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate initialPaymentDate;

    @PositiveOrZero
    private Double totalPayment;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate totalPaymentDate;

    private Long leadId;
    private PaymentStatus paymentStatus;

    private String comment;

    // ✅ Yangi qo‘shildi
    private String contractNumber;
    private Long statusId;

}
