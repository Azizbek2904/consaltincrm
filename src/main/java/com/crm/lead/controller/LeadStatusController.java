package com.crm.lead.controller;

import com.crm.common.util.ApiResponse;
import com.crm.lead.entity.LeadStatus;
import com.crm.lead.service.LeadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lead-statuses")
@RequiredArgsConstructor
public class LeadStatusController {

    private final LeadStatusService service;

    // ✅ 1. Yangi status yaratish
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<LeadStatus>> createStatus(@RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.ok("Status created", service.createStatus(name)));
    }

    // ✅ 2. Barcha statuslarni olish
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadStatus>>> getAllStatuses() {
        return ResponseEntity.ok(ApiResponse.ok("All statuses", service.getAllStatuses()));
    }

    // ✅ 3. Statusni o‘chirish
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStatus(@PathVariable Long id) {
        service.deleteStatus(id);
        return ResponseEntity.ok(ApiResponse.ok("Status deleted", null));
    }
}
