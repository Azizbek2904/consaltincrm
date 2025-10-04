package com.crm.lead.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // masalan: NEW, THINKING, CLIENT, REJECTED
    // 🎨 Rang saqlash (hex formatda: #FF0000, #4ade80 va h.k.)
    @Column(length = 10)
    private String color;


}
