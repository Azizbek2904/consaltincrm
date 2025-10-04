package com.crm.leadassign.controller;

import com.crm.common.util.ApiResponse;
import com.crm.lead.dto.LeadResponse;
import com.crm.leadassign.dto.*;
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

    // ✅ 1. Lead assign qilish
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<LeadAssignResponse>> assignLeads(@RequestBody LeadAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Leads assigned successfully",
                leadAssignService.assignLeads(request)
        ));
    }

    // ✅ 2. Barcha assign tarixini olish
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadAssignHistoryResponse>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.ok(
                "Assignment history fetched",
                leadAssignService.getAssignmentHistory()
        ));
    }

    // ✅ 3. Sales Manager bo‘yicha qidirish
    @GetMapping("/history/search/by-manager")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadAssignHistoryResponse>>> searchByManager(@RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Assignments filtered by manager",
                leadAssignService.searchBySalesManager(name)
        ));
    }

    // ✅ 4. Sana oralig‘i bo‘yicha qidirish
    @GetMapping("/history/search/by-date")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadAssignHistoryResponse>>> searchByDate(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Assignments filtered by date",
                leadAssignService.searchByDateRange(start, end)
        ));
    }

    // ✅ 5. Assign update qilish
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<LeadAssignResponse>> updateAssignment(
            @PathVariable Long id,
            @RequestBody LeadAssignUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Assignment updated successfully",
                leadAssignService.updateAssignment(id, request)
        ));
    }

    // ✅ 6. Assignni o‘chirish
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        leadAssignService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.ok("Assignment deleted", null));
    }

    // ✅ 7. Bo‘sh (unassigned) leadlarni olish
    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getUnassignedLeads() {
        return ResponseEntity.ok(ApiResponse.ok(
                "Unassigned leads fetched",
                leadAssignService.getUnassignedLeads()
        ));
    }

    // ✅ 8. Sales Manager o‘zining leadlarini ko‘radi
    @GetMapping("/my-leads")
    @PreAuthorize("hasRole('SALES_MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getMyLeads() {
        return ResponseEntity.ok(ApiResponse.ok(
                "My leads fetched",
                leadAssignService.getLeadsForCurrentUser()
        ));
    }
}
