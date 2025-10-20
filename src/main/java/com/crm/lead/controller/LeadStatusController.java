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

    // ✅ 1. Yangi lead status yaratish
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('LEAD_STATUS_CREATE')")
    public ResponseEntity<ApiResponse<LeadStatus>> createStatus(
            @RequestParam String name,
            @RequestParam(required = false) String color
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lead status created", service.createStatus(name, color))
        );
    }

    // ✅ 2. Barcha lead statuslarni olish
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SALES_MANAGER') or hasAuthority('LEAD_STATUS_VIEW')")
    public ResponseEntity<ApiResponse<List<LeadStatus>>> getAllStatuses() {
        return ResponseEntity.ok(
                ApiResponse.ok("All lead statuses", service.getAllStatuses())
        );
    }

    // ✅ 3. Lead statusni o‘chirish
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('LEAD_STATUS_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteStatus(@PathVariable Long id) {
        service.deleteStatus(id);
        return ResponseEntity.ok(ApiResponse.ok("Lead status deleted", null));
    }
}
