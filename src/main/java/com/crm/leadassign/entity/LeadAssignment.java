package com.crm.leadassign.entity;

import com.crm.lead.entity.Lead;
import com.crm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lead_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User salesManager;

    @ManyToOne
    private User assignedBy;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "assignment_leads",
            joinColumns = @JoinColumn(name = "assignment_id"),
            inverseJoinColumns = @JoinColumn(name = "lead_id")
    )
    private List<Lead> leads;

    private LocalDateTime assignedAt;
    private LocalDateTime lastUpdatedAt;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<LeadActivity> activities; // yangi jadval (lead bilan ishlash tarixlari)

    private boolean deleted; // 🆕 assign o‘chirilganmi
}
