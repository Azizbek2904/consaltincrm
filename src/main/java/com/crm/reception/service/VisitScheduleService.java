package com.crm.reception.service;

import com.crm.client.entity.Client;
import com.crm.client.repository.ClientRepository;
import com.crm.common.exception.CustomException;
import com.crm.lead.entity.Lead;
import com.crm.lead.reposiroty.LeadRepository;
import com.crm.reception.entity.VisitSchedule;
import com.crm.reception.entity.VisitStatus;
import com.crm.reception.repository.VisitScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitScheduleService {

    private final VisitScheduleRepository visitScheduleRepository;
    private final LeadRepository leadRepository;
    private final ClientRepository clientRepository;

    // ✅ Lead uchun kelish belgilash
    public VisitSchedule scheduleLeadVisit(Long leadId, LocalDateTime dateTime) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new CustomException("Lead not found", HttpStatus.NOT_FOUND));

        VisitSchedule schedule = VisitSchedule.builder()
                .lead(lead)
                .scheduledDateTime(dateTime)
                .status(VisitStatus.PLANNED)
                .build();

        return visitScheduleRepository.save(schedule);
    }

    // ✅ Client uchun kelish belgilash
    public VisitSchedule scheduleClientVisit(Long clientId, LocalDateTime dateTime) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        VisitSchedule schedule = VisitSchedule.builder()
                .client(client)
                .scheduledDateTime(dateTime)
                .status(VisitStatus.PLANNED)
                .build();

        return visitScheduleRepository.save(schedule);
    }

    // ✅ Reception → kelganini belgilash
    public VisitSchedule markCame(Long scheduleId) {
        VisitSchedule schedule = visitScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException("Schedule not found", HttpStatus.NOT_FOUND));
        schedule.setStatus(VisitStatus.CAME);
        return visitScheduleRepository.save(schedule);
    }

    // ✅ Reception → kelmaganini belgilash
    public VisitSchedule markMissed(Long scheduleId) {
        VisitSchedule schedule = visitScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException("Schedule not found", HttpStatus.NOT_FOUND));
        schedule.setStatus(VisitStatus.MISSED);
        return visitScheduleRepository.save(schedule);
    }

    // ✅ Keladigan ro‘yxat (Reception ko‘radi)
    public List<VisitSchedule> getPlannedVisits() {
        return visitScheduleRepository.findByStatus(VisitStatus.PLANNED);
    }

    // ✅ Sana bo‘yicha filter
    public List<VisitSchedule> getVisitsByDate(LocalDateTime start, LocalDateTime end) {
        return visitScheduleRepository.findByScheduledDateTimeBetween(start, end);
    }
}
