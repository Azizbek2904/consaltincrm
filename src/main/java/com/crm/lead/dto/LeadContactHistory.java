package com.crm.lead.dto;

import com.crm.lead.entity.Lead;
import com.crm.lead.entity.LeadStatus;
import com.crm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_contact_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadContactHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Qaysi lead bilan gaplashilgan
    @ManyToOne
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    // Kim gaplashdi (operator)
    @ManyToOne
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;

    // Gaplashilgan vaqt
    private LocalDateTime contactDate;

    // Gaplashganda qo‘yilgan status
    @ManyToOne
    @JoinColumn(name = "status_id")
    private LeadStatus status;

    // Qo‘shimcha izoh
    @Column(columnDefinition = "TEXT")
    private String note;
}
