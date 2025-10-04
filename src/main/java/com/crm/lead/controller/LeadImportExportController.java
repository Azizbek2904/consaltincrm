package com.crm.lead.controller;

import com.crm.common.util.ApiResponse;
import com.crm.lead.entity.Lead;
import com.crm.lead.service.LeadImportExportService;
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
@RequestMapping("/leads/import-export")
@RequiredArgsConstructor
public class LeadImportExportController {

    private final LeadImportExportService importExportService;

    // ✅ 1. Import qilish (Excel yuklash)
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('IMPORT_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Lead>>> importLeads(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok("Leads imported", importExportService.importLeads(file)));
    }

    // ✅ 2. Export qilish (Excel)
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('EXPORT_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<InputStreamResource> exportLeads(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) throws IOException {

        ByteArrayInputStream in = importExportService.exportLeads(region, targetCountry, start, end);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leads.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }
}
