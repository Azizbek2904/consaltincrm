package com.crm.reception.controller;

import com.crm.reception.entity.Attendance;
import com.crm.reception.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reception/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService service;

    // ✅ Hodim ishga keldi
    @PostMapping("/{userId}/check-in")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION')")
    public ResponseEntity<Attendance> checkIn(@PathVariable Long userId) {
        return ResponseEntity.ok(service.checkIn(userId));
    }

    // ✅ Hodim ishni tugatdi
    @PostMapping("/{userId}/check-out")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION')")
    public ResponseEntity<Attendance> checkOut(@PathVariable Long userId) {
        return ResponseEntity.ok(service.checkOut(userId));
    }

    // ✅ Bugungi attendance
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RECEPTION')")
    public ResponseEntity<List<Attendance>> getTodayAttendance() {
        return ResponseEntity.ok(service.getTodayAttendance());
    }

    // ✅ Kunlik hisobot
    @GetMapping("/report/daily")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Map<String, Long>> getDailyReport(@RequestParam String date) {
        return ResponseEntity.ok(service.getDailyReport(LocalDate.parse(date)));
    }

    // ✅ Haftalik hisobot
    @GetMapping("/report/weekly")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Map<String, Long>> getWeeklyReport(@RequestParam String start, @RequestParam String end) {
        return ResponseEntity.ok(service.getWeeklyReport(LocalDate.parse(start), LocalDate.parse(end)));
    }

    // ✅ Oylik hisobot
    @GetMapping("/report/monthly")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Map<String, Long>> getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(service.getMonthlyReport(year, month));
    }
}
