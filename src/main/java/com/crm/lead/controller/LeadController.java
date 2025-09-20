package com.crm.lead.controller;

import com.crm.client.dto.ClientResponse;
import com.crm.common.util.ApiResponse;
import com.crm.lead.dto.LeadRequest;
import com.crm.lead.dto.LeadResponse;
import com.crm.lead.entity.Lead;
import com.crm.lead.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION')")
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@Valid @RequestBody LeadRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Lead created", leadService.createLead(request)));
    }

    // ✅ 2. Leadlarni olish
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getAllLeads() {
        return ResponseEntity.ok(ApiResponse.ok("All leads", leadService.getAllLeads()));
    }

    // ✅ 3. Bitta leadni olish
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<LeadResponse>> getLeadById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lead details", leadService.getLeadById(id)));
    }

    // ✅ 4. Lead yangilash
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(@PathVariable Long id,
                                                                @Valid @RequestBody LeadRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Lead updated", leadService.updateLead(id, request)));
    }

    // ✅ 5. Leadni o‘chirish
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.ok("Lead deleted", null));
    }

    // ✅ 6. Lead → Client qilish
    @PutMapping("/{id}/convert")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ClientResponse>> convertToClient(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lead converted to client", leadService.convertToClient(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LeadResponse>> searchLeads(@RequestParam String query) {
        return ResponseEntity.ok(leadService.searchLeads(query));
    }


        // ✅ Soft Delete
        @DeleteMapping("/{id}/soft")
        public ResponseEntity<Void> softDeleteLead(@PathVariable Long id) {
            leadService.softDeleteLead(id);
            return ResponseEntity.noContent().build();
        }

        // ✅ Archive
        @PutMapping("/{id}/archive")
        public ResponseEntity<Void> archiveLead(@PathVariable Long id) {
            leadService.archiveLead(id);
            return ResponseEntity.noContent().build();
        }

        // ✅ Restore
        @PutMapping("/{id}/restore")
        public ResponseEntity<Void> restoreLead(@PathVariable Long id) {
            leadService.restoreLead(id);
            return ResponseEntity.noContent().build();
        }

        // ❌ Permanent delete (faqat SUPER_ADMIN ruxsat)
        @DeleteMapping("/{id}/permanent")
        public ResponseEntity<Void> permanentDeleteLead(@PathVariable Long id) {
            leadService.permanentDeleteLead(id);
            return ResponseEntity.noContent().build();
        }

        // ✅ Archived list
        @GetMapping("/archived")
        public ResponseEntity<List<LeadResponse>> getArchivedLeads() {
            return ResponseEntity.ok(leadService.getArchivedLeads());
        }

        // ✅ Deleted list
        @GetMapping("/deleted")
        public ResponseEntity<List<LeadResponse>> getDeletedLeads() {
            return ResponseEntity.ok(leadService.getDeletedLeads());
        }

}
