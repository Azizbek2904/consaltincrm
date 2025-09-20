package com.crm.client.entity;

import com.crm.client.entity.Client;
import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}
