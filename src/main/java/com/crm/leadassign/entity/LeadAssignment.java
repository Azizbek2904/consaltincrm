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

    // Kimga assign qilingan (Sales Manager)
    @ManyToOne
    @JoinColumn(name = "sales_manager_id")
    private User salesManager;

    // Kim bog‘lagan (Admin / Super Admin / Manager)
    @ManyToOne
    @JoinColumn(name = "assigned_by_id")
    private User assignedBy;

    @ManyToMany
    @JoinTable(
            name = "lead_assignment_leads",
            joinColumns = @JoinColumn(name = "assignment_id"),
            inverseJoinColumns = @JoinColumn(name = "lead_id")
    )
    private List<Lead> leads;

    private LocalDateTime assignedAt;
}
