package com.crm.lead.entity;

import com.crm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String phone;
    private String region;         // viloyat
    private String targetCountry;  // qaysi davlatga ketadi
    private LocalDate lastContactDate;
    @ManyToOne
    @JoinColumn(name = "status_id")
    private LeadStatus status; // dynamic status
    // Lead kimga assign qilingan (Sales Manager)
    @ManyToOne
    private User assignedTo;
    private boolean convertedToClient = false;
    private boolean deleted = false;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 🔹 BU YO‘Q EDI – qo‘shamiz
    @Column(nullable = false)
    private boolean archived = false;
    private LocalDateTime meetingDateTime;
    @Enumerated(EnumType.STRING)
    private MeetingStatus meetingStatus;

    @Column(nullable = false)
    private boolean assigned = false; // default: false




}
