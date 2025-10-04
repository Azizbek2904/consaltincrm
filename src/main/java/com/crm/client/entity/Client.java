package com.crm.client.entity;

import com.crm.client.dto.PaymentStatus;
import com.crm.lead.entity.Lead;
import com.crm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
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
    @ManyToOne
    @JoinColumn(name = "lead_id")
    private Lead lead;

    // ✅ Fayllar
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClientFile> files = new ArrayList<>();

    // ✅ To‘lov tarixi
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClientPaymentHistory> paymentHistory = new ArrayList<>();

    // ✅ Kommentlar
    @ElementCollection
    @CollectionTable(name = "client_comments", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "comment")
    private List<String> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "converted_by_id")
    private User convertedBy;


    // ✅ Shartnoma raqami qo‘shildi
    @Column(name = "contract_number", nullable = true)
    private String contractNumber;


    // ✅ Status flags
    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private boolean deleted = false;
}
