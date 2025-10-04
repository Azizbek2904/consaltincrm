package com.crm.client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePaymentStatusRequest {
    private PaymentStatus paymentStatus;
}
