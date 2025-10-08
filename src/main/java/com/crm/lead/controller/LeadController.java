package com.crm.lead.controller;

import com.crm.client.dto.ClientResponse;
import com.crm.common.util.ApiResponse;
import com.crm.lead.dto.LeadRequest;
import com.crm.lead.dto.LeadResponse;
import com.crm.lead.service.LeadService;
import com.crm.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    // ✅ 1. Lead qo‘shish
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@Valid @RequestBody LeadRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Lead created", leadService.createLead(request)));
    }
    @GetMapping("/unassigned")
    public ResponseEntity<ApiResponse<?>> getUnassignedLeads() {
        return ResponseEntity.ok(ApiResponse.ok("Unassigned leads", leadService.getUnassignedLeads()));
    }

    // 🔹 Hodimga biriktirilgan leadlar
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyLeads() {
        Long currentUserId = SecurityContextHolder.getContext().getAuthentication() != null
                ? ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()
                : null;

        if (currentUserId == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));

        return ResponseEntity.ok(ApiResponse.ok("My assigned leads", leadService.getMyLeads(currentUserId)));
    }

    // ✅ 2. Lead yangilash
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Lead updated", leadService.updateLead(id, request)));
    }

    // ✅ 3. Barcha leadlarni olish
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getAllLeads() {
        return ResponseEntity.ok(ApiResponse.ok("All leads", leadService.getAllLeads()));
    }

    // ✅ 4. Bitta leadni olish
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LeadResponse>> getLeadById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lead details", leadService.getLeadById(id)));
    }

    // ✅ 5. Soft delete (vaqtincha o‘chirish)
    @DeleteMapping("/{id}/soft")
    @PreAuthorize("hasAuthority('DELETE_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteLead(@PathVariable Long id) {
        leadService.softDeleteLead(id);
        return ResponseEntity.ok(ApiResponse.ok("Lead vaqtincha o‘chirildi", null));
    }

    // ✅ 6. Permanent delete (bazadan butunlay)
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasAuthority('DELETE_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> permanentDeleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.ok("Lead butunlay o‘chirildi", null));
    }

    // ✅ 7. Lead → Client konvertatsiya
    @PutMapping("/{id}/convert")
    @PreAuthorize("hasAuthority('CONVERT_LEAD_TO_CLIENT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> convertToClient(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lead converted to client", leadService.convertToClient(id)));
    }

    // ✅ 8. Qidiruv
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('VIEW_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> searchLeads(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.ok("Search results", leadService.searchLeads(query)));
    }

    // ✅ 9. Restore (qayta tiklash)
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('UPDATE_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> restoreLead(@PathVariable Long id) {
        leadService.restoreLead(id);
        return ResponseEntity.ok(ApiResponse.ok("Lead restored", null));
    }

    // ✅ 10. Deleted bo‘limi
    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('VIEW_LEADS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getDeletedLeads() {
        return ResponseEntity.ok(ApiResponse.ok("Deleted leads", leadService.getDeletedLeads()));
    }
}
