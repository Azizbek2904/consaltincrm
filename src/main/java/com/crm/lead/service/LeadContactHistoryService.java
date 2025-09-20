package com.crm.lead.service;

import com.crm.common.exception.CustomException;
import com.crm.lead.dto.LeadContactHistoryRequest;
import com.crm.lead.dto.LeadContactHistoryResponse;
import com.crm.lead.entity.Lead;
import com.crm.lead.dto.LeadContactHistory;
import com.crm.lead.entity.LeadStatus;
import com.crm.lead.reposiroty.LeadContactHistoryRepository;
import com.crm.lead.reposiroty.LeadRepository;
import com.crm.lead.reposiroty.LeadStatusRepository;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadContactHistoryService {

    private final LeadContactHistoryRepository historyRepository;
    private final LeadRepository leadRepository;
    private final LeadStatusRepository statusRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new CustomException("Operator not found", HttpStatus.NOT_FOUND));
    }

    // ✅ Gaplashuv qo‘shish
    public LeadContactHistoryResponse addHistory(LeadContactHistoryRequest request) {
        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));

        LeadStatus status = null;
        if (request.getStatusId() != null) {
            status = statusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new CustomException("Status not found", HttpStatus.NOT_FOUND));
        }

        User operator = getCurrentUser();

        LeadContactHistory history = LeadContactHistory.builder()
                .lead(lead)
                .operator(operator)
                .status(status)
                .note(request.getNote())
                .contactDate(LocalDateTime.now())
                .build();

        historyRepository.save(history);
        return mapToResponse(history);
    }

    // ✅ Lead bo‘yicha gaplashuvlar
    public List<LeadContactHistoryResponse> getLeadHistory(Long leadId) {
        return historyRepository.findByLeadId(leadId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ Yangilash
    public LeadContactHistoryResponse updateHistory(Long historyId, LeadContactHistoryRequest request) {
        LeadContactHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new CustomException("History not found", HttpStatus.NOT_FOUND));

        if (request.getStatusId() != null) {
            LeadStatus status = statusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new CustomException("Status not found", HttpStatus.NOT_FOUND));
            history.setStatus(status);
        }
        if (request.getNote() != null) {
            history.setNote(request.getNote());
        }

        historyRepository.save(history);
        return mapToResponse(history);
    }

    // ✅ O‘chirish
    public void deleteHistory(Long historyId) {
        if (!historyRepository.existsById(historyId)) {
            throw new CustomException("History not found", HttpStatus.NOT_FOUND);
        }
        historyRepository.deleteById(historyId);
    }

    // ✅ Mapper
    private LeadContactHistoryResponse mapToResponse(LeadContactHistory history) {
        return LeadContactHistoryResponse.builder()
                .id(history.getId())
                .operatorName(history.getOperator().getFullName())
                .operatorRole(history.getOperator().getRole().name())
                .operatorDepartment(history.getOperator().getDepartment())
                .leadName(history.getLead().getFullName()) // ✅ Lead entity
                .status(history.getStatus() != null ? history.getStatus().getName() : "No status") // ✅ LeadStatus entity
                .note(history.getNote())
                .contactDate(history.getContactDate())
                .build();
    }
}
