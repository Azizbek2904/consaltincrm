package com.crm.reception.controller;

import com.crm.reception.entity.VisitSchedule;
import com.crm.reception.service.VisitScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reception/visits")
@RequiredArgsConstructor
public class VisitScheduleController {

    private final VisitScheduleService visitScheduleService;

    // ✅ Lead uchun tashrif belgilash
    @PostMapping("/lead/{leadId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION') or hasAuthority('CREATE_CLIENTS')")
    public ResponseEntity<VisitSchedule> scheduleLead(
            @PathVariable Long leadId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        return ResponseEntity.ok(visitScheduleService.scheduleLeadVisit(leadId, dateTime));
    }

    // ✅ Client uchun tashrif belgilash
    @PostMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION') or hasAuthority('CREATE_CLIENTS')")
    public ResponseEntity<VisitSchedule> scheduleClient(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        return ResponseEntity.ok(visitScheduleService.scheduleClientVisit(clientId, dateTime));
    }

    // ✅ Reception → keldi deb belgilash
    @PutMapping("/{id}/came")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION') or hasAuthority('UPDATE_CLIENTS')")
    public ResponseEntity<VisitSchedule> markCame(@PathVariable Long id) {
        return ResponseEntity.ok(visitScheduleService.markCame(id));
    }

    // ✅ Reception → kelmadi deb belgilash
    @PutMapping("/{id}/missed")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION') or hasAuthority('UPDATE_CLIENTS')")
    public ResponseEntity<VisitSchedule> markMissed(@PathVariable Long id) {
        return ResponseEntity.ok(visitScheduleService.markMissed(id));
    }

    // ✅ Reception → keladigan ro‘yxat
    @GetMapping("/planned")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION') or hasAuthority('VIEW_CLIENTS')")
    public ResponseEntity<List<VisitSchedule>> getPlanned() {
        return ResponseEntity.ok(visitScheduleService.getPlannedVisits());
    }

    // ✅ Sana oralig‘ida tashriflarni olish
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION') or hasAuthority('VIEW_CLIENTS')")
    public ResponseEntity<List<VisitSchedule>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(visitScheduleService.getVisitsByDate(start, end));
    }
}
