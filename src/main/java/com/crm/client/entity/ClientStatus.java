package com.crm.client.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client_statuses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Masalan: "Yangi", "Jarayonda", "To‘lovda", "Yopilgan"

    private String color; // optional — UI uchun badge rangi (masalan: green, yellow)
}
