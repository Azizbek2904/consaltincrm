package com.crm.leadassign.service;

import com.crm.common.exception.CustomException;
import com.crm.lead.entity.Lead;
import com.crm.lead.reposiroty.LeadRepository;
import com.crm.leadassign.dto.LeadAssignHistoryResponse;
import com.crm.leadassign.dto.LeadAssignRequest;
import com.crm.leadassign.dto.LeadAssignResponse;
import com.crm.leadassign.dto.LeadAssignUpdateRequest;
import com.crm.leadassign.entity.LeadAssignment;
import com.crm.leadassign.repository.LeadAssignmentRepository;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        User assignedBy = userRepository.findById(request.getAssignedById())
                .orElseThrow(() -> new CustomException("AssignedBy user not found", HttpStatus.NOT_FOUND));

        List<Lead> leads = leadRepository.findAllById(request.getLeadIds());
        if (leads.isEmpty()) {
            throw new CustomException("No leads found to assign", HttpStatus.BAD_REQUEST);
        }

        // Leadlarni sales manager’ga biriktiramiz
        leads.forEach(l -> l.setAssignedTo(salesManager));
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

    // ✅ Tarixni olish
    public List<LeadAssignHistoryResponse> getAssignmentHistory() {
        return assignmentRepository.findAll().stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    // ✅ Tarixni qidirish (Sales Manager ismi bo‘yicha)
    public List<LeadAssignHistoryResponse> searchBySalesManager(String name) {
        return assignmentRepository.findBySalesManager_FullNameContainingIgnoreCase(name).stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    // ✅ Tarixni qidirish (sana oralig‘i bo‘yicha)
    public List<LeadAssignHistoryResponse> searchByDateRange(LocalDateTime start, LocalDateTime end) {
        return assignmentRepository.findByAssignedAtBetween(start, end).stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    // ✅ Update qilish
    public LeadAssignResponse updateAssignment(Long id, LeadAssignUpdateRequest request) {
        LeadAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Assignment not found", HttpStatus.NOT_FOUND));

        User newSalesManager = userRepository.findById(request.getSalesManagerId())
                .orElseThrow(() -> new CustomException("Sales Manager not found", HttpStatus.NOT_FOUND));

        List<Lead> newLeads = leadRepository.findAllById(request.getLeadIds());
        if (newLeads.isEmpty()) {
            throw new CustomException("No leads found", HttpStatus.BAD_REQUEST);
        }

        // eski leadlardan aloqani uzib, yangilarni biriktiramiz
        newLeads.forEach(l -> l.setAssignedTo(newSalesManager));
        leadRepository.saveAll(newLeads);

        assignment.setSalesManager(newSalesManager);
        assignment.setLeads(newLeads);
        assignment.setAssignedAt(LocalDateTime.now());

        assignmentRepository.save(assignment);

        return mapToResponse(assignment);
    }

    // ✅ O‘chirish
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new CustomException("Assignment not found", HttpStatus.NOT_FOUND);
        }
        assignmentRepository.deleteById(id);
    }

    private LeadAssignResponse mapToResponse(LeadAssignment assignment) {
        return LeadAssignResponse.builder()
                .id(assignment.getId())
                .salesManager(assignment.getSalesManager().getFullName())
                .assignedBy(assignment.getAssignedBy().getFullName())
                .leadIds(assignment.getLeads().stream().map(Lead::getId).collect(Collectors.toList()))
                .assignedAt(assignment.getAssignedAt())
                .build();
    }

    private LeadAssignHistoryResponse mapToHistoryResponse(LeadAssignment assignment) {
        return LeadAssignHistoryResponse.builder()
                .assignmentId(assignment.getId())
                .salesManager(assignment.getSalesManager().getFullName())
                .assignedBy(assignment.getAssignedBy().getFullName())
                .leads(assignment.getLeads().stream().map(Lead::getFullName).collect(Collectors.toList()))
                .assignedAt(assignment.getAssignedAt())
                .build();
    }
}
