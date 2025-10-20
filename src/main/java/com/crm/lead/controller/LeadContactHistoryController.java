package com.crm.lead.controller;

import com.crm.common.util.ApiResponse;
import com.crm.lead.dto.LeadContactHistoryRequest;
import com.crm.lead.dto.LeadContactHistoryResponse;
import com.crm.lead.service.LeadContactHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leads/history")
@RequiredArgsConstructor
public class LeadContactHistoryController {

    private final LeadContactHistoryService historyService;

    // ✅ Gaplashuv qo‘shish
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LeadContactHistoryResponse>> addHistory(
            @RequestBody LeadContactHistoryRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lead contact history added", historyService.addHistory(request))
        );
    }

    // ✅ Lead bo‘yicha gaplashuvlarni olish
    @GetMapping("/{leadId}")
    @PreAuthorize("hasAuthority('VIEW_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LeadContactHistoryResponse>>> getLeadHistory(
            @PathVariable Long leadId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lead contact history list", historyService.getLeadHistory(leadId))
        );
    }

    // ✅ Gaplashuvni yangilash
    @PutMapping("/{historyId}")
    @PreAuthorize("hasAuthority('UPDATE_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LeadContactHistoryResponse>> updateHistory(
            @PathVariable Long historyId,
            @RequestBody LeadContactHistoryRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lead contact history updated", historyService.updateHistory(historyId, request))
        );
    }

    // ✅ Gaplashuvni o‘chirish
    @DeleteMapping("/{historyId}")
    @PreAuthorize("hasAuthority('DELETE_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteHistory(@PathVariable Long historyId) {
        historyService.deleteHistory(historyId);
        return ResponseEntity.ok(ApiResponse.ok("Lead contact history deleted", "Deleted successfully"));
    }
}
