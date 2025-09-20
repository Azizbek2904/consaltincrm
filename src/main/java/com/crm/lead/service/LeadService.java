package com.crm.lead.service;

import com.crm.client.dto.ClientRequest;
import com.crm.client.dto.ClientResponse;
import com.crm.client.dto.PaymentStatus;
import com.crm.client.service.ClientService;
import com.crm.common.exception.CustomException;
import com.crm.lead.dto.LeadRequest;
import com.crm.lead.dto.LeadResponse;
import com.crm.lead.entity.Lead;
import com.crm.lead.entity.LeadStatus;
import com.crm.lead.reposiroty.LeadRepository;
import com.crm.lead.reposiroty.LeadStatusRepository;
import com.crm.reception.entity.VisitSchedule;
import com.crm.reception.entity.VisitStatus;
import com.crm.reception.repository.VisitScheduleRepository;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadStatusRepository statusRepository;
    private final UserRepository userRepository;
    private final ClientService clientService;

    private final VisitScheduleRepository visitScheduleRepository;


    // ✅ Lead yaratish
    public LeadResponse createLead(LeadRequest request) {
        LeadStatus status = statusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new CustomException("Status not found", HttpStatus.NOT_FOUND));

        Lead lead = Lead.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .region(request.getRegion())
                .targetCountry(request.getTargetCountry())
                .status(status)
                .lastContactDate(request.getLastContactDate())
                .convertedToClient(false)
                .build();

        leadRepository.save(lead);
        return mapToResponse(lead);
    }

    // ✅ Barcha leadlarni olish
    public List<LeadResponse> getAllLeads() {
        return leadRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ Bitta leadni olish
    public LeadResponse getLeadById(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));
        return mapToResponse(lead);
    }

    // ✅ Lead yangilash
    public LeadResponse updateLead(Long id, LeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));

        LeadStatus status = statusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new CustomException("Status not found", HttpStatus.NOT_FOUND));

        lead.setFullName(request.getFullName());
        lead.setPhone(request.getPhone());
        lead.setRegion(request.getRegion());
        lead.setTargetCountry(request.getTargetCountry());
        lead.setStatus(status);
        lead.setLastContactDate(request.getLastContactDate());

        leadRepository.save(lead);
        return mapToResponse(lead);
    }

    // ✅ Lead o‘chirish
    public void deleteLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));
        leadRepository.delete(lead);
    }

       // ✅ Lead → Client konvertatsiya qilish
    public ClientResponse convertToClient(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));

        if (lead.isConvertedToClient()) {
            throw new CustomException("Lead already converted to client", HttpStatus.BAD_REQUEST);
        }

        // Lead ma’lumotidan ClientRequest yasaymiz
        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setFullName(lead.getFullName());
        clientRequest.setPhone1(lead.getPhone());
        clientRequest.setPhone2(null);
        clientRequest.setRegion(lead.getRegion());
        clientRequest.setTargetCountry(lead.getTargetCountry());
        clientRequest.setPaymentStatus(PaymentStatus.PENDING);

        // ClientService orqali Client yaratamiz
        ClientResponse clientResponse = clientService.createClient(clientRequest);

        // Lead flag update
        lead.setConvertedToClient(true);
        leadRepository.save(lead);

        return clientResponse;
    }

    public List<LeadResponse> searchLeads(String query) {
        List<Lead> leads = leadRepository.searchLeads(query);
        return leads.stream().map(this::mapToResponse).toList();
    }
    public void softDeleteLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));
        lead.setDeleted(true);
        leadRepository.save(lead);
    }

    public void archiveLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));
        lead.setArchived(true);
        leadRepository.save(lead);
    }

    public void restoreLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));
        lead.setDeleted(false);
        lead.setArchived(false);
        leadRepository.save(lead);
    }

    public void permanentDeleteLead(Long id) {
        if (!leadRepository.existsById(id)) {
            throw new CustomException("Lead not found", HttpStatus.NOT_FOUND);
        }
        leadRepository.deleteById(id);
    }

    public List<LeadResponse> getArchivedLeads() {
        return leadRepository.findAllArchived().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<LeadResponse> getDeletedLeads() {
        return leadRepository.findAllDeleted().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LeadResponse mapToResponse(Lead lead) {
        LocalDateTime nextVisit = visitScheduleRepository.findFirstByLeadIdAndStatusOrderByScheduledDateTimeAsc(
                lead.getId(), VisitStatus.PLANNED
        ).map(VisitSchedule::getScheduledDateTime).orElse(null);

        return LeadResponse.builder()
                .id(lead.getId())
                .fullName(lead.getFullName())
                .phone(lead.getPhone())
                .region(lead.getRegion())
                .targetCountry(lead.getTargetCountry())
                .lastContactDate(lead.getLastContactDate())
                .status(lead.getStatus() != null ? lead.getStatus().getName() : null)
                .convertedToClient(lead.isConvertedToClient())
                .nextVisitDate(nextVisit) // 🔔 qo‘shildi
                .build();
    }
}
