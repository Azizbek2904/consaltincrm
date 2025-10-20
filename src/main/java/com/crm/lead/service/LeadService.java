package com.crm.lead.service;

import com.crm.auth.security.CustomUserDetails;
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
import com.crm.user.entity.Role;
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
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadStatusRepository statusRepository;
    private final UserRepository userRepository;
    private final ClientService clientService;

    private final VisitScheduleRepository visitScheduleRepository;
    // 🔹 Umumiy ro‘yxat (faqat unassigned)
    public List<Lead> getUnassignedLeads() {
        return leadRepository.findAllByAssignedFalseAndDeletedFalse();
    }

    // 🔹 Hodim faqat o‘ziga berilgan leadlarni ko‘radi
    public List<Lead> getMyLeads(Long userId) {
        return leadRepository.findAllByAssignedToId(userId);
    }


    // ✅ Lead yaratish
    public LeadResponse createLead(LeadRequest request) {
        LeadStatus status = null;
        if (request.getStatusId() != null && request.getStatusId() > 0) {
            status = statusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new CustomException("Status not found", HttpStatus.NOT_FOUND));
        }


        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        }

        Lead lead = Lead.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .region(request.getRegion())
                .targetCountry(request.getTargetCountry())
                .status(status)           // null bo‘lishi mumkin
                .assignedTo(assignedTo)   // null bo‘lishi mumkin
                .lastContactDate(request.getLastContactDate())
                .convertedToClient(false)
                .deleted(false)
                .archived(false)
                .meetingDateTime(request.getMeetingDateTime())   // 🆕
                .meetingStatus(request.getMeetingStatus())       // 🆕

                .build();

        leadRepository.save(lead);
        return mapToResponse(lead);
    }
    // 🔥 1. Hozirgi foydalanuvchini olish
    // 🔥 1. Hozirgi foydalanuvchini olish (to‘g‘rilangan versiya)
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        }

        throw new CustomException("Unauthorized or invalid token", HttpStatus.UNAUTHORIZED);
    }


    public List<LeadResponse> getAllLeads() {
        User currentUser = getCurrentUser();
        Role role = currentUser.getRole();

        // Super admin / admin → barcha leadlarni ko‘radi
        if (role == Role.SUPER_ADMIN || role == Role.ADMIN) {
            return leadRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        // Sales manager → faqat o‘ziga tegishli
        if (role == Role.SALES_MANAGER) {
            return leadRepository.findByAssignedTo(currentUser)
                    .stream().map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        // Qolganlar → faqat bo‘sh (unassigned)
        return leadRepository.findAllByAssignedToIsNullAndDeletedFalseAndArchivedFalse()
                .stream().map(this::mapToResponse)
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

        // 🔹 Faqat kelgan fieldlarni yangilash
        if (request.getFullName() != null) {
            lead.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            lead.setPhone(request.getPhone());
        }
        if (request.getRegion() != null) {
            lead.setRegion(request.getRegion());
        }
        if (request.getTargetCountry() != null) {
            lead.setTargetCountry(request.getTargetCountry());
        }
        if (request.getLastContactDate() != null) {
            lead.setLastContactDate(request.getLastContactDate());
        }
        if (request.getMeetingDateTime() != null) {
            lead.setMeetingDateTime(request.getMeetingDateTime());
        }
        if (request.getMeetingStatus() != null) {
            lead.setMeetingStatus(request.getMeetingStatus());
        }

        if (request.getStatusId() != null) {
            if (request.getStatusId() > 0) {
                LeadStatus status = statusRepository.findById(request.getStatusId())
                        .orElseThrow(() -> new CustomException("Status not found", HttpStatus.NOT_FOUND));
                lead.setStatus(status);
            } else {
                // 0 yoki null kelsa — statusni olib tashlaymiz
                lead.setStatus(null);
            }
        }


        // 🔹 assignedTo ixtiyoriy
        if (request.getAssignedToId() != null) {
            User user = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new CustomException("Assigned user not found", HttpStatus.NOT_FOUND));
            lead.setAssignedTo(user);
        }

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
       // LeadService.java
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
           clientRequest.setPhone2(null); // boshlang‘ich null
           clientRequest.setRegion(lead.getRegion());
           clientRequest.setTargetCountry(lead.getTargetCountry());

           // Payment ma’lumotlari keyin Reception / Finance qo‘shadi
           clientRequest.setInitialPayment(null);
           clientRequest.setInitialPaymentDate(null);
           clientRequest.setTotalPayment(null);
           clientRequest.setTotalPaymentDate(null);

           clientRequest.setPaymentStatus(PaymentStatus.PENDING);
           clientRequest.setLeadId(lead.getId());

           // Client yaratish
           ClientResponse clientResponse = clientService.createClient(clientRequest);

           // Lead flag update
           lead.setConvertedToClient(true);
           leadRepository.save(lead);

           return clientResponse;
       }

    public List<LeadResponse> searchLeads(String query) {
        List<Lead> leads = leadRepository.searchLeads(query);
        return leads.stream()
                .map(this::mapToResponse)
                .toList();
    }


    // ✅ Soft delete
    public void softDeleteLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));
        lead.setDeleted(true);
        leadRepository.save(lead);
    }


    // ✅ Restore
    public void restoreLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));
        lead.setDeleted(false);
        leadRepository.save(lead);
    }






    public List<LeadResponse> getDeletedLeads() {
        return leadRepository.findAllDeleted().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LeadResponse mapToResponse(Lead lead) {
        // 🔹 Keyingi tashrif sanasini olish (Visit jadvalidan)
        LocalDateTime nextVisit = visitScheduleRepository
                .findFirstByLeadIdAndStatusOrderByScheduledDateTimeAsc(
                        lead.getId(), VisitStatus.PLANNED
                )
                .map(VisitSchedule::getScheduledDateTime)
                .orElse(null);

        // 🔹 Kimga biriktirilganligini string sifatida formatlaymiz
        String assignedToDisplay = null;
        if (lead.getAssignedTo() != null) {
            User u = lead.getAssignedTo();
            assignedToDisplay = u.getFullName() != null
                    ? u.getFullName() + " (" + u.getRole().name() + ")"
                    : u.getEmail() + " (" + u.getRole().name() + ")";
        }

        // 🔹 LeadResponse yaratish
        return LeadResponse.builder()
                .id(lead.getId())
                .fullName(lead.getFullName())
                .phone(lead.getPhone())
                .region(lead.getRegion())
                .targetCountry(lead.getTargetCountry())
                .lastContactDate(lead.getLastContactDate())
                .statusId(lead.getStatus() != null ? lead.getStatus().getId() : null)
                .statusName(lead.getStatus() != null ? lead.getStatus().getName() : null)
                .convertedToClient(lead.isConvertedToClient())
                .nextVisitDate(nextVisit)
                .meetingDateTime(lead.getMeetingDateTime())
                .meetingStatus(lead.getMeetingStatus())
                .assignedTo(assignedToDisplay) // ✅ shu yerda qo‘shildi
                .build();
    }


}
