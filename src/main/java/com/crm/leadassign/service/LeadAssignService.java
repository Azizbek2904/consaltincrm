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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadAssignService {

    private final LeadAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    @Transactional
    public LeadAssignResponse assignLeads(LeadAssignRequest request) {
        User salesManager = userRepository.findById(request.getSalesManagerId())
                .orElseThrow(() -> new CustomException("Sales Manager not found", HttpStatus.NOT_FOUND));

        User assignedBy = userRepository.findById(request.getAssignedById())
                .orElseThrow(() -> new CustomException("AssignedBy user not found", HttpStatus.NOT_FOUND));

        List<Lead> leads = leadRepository.findAllById(request.getLeadIds());
        if (leads.isEmpty()) {
            throw new CustomException("No leads found to assign", HttpStatus.BAD_REQUEST);
        }

        LeadAssignment assignment = LeadAssignment.builder()
                .salesManager(salesManager)
                .assignedBy(assignedBy)
                .assignedAt(LocalDateTime.now())
                .build();

        LeadAssignment saved = assignmentRepository.save(assignment);

        for (Lead lead : leads) {
            lead.setAssigned(true);                 // umumiydan yashirish uchun
            lead.setAssignedTo(salesManager);       // kimga berilganini yozish
            leadRepository.save(lead);
        }

        saved.setLeads(leads);
        assignmentRepository.save(saved);

        return LeadAssignResponse.builder()
                .assignmentId(saved.getId())
                .salesManagerName(salesManager.getFullName())
                .assignedByName(assignedBy.getFullName())
                .assignedLeadCount(leads.size())
                .leadIds(leads.stream().map(Lead::getId).toList())
                .assignedAt(saved.getAssignedAt())
                .build();
    }


    // ✅ 2. Sana oralig‘ida qidirish
    public List<LeadAssignHistoryResponse> searchByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null)
            throw new CustomException("Start and end date must be provided", HttpStatus.BAD_REQUEST);

        List<LeadAssignment> assignments = assignmentRepository.findByAssignedAtBetween(start, end);
        if (assignments.isEmpty())
            throw new CustomException("No assignments found in this date range", HttpStatus.NOT_FOUND);

        return assignments.stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }
    @Transactional
    public LeadResponse updateMyLead(Long leadId, LeadResponse updatedLead) {
        // 1️⃣ Joriy foydalanuvchini aniqlaymiz
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        // 2️⃣ Foydalanuvchi faqat SALES_MANAGER bo‘lishi kerak
        if (currentUser.getRole() != Role.SALES_MANAGER) {
            throw new CustomException("Only Sales Managers can update their leads", HttpStatus.FORBIDDEN);
        }

        // 3️⃣ Shu foydalanuvchiga biriktirilgan leadlarni topamiz
        LeadAssignment assignment = assignmentRepository.findAll().stream()
                .filter(a -> a.getSalesManager().equals(currentUser))
                .filter(a -> a.getLeads().stream().anyMatch(l -> l.getId().equals(leadId)))
                .findFirst()
                .orElseThrow(() -> new CustomException("Lead not found or not assigned to you", HttpStatus.FORBIDDEN));

        // 4️⃣ Shu leadni assignment ichidan topamiz
        Lead lead = assignment.getLeads().stream()
                .filter(l -> l.getId().equals(leadId))
                .findFirst()
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));

        // 5️⃣ Yangilanish maydonlari
        if (updatedLead.getFullName() != null && !updatedLead.getFullName().isBlank())
            lead.setFullName(updatedLead.getFullName());

        if (updatedLead.getPhone() != null && !updatedLead.getPhone().isBlank())
            lead.setPhone(updatedLead.getPhone());

        if (updatedLead.getRegion() != null && !updatedLead.getRegion().isBlank())
            lead.setRegion(updatedLead.getRegion());

        if (updatedLead.getTargetCountry() != null && !updatedLead.getTargetCountry().isBlank())
            lead.setTargetCountry(updatedLead.getTargetCountry());

        // 🔹 Qo‘shimcha maydonlar bo‘lsa (status, comment va hokazo), shu yerda qo‘shiladi
        // misol:
        // if (updatedLead.getStatus() != null) lead.setStatus(updatedLead.getStatus());

        // 6️⃣ Saqlaymiz
        assignmentRepository.save(assignment);

        // 7️⃣ Natija sifatida yangilangan leadni qaytaramiz
        return LeadResponse.builder()
                .id(lead.getId())
                .fullName(lead.getFullName())
                .phone(lead.getPhone())
                .region(lead.getRegion())
                .targetCountry(lead.getTargetCountry())
                .assignedTo(currentUser.getFullName())
                .build();
    }

    // ✅ 3. Tarixni o‘chirish (faqat adminlar uchun)
    @Transactional
    public void deleteAssignment(Long id) {
        LeadAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Assignment not found", HttpStatus.NOT_FOUND));

        // 🔹 Optional: tarixdan o‘chirilgan leadlarni qayta umumiy ro‘yxatga qo‘shish
        List<Lead> assignedLeads = assignment.getLeads();
        if (assignedLeads != null && !assignedLeads.isEmpty()) {
            for (Lead lead : assignedLeads) {
                lead.setAssignedTo(null); // unassign
                leadRepository.save(lead);
            }
        }

        assignmentRepository.delete(assignment);
    }

    // ✅ 4. Tarix
    public List<LeadAssignHistoryResponse> getAssignmentHistory() {
        return assignmentRepository.findAll().stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    // ✅ 5. Joriy foydalanuvchi uchun o‘z leadlari
    public List<LeadResponse> getLeadsForCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        List<Lead> leads = leadRepository.findByAssignedTo(currentUser);
        return leads.stream().map(this::mapLeadToResponse).toList();
    }


    // ✅ 6. Bo‘sh (unassigned) leadlar
    // ✅ faqat assignedTo = null bo'lgan leadlar (ya'ni hali hech kimga berilmaganlar)
    public List<LeadResponse> getUnassignedLeads() {
        return leadRepository.findAll().stream()
                .filter(l -> l.getAssignedTo() == null)
                .map(this::mapLeadToResponse)
                .toList();
    }


    // ✅ 7. Sales managerlar ro‘yxati
    public List<LeadAssignSalesManagerResponse> getSalesManagers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.SALES_MANAGER && u.isActive())
                .map(u -> new LeadAssignSalesManagerResponse(u.getId(), u.getFullName(), u.getEmail()))
                .toList();
    }

    // 🧩 Helper mappers
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
