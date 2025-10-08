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

    // ✅ 1. Leadlarni Sales Manager’ga biriktirish (faqat ADMIN yoki MANAGER)
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<LeadAssignResponse>> assignLeads(@RequestBody LeadAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "✅ Leads successfully assigned!",
                leadAssignService.assignLeads(request)
        ));
    }

    // ✅ 2. Tarix: barcha assign’lar (faqat ADMIN yoki MANAGER)
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadAssignHistoryResponse>>> getAssignmentHistory() {
        return ResponseEntity.ok(ApiResponse.ok(
                "📜 Assignment history loaded",
                leadAssignService.getAssignmentHistory()
        ));
    }

    // ✅ 3. Sana oralig‘i bo‘yicha qidirish
    @GetMapping("/history/by-date")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadAssignHistoryResponse>>> searchByDate(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "📅 Assignments filtered by date",
                leadAssignService.searchByDateRange(start, end)
        ));
    }
    // ✅ 4. Sales Manager’lar ro‘yxati (dropdown uchun)
    @GetMapping("/sales-managers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadAssignSalesManagerResponse>>> getSalesManagers() {
        return ResponseEntity.ok(ApiResponse.ok(
                "👥 Sales managers list loaded",
                leadAssignService.getSalesManagers()
        ));
    }

    // ✅ 5. Umumiy bo‘sh (unassigned) leadlar
    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getUnassignedLeads() {
        return ResponseEntity.ok(ApiResponse.ok(
                "🟢 Unassigned leads fetched",
                leadAssignService.getUnassignedLeads()
        ));
    }

    // ✅ 6. Joriy foydalanuvchi (Sales Manager) uchun — o‘z leadlarini olish
    @GetMapping("/my-leads")
    @PreAuthorize("hasAnyRole('SALES_MANAGER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getMyLeads() {
        return ResponseEntity.ok(ApiResponse.ok(
                "👤 Your assigned leads loaded",
                leadAssignService.getLeadsForCurrentUser()
        ));
    }

    // ✅ 7. Hodim o‘z leadini yangilaydi (faqat o‘ziga tegishli lead uchun)
    @PutMapping("/my-leads/{leadId}")
    @PreAuthorize("hasRole('SALES_MANAGER')")
    public ResponseEntity<ApiResponse<LeadResponse>> updateMyLead(
            @PathVariable Long leadId,
            @RequestBody LeadResponse updatedLead
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "✏️ Lead updated successfully",
                leadAssignService.updateMyLead(leadId, updatedLead)
        ));
    }

    // ✅ 8. Tarixdan assign’ni o‘chirish (faqat ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        leadAssignService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.ok("🗑️ Assignment deleted", null));
    }
}
