package com.crm.leadassign.service;

import com.crm.common.exception.CustomException;
import com.crm.lead.dto.LeadResponse;
import com.crm.lead.entity.Lead;
import com.crm.lead.reposiroty.LeadRepository;
import com.crm.leadassign.dto.*;
import com.crm.leadassign.entity.LeadAssignment;
import com.crm.leadassign.repository.LeadAssignmentRepository;
import com.crm.user.entity.Role;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadAssignService {

    private final LeadAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    // ✅ Lead assign qilish
    public LeadAssignResponse assignLeads(LeadAssignRequest request) {
        User salesManager = userRepository.findById(request.getSalesManagerId())
                .orElseThrow(() -> new CustomException("Sales Manager not found", HttpStatus.NOT_FOUND));

        if (salesManager.getRole() != Role.SALES_MANAGER) {
            throw new CustomException("Leads can only be assigned to SALES_MANAGER role", HttpStatus.BAD_REQUEST);
        }

        User assignedBy = userRepository.findById(request.getAssignedById())
                .orElseThrow(() -> new CustomException("AssignedBy user not found", HttpStatus.NOT_FOUND));

        List<Lead> leads = leadRepository.findAllById(request.getLeadIds());
        if (leads.isEmpty()) {
            throw new CustomException("No leads found to assign", HttpStatus.BAD_REQUEST);
        }

        // ✅ Har bir leadni tekshirish
        for (Lead l : leads) {
            if (l.getAssignedTo() != null && !l.getAssignedTo().equals(salesManager)) {
                throw new CustomException("Lead " + l.getId() + " already assigned", HttpStatus.BAD_REQUEST);
            }
            l.setAssignedTo(salesManager);
        }
        leadRepository.saveAll(leads);

        LeadAssignment assignment = LeadAssignment.builder()
                .salesManager(salesManager)
                .assignedBy(assignedBy)
                .leads(leads)
                .assignedAt(LocalDateTime.now())
                .build();

        assignmentRepository.save(assignment);

        return mapToResponse(assignment);
    }

    // ✅ Tarix
    public List<LeadAssignHistoryResponse> getAssignmentHistory() {
        return assignmentRepository.findAll().stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    public List<LeadAssignHistoryResponse> searchBySalesManager(String name) {
        return assignmentRepository.findBySalesManager_FullNameContainingIgnoreCase(name).stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    public List<LeadAssignHistoryResponse> searchByDateRange(LocalDateTime start, LocalDateTime end) {
        return assignmentRepository.findByAssignedAtBetween(start, end).stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    // ✅ Update
    public LeadAssignResponse updateAssignment(Long id, LeadAssignUpdateRequest request) {
        LeadAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Assignment not found", HttpStatus.NOT_FOUND));

        User newSalesManager = userRepository.findById(request.getSalesManagerId())
                .orElseThrow(() -> new CustomException("Sales Manager not found", HttpStatus.NOT_FOUND));

        if (newSalesManager.getRole() != Role.SALES_MANAGER) {
            throw new CustomException("Only SALES_MANAGER role can receive leads", HttpStatus.BAD_REQUEST);
        }

        List<Lead> newLeads = leadRepository.findAllById(request.getLeadIds());
        if (newLeads.isEmpty()) {
            throw new CustomException("No leads found", HttpStatus.BAD_REQUEST);
        }

        newLeads.forEach(l -> l.setAssignedTo(newSalesManager));
        leadRepository.saveAll(newLeads);

        assignment.setSalesManager(newSalesManager);
        assignment.setLeads(newLeads);
        assignment.setAssignedAt(LocalDateTime.now());

        assignmentRepository.save(assignment);

        return mapToResponse(assignment);
    }

    // ✅ Delete
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new CustomException("Assignment not found", HttpStatus.NOT_FOUND);
        }
        assignmentRepository.deleteById(id);
    }

    // ✅ Bo‘sh leadlar
    public List<LeadResponse> getUnassignedLeads() {
        return leadRepository.findAll().stream()
                .filter(l -> l.getAssignedTo() == null)
                .map(this::mapLeadToResponse)
                .toList();
    }

    // ✅ Joriy user faqat o‘z leadlarini ko‘radi
    public List<LeadResponse> getLeadsForCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        return leadRepository.findAll().stream()
                .filter(l -> l.getAssignedTo() != null && l.getAssignedTo().equals(currentUser))
                .map(this::mapLeadToResponse)
                .toList();
    }

    // 🔹 Mapping helpers
    private LeadAssignResponse mapToResponse(LeadAssignment assignment) {
        return LeadAssignResponse.builder()
                .id(assignment.getId())
                .salesManager(assignment.getSalesManager().getFullName())
                .assignedBy(assignment.getAssignedBy().getFullName())
                .leadIds(assignment.getLeads().stream().map(Lead::getId).toList())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }

    private LeadAssignHistoryResponse mapToHistoryResponse(LeadAssignment assignment) {
        return LeadAssignHistoryResponse.builder()
                .assignmentId(assignment.getId())
                .salesManager(assignment.getSalesManager().getFullName())
                .assignedBy(assignment.getAssignedBy().getFullName())
                .leads(assignment.getLeads().stream().map(Lead::getFullName).toList())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }

    private LeadResponse mapLeadToResponse(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .fullName(lead.getFullName())
                .phone(lead.getPhone())
                .region(lead.getRegion())
                .targetCountry(lead.getTargetCountry())
                .assignedTo(lead.getAssignedTo() != null ? lead.getAssignedTo().getFullName() : null)
                .build();
    }
}
