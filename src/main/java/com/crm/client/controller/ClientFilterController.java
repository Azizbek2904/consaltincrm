package com.crm.client.controller;

import com.crm.client.dto.PaymentStatus;
import com.crm.client.entity.Client;
import com.crm.client.service.ClientFilterService;
import com.crm.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/clients/filter")
@RequiredArgsConstructor
public class ClientFilterController {

    private final ClientFilterService filterService;

    // ✅ 1. Filter qilish (statusId qo‘shildi)
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE') or hasAuthority('CLIENT_VIEW')")
    public ResponseEntity<ApiResponse<List<Client>>> filterClients(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long statusId // ✅ qo‘shiladi
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Filtered clients", filterService.filterClients(status, targetCountry, start, end, statusId))
        );
    }

    // ✅ 2. Filterlangan clientlarni o‘chirish
    @DeleteMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('CLIENT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteFilteredClients(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long statusId // ✅ yangi qo‘shildi
    ) {
        filterService.deleteFilteredClients(status, targetCountry, start, end, statusId);
        return ResponseEntity.ok(ApiResponse.ok("Filtered clients deleted", null));
    }

    // ✅ 3. Excel export (statusId qo‘shildi)
    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE') or hasAuthority('CLIENT_VIEW')")
    public ResponseEntity<InputStreamResource> exportClientsExcel(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long statusId // ✅ yangi qo‘shildi
    ) throws IOException {
        ByteArrayInputStream in = filterService.exportClientsToExcel(status, targetCountry, start, end, statusId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=filtered_clients.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    // ✅ 4. CSV export (statusId qo‘shildi)
    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE') or hasAuthority('CLIENT_VIEW')")
    public ResponseEntity<InputStreamResource> exportClientsCsv(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long statusId // ✅ yangi qo‘shildi
    ) throws IOException {
        ByteArrayInputStream in = filterService.exportClientsToCsv(status, targetCountry, start, end, statusId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=filtered_clients.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }
}
