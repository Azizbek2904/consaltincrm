package com.crm.leadassign.service;

import com.crm.common.exception.CustomException;
import com.crm.lead.dto.LeadResponse;
import com.crm.lead.entity.Lead;
import com.crm.lead.reposiroty.LeadRepository;
import com.crm.leadassign.dto.*;
import com.crm.leadassign.entity.LeadActivity;
import com.crm.leadassign.entity.LeadAssignment;
import com.crm.leadassign.repository.LeadActivityRepository;
import com.crm.leadassign.repository.LeadAssignmentRepository;
import com.crm.user.entity.Role;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadAssignService {

    private final LeadAssignmentRepository assignmentRepository;
    private final LeadActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    // ✅ 1. Leadlarni Sales Manager’ga biriktirish
    @Transactional
    public LeadAssignResponse assignLeads(LeadAssignRequest request) {
        User salesManager = userRepository.findById(request.getSalesManagerId())
                .orElseThrow(() -> new CustomException("Sales manager not found", HttpStatus.NOT_FOUND));

        User assignedBy = userRepository.findById(request.getAssignedById())
                .orElseThrow(() -> new CustomException("AssignedBy user not found", HttpStatus.NOT_FOUND));

        List<Lead> leads = leadRepository.findAllById(request.getLeadIds());
        if (leads.isEmpty()) {
            throw new CustomException("No leads found to assign", HttpStatus.BAD_REQUEST);
        }

        // 1) Lead’larni belgilaymiz
        for (Lead lead : leads) {
            lead.setAssigned(true);
            lead.setAssignedTo(salesManager);
            lead.setArchived(false);
            lead.setDeleted(false);
        }
        leadRepository.saveAll(leads);

        // 2) LeadAssignment yaratamiz (tarix uchun)
        LeadAssignment assignment = LeadAssignment.builder()
                .salesManager(salesManager)
                .assignedBy(assignedBy)
                .leads(leads)
                .assignedAt(LocalDateTime.now())
                .build();
        assignment = assignmentRepository.save(assignment);

        return LeadAssignResponse.builder()
                .assignmentId(assignment.getId())
                .salesManagerName(salesManager.getFullName())
                .assignedByName(assignedBy.getFullName())
                .assignedLeadCount(leads.size())
                .leadIds(leads.stream().map(Lead::getId).toList())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }


    // ✅ 2. Barcha tarix (Assignment History)
    public List<LeadAssignHistoryResponse> getAssignmentHistory() {
        return assignmentRepository.findAll().stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    // ✅ 3. Sana oralig‘ida qidirish
    public List<LeadAssignHistoryResponse> searchByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null)
            throw new CustomException("Start and end date required", HttpStatus.BAD_REQUEST);

        return assignmentRepository.findByAssignedAtBetween(start, end).stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    // ✅ 4. Sales Managerlar ro‘yxati (dropdown uchun)
    public List<LeadAssignSalesManagerResponse> getSalesManagers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.SALES_MANAGER && u.isActive())
                .map(u -> new LeadAssignSalesManagerResponse(u.getId(), u.getFullName(), u.getEmail()))
                .toList();
    }

    // ✅ 5. Bo‘sh (unassigned) leadlar
    public List<LeadResponse> getUnassignedLeads() {
        return leadRepository.findAll().stream()
                .filter(l -> !l.isAssigned() && !l.isDeleted() && !l.isArchived())
                .map(this::mapLeadToResponse)
                .toList();
    }

    // ✅ 6. Sales Manager — o‘ziga biriktirilgan leadlar
    public List<LeadResponse> getLeadsForCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        List<Lead> leads = leadRepository.findByAssignedTo(currentUser);
        return leads.stream()
                .filter(Lead::isAssigned)
                .filter(l -> !l.isDeleted())
                .map(this::mapLeadToResponse)
                .toList();
    }

    // ✅ 7. Sales Manager o‘z leadini yangilaydi
    @Transactional
    public LeadResponse updateMyLead(Long leadId, LeadResponse updatedLead) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (currentUser.getRole() != Role.SALES_MANAGER)
            throw new CustomException("Only Sales Managers can update leads", HttpStatus.FORBIDDEN);

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));

        if (!currentUser.equals(lead.getAssignedTo()))
            throw new CustomException("Not your lead", HttpStatus.FORBIDDEN);

        if (updatedLead.getFullName() != null) lead.setFullName(updatedLead.getFullName());
        if (updatedLead.getPhone() != null) lead.setPhone(updatedLead.getPhone());
        if (updatedLead.getRegion() != null) lead.setRegion(updatedLead.getRegion());
        if (updatedLead.getTargetCountry() != null) lead.setTargetCountry(updatedLead.getTargetCountry());

        leadRepository.save(lead);

        // 🔥 Log yozamiz
        LeadActivity activity = LeadActivity.builder()
                .lead(lead)
                .salesManager(currentUser)
                .action("UPDATED")
                .note("Lead info updated by " + currentUser.getFullName())
                .createdAt(LocalDateTime.now())
                .build();
        activityRepository.save(activity);

        return mapLeadToResponse(lead);
    }

    // ✅ 8. Assignmentni o‘chirish yoki qaytarish
    @Transactional
    public void deleteAssignment(Long id, boolean restoreToGeneralList) {
        LeadAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Assignment not found", HttpStatus.NOT_FOUND));

        // Defensive copy: ConcurrentModification oldini olish uchun
        List<Lead> assignedLeads = assignment.getLeads() != null
                ? List.copyOf(assignment.getLeads())
                : List.of();

        if (!assignedLeads.isEmpty()) {
            if (restoreToGeneralList) {
                // ♻️ Umumiy ro‘yxatga qaytarish
                for (Lead lead : assignedLeads) {
                    lead.setAssigned(false);
                    lead.setAssignedTo(null);
                    lead.setArchived(false);
                    lead.setDeleted(false);
                }
            } else {
                // 🗑️ Butunlay o‘chirish (soft-delete tavsiya qilaman; agar hard kerak bo‘lsa, alohida endpoint qiling)
                for (Lead lead : assignedLeads) {
                    lead.setAssigned(false);
                    lead.setAssignedTo(null);
                    lead.setDeleted(true);
                }
            }
            leadRepository.saveAll(assignedLeads);
        }

        // 🔑 Muhim: avval join jadvalni tozalaymiz, so‘ng o‘chiramiz
        assignment.getLeads().clear();
        assignmentRepository.save(assignment); // join-table tozalanadi

        assignmentRepository.deleteById(id);
    }

    // ✅ 9. Assignment detail (leads + activities)
    public LeadAssignDetailedResponse getAssignmentDetails(Long assignmentId) {
        LeadAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException("Assignment not found", HttpStatus.NOT_FOUND));

        List<LeadActivity> activities = activityRepository.findByAssignmentId(assignmentId);

        return LeadAssignDetailedResponse.builder()
                .assignmentId(assignment.getId())
                .salesManager(assignment.getSalesManager().getFullName())
                .assignedBy(assignment.getAssignedBy().getFullName())
                .totalLeads(assignment.getLeads().size())
                .assignedAt(assignment.getAssignedAt())
                .lastUpdatedAt(assignment.getLastUpdatedAt())
                .leads(assignment.getLeads().stream().map(this::mapLeadToResponse).toList())
                .activities(activities.stream().map(this::mapActivityToResponse).toList())
                .build();
    }

    // ✅ 10. Sales Manager activity log (faoliyati)
    public List<LeadActivityResponse> getActivitiesBySalesManager(Long userId) {
        List<LeadActivity> activities = activityRepository.findBySalesManagerId(userId);
        return activities.stream().map(this::mapActivityToResponse).toList();
    }

    // ✅ 11. Qo‘lda activity qo‘shish
    public void addLeadActivity(Long leadId, String action, String note) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User salesManager = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));

        LeadAssignment assignment = assignmentRepository.findAll().stream()
                .filter(a -> a.getLeads().contains(lead))
                .findFirst()
                .orElseThrow(() -> new CustomException("Assignment not found", HttpStatus.NOT_FOUND));

        LeadActivity activity = LeadActivity.builder()
                .assignment(assignment)
                .lead(lead)
                .salesManager(salesManager)
                .action(action)
                .note(note)
                .createdAt(LocalDateTime.now())
                .build();

        activityRepository.save(activity);
    }

    // 🧩 Helper mappers
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

    private LeadActivityResponse mapActivityToResponse(LeadActivity activity) {
        return LeadActivityResponse.builder()
                .id(activity.getId())
                .salesManager(activity.getSalesManager() != null ? activity.getSalesManager().getFullName() : null)
                .leadName(activity.getLead() != null ? activity.getLead().getFullName() : null)
                .action(activity.getAction())
                .note(activity.getNote())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
