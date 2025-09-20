package com.crm.reception.entity;

import com.crm.lead.entity.Lead;
import com.crm.client.entity.Client;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "visit_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime scheduledDateTime; // belgilangan vaqt

    @Enumerated(EnumType.STRING)
    private VisitStatus status;

    // Lead bilan bog‘lanishi
    @ManyToOne
    private Lead lead;

    // Client bilan bog‘lanishi
    @ManyToOne
    private Client client;
}
