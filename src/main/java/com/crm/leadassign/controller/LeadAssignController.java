package com.crm.leadassign.controller;

import com.crm.common.util.ApiResponse;
import com.crm.leadassign.dto.LeadAssignHistoryResponse;
import com.crm.leadassign.dto.LeadAssignRequest;
import com.crm.leadassign.dto.LeadAssignResponse;
import com.crm.leadassign.dto.LeadAssignUpdateRequest;
import com.crm.leadassign.service.LeadAssignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/lead-assign")
@RequiredArgsConstructor
public class LeadAssignController {

    private final LeadAssignService leadAssignService;

    // ✅ 1. Assign qilish
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<LeadAssignResponse>> assignLeads(@RequestBody LeadAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Leads assigned", leadAssignService.assignLeads(request)));
    }

    // ✅ 2. Tarixni olish
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadAssignHistoryResponse>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.ok("Assignment history", leadAssignService.getAssignmentHistory()));
    }

    // ✅ 3. Sales Manager bo‘yicha qidirish
    @GetMapping("/history/search/by-manager")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadAssignHistoryResponse>>> searchByManager(@RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.ok("Assignments by manager", leadAssignService.searchBySalesManager(name)));
    }

    // ✅ 4. Sana oralig‘i bo‘yicha qidirish
    @GetMapping("/history/search/by-date")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadAssignHistoryResponse>>> searchByDate(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.ok("Assignments by date", leadAssignService.searchByDateRange(start, end)));
    }

    // ✅ 5. Update qilish
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<LeadAssignResponse>> updateAssignment(
            @PathVariable Long id,
            @RequestBody LeadAssignUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Assignment updated", leadAssignService.updateAssignment(id, request)));
    }

    // ✅ 6. O‘chirish
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        leadAssignService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.ok("Assignment deleted", null));
    }
}
