package com.crm.lead.controller;

import com.crm.common.util.ApiResponse;
import com.crm.lead.entity.Lead;
import com.crm.lead.service.LeadFilterService;
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
@RequestMapping("/leads/filter")
@RequiredArgsConstructor
public class LeadFilterController {

    private final LeadFilterService filterService;

    // ✅ 1. Filter qilish
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Lead>>> filterLeads(
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Filtered leads",
                filterService.filterLeads(statusId, region, targetCountry, start, end)));
    }

    // ✅ 2. Filterlanganlarni o‘chirish
    @DeleteMapping
    @PreAuthorize("hasAuthority('DELETE_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFilteredLeads(
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        filterService.deleteFilteredLeads(statusId, region, targetCountry, start, end);
        return ResponseEntity.ok(ApiResponse.ok("Filtered leads deleted", null));
    }

    // ✅ 3. Excel export
    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('VIEW_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<InputStreamResource> exportLeadsExcel(
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) throws IOException {
        ByteArrayInputStream in = filterService.exportLeadsToExcel(statusId, region, targetCountry, start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=filtered_leads.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    // ✅ 4. CSV export
    @GetMapping("/export/csv")
    @PreAuthorize("hasAuthority('VIEW_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<InputStreamResource> exportLeadsCsv(
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) throws IOException {
        ByteArrayInputStream in = filterService.exportLeadsToCsv(statusId, region, targetCountry, start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=filtered_leads.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }
}
