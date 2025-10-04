package com.crm.client.entity;

import com.crm.client.dto.PaymentStatus;
import com.crm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_payment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientPaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount; // To‘lov summasi

    private LocalDateTime paymentDate; // To‘lov sanasi

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // FULLY_PAID, PARTIALLY_PAID, PENDING

    private String method; // Naqd / karta / click / payme
    private String note;   // Qo‘shimcha izoh

    // ✅ Qaysi clientga tegishli
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // ✅ Kim qabul qilgan (Finance user)
    @ManyToOne
    @JoinColumn(name = "received_by_id")
    private User receivedBy;
}
