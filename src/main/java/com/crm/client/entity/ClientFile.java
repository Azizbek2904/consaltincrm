package com.crm.client.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "client_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private String filePath;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType; // ✅ qo‘shildi

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    @Column(name = "upload_date")
    private LocalDate uploadDate;

}
