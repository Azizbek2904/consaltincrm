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

    // ✅ 1. Import qilish (Excel yuklash)
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<List<Client>>> importClients(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok("Clients imported", importExportService.importClients(file)));
    }

    // ✅ 2. Export qilish (Excel)
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE','MANAGER')")
    public ResponseEntity<InputStreamResource> exportClients(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) throws IOException {

        ByteArrayInputStream in = importExportService.exportClients(region, targetCountry, start, end);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clients.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }
}
