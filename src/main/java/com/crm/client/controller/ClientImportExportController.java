package com.crm.client.controller;

import com.crm.client.entity.Client;
import com.crm.client.service.ClientImportExportService;
import com.crm.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/clients/import-export")
@RequiredArgsConstructor
public class ClientImportExportController {

    private final ClientImportExportService importExportService;

    /**
     * ✅ 1. Clientlarni Excel fayldan import qilish
     * Ruxsat: SUPER_ADMIN, ADMIN yoki CLIENT_IMPORT permissioniga ega foydalanuvchilar
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('CLIENT_IMPORT')")
    public ResponseEntity<ApiResponse<List<Client>>> importClients(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        List<Client> importedClients = importExportService.importClients(file);
        return ResponseEntity.ok(ApiResponse.ok("✅ Clients imported successfully", importedClients));
    }

    /**
     * ✅ 2. Clientlarni Excel faylga eksport qilish
     * Ruxsat: SUPER_ADMIN, ADMIN, FINANCE, MANAGER yoki CLIENT_EXPORT permissioniga ega foydalanuvchilar
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE','MANAGER') or hasAuthority('CLIENT_EXPORT')")
    public ResponseEntity<InputStreamResource> exportClients(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) throws IOException {

        ByteArrayInputStream in = importExportService.exportClients(region, targetCountry, start, end);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clients.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }
}
