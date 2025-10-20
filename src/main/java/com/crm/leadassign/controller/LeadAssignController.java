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

    // ✅ 1. Leadlarni Sales Manager’ga biriktirish
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER') or hasAuthority('LEAD_UPDATE')")
    public ResponseEntity<ApiResponse<LeadAssignResponse>> assignLeads(@RequestBody LeadAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "✅ Leads successfully assigned!",
                leadAssignService.assignLeads(request)
        ));
    }

    // ✅ 2. Barcha tarix (Assignment History)
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER') or hasAuthority('LEAD_VIEW')")
    public ResponseEntity<ApiResponse<List<LeadAssignHistoryResponse>>> getAssignmentHistory() {
        return ResponseEntity.ok(ApiResponse.ok(
                "📜 Assignment history loaded",
                leadAssignService.getAssignmentHistory()
        ));
    }

    // ✅ 3. Sana oralig‘ida qidirish
    @GetMapping("/history/by-date")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER') or hasAuthority('LEAD_VIEW')")
    public ResponseEntity<ApiResponse<List<LeadAssignHistoryResponse>>> searchByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "📅 Assignments filtered by date",
                leadAssignService.searchByDateRange(start, end)
        ));
    }

    // ✅ 4. Sales Managerlar ro‘yxati (dropdown uchun)
    @GetMapping("/sales-managers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER') or hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<List<LeadAssignSalesManagerResponse>>> getSalesManagers() {
        return ResponseEntity.ok(ApiResponse.ok(
                "👥 Sales managers list loaded",
                leadAssignService.getSalesManagers()
        ));
    }

    // ✅ 5. Umumiy bo‘sh (unassigned) leadlar
    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','RECEPTION') or hasAuthority('LEAD_VIEW')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getUnassignedLeads() {
        return ResponseEntity.ok(ApiResponse.ok(
                "🟢 Unassigned leads fetched",
                leadAssignService.getUnassignedLeads()
        ));
    }

    // ✅ 6. Joriy foydalanuvchi (Sales Manager) — o‘z leadlarini ko‘radi
    @GetMapping("/my-leads")
    @PreAuthorize("hasAnyRole('SALES_MANAGER','ADMIN','SUPER_ADMIN') or hasAuthority('LEAD_VIEW')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getMyLeads() {
        return ResponseEntity.ok(ApiResponse.ok(
                "👤 Your assigned leads loaded",
                leadAssignService.getLeadsForCurrentUser()
        ));
    }

    // ✅ 7. Sales Manager o‘z leadini yangilaydi
    @PutMapping("/my-leads/{leadId}")
    @PreAuthorize("hasAnyRole('SALES_MANAGER','ADMIN','SUPER_ADMIN') or hasAuthority('LEAD_UPDATE')")
    public ResponseEntity<ApiResponse<LeadResponse>> updateMyLead(
            @PathVariable Long leadId,
            @RequestBody LeadResponse updatedLead
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "✏️ Lead updated successfully",
                leadAssignService.updateMyLead(leadId, updatedLead)
        ));
    }

    // ✅ 8. Assignment detail (leads + activities)
    @GetMapping("/{assignmentId}/details")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER') or hasAuthority('LEAD_VIEW')")
    public ResponseEntity<ApiResponse<LeadAssignDetailedResponse>> getAssignmentDetails(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "📋 Assignment details loaded",
                leadAssignService.getAssignmentDetails(assignmentId)
        ));
    }

    // ✅ 9. Sales Manager activity log (faoliyati)
    @GetMapping("/sales-manager/{id}/activities")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER') or hasAuthority('LEAD_VIEW')")
    public ResponseEntity<ApiResponse<List<LeadActivityResponse>>> getActivitiesBySalesManager(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                "🧾 Sales manager activity log loaded",
                leadAssignService.getActivitiesBySalesManager(id)
        ));
    }

    // ✅ 10. Activity qo‘shish (Sales Manager yoki Admin)
    @PostMapping("/leads/{leadId}/activity")
    @PreAuthorize("hasAnyRole('SALES_MANAGER','ADMIN','SUPER_ADMIN') or hasAuthority('LEAD_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> addLeadActivity(
            @PathVariable Long leadId,
            @RequestParam String action,
            @RequestParam(required = false) String note
    ) {
        leadAssignService.addLeadActivity(leadId, action, note);
        return ResponseEntity.ok(ApiResponse.ok("📝 Activity logged", null));
    }

    // ✅ 11. Assignmentni o‘chirish yoki tiklash
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('LEAD_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean restoreToGeneralList
    ) {
        leadAssignService.deleteAssignment(id, restoreToGeneralList);
        String msg = restoreToGeneralList
                ? "♻️ Lead(lar) umumiy ro‘yxatga qaytarildi, tarix o‘chirildi"
                : "🗑️ Lead(lar) o‘chirildi, tarix o‘chirildi";
        return ResponseEntity.ok(ApiResponse.ok(msg, null));
    }

}
