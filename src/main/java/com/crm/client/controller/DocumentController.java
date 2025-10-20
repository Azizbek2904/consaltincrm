package com.crm.client.controller;

import com.crm.client.dto.ClientFileDto;
import com.crm.client.entity.ClientFile;
import com.crm.client.repository.ClientFileRepository;
import com.crm.common.util.ApiResponse;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@PermitAll
@CrossOrigin(origins = "*")
public class DocumentController {

    private final ClientFileRepository fileRepository;

    // 🔹 Hamma hujjatlarni olish (client bilan birga)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClientFileDto>>> getAllDocuments() {
        List<ClientFile> files = fileRepository.findAllWithClient();

        List<ClientFileDto> dtos = files.stream()
                .map(f -> new ClientFileDto(
                        f.getId(),
                        f.getFileName(),
                        f.getFileType(),
                        f.getDocumentType() != null ? f.getDocumentType().name() : "OTHER",
                        f.getUploadDate() != null ? f.getUploadDate().toString() : null,
                        f.getClient() != null ? f.getClient().getId() : null,
                        f.getClient() != null ? f.getClient().getFullName() : "—"
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok("Documents fetched successfully", dtos));
    }
}
