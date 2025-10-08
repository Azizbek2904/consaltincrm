package com.crm.client.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientFileResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private String url; // API orqali preview yoki yuklab olish uchun
    private String documentType;
    private String previewUrl;   // 🔹 inline ko‘rish uchun
    private String downloadUrl;  // 🔹 yuklab olish uchun
    private String region;
    private String targetCountry;

}
