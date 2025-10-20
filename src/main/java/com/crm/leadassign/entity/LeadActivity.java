package com.crm.leadassign.entity;

import com.crm.lead.entity.Lead;
import com.crm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_activities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private LeadAssignment assignment;
    @ManyToOne private Lead lead;
    @ManyToOne private User salesManager;

    private String action;           // ex: "Contacted", "Updated status", "Converted to client"
    private String note;             // qo‘shimcha izoh (gaplashilgan tafsilot)
    private LocalDateTime createdAt; // qachon bo‘lgan
}
