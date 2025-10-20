package com.crm.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientFileDto {
    private Long id;
    private String fileName;
    private String fileType;
    private String documentType;
    private String uploadDate;
    private Long clientId;
    private String clientFullName;
}
