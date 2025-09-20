package com.crm.client.entity;

import com.crm.client.dto.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    // ✅ Lead bilan bog‘lash
    private Long leadId;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientFile> files;

    private boolean deleted = false;
    private boolean archived = false;

}
