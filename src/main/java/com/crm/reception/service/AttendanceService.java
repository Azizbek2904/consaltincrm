package com.crm.reception.service;

import com.crm.common.exception.CustomException;
import com.crm.reception.entity.Attendance;
import com.crm.reception.repository.AttendanceRepository;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    // ✅ Hodim ishga keldi
    public Attendance checkIn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByUserId(userId).stream()
                .filter(a -> a.getDate().equals(today))
                .findFirst()
                .orElse(Attendance.builder().user(user).date(today).build());

        if (attendance.getCheckIn() != null) {
            throw new CustomException("Already checked in today", HttpStatus.BAD_REQUEST);
        }

        attendance.setCheckIn(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    // ✅ Hodim ishni tugatdi
    public Attendance checkOut(Long userId) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByUserId(userId).stream()
                .filter(a -> a.getDate().equals(today))
                .findFirst()
                .orElseThrow(() -> new CustomException("No check-in found for today", HttpStatus.NOT_FOUND));

        if (attendance.getCheckOut() != null) {
            throw new CustomException("Already checked out today", HttpStatus.BAD_REQUEST);
        }

        attendance.setCheckOut(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    // ✅ Bugungi barcha hodimlar
    public List<Attendance> getTodayAttendance() {
        return attendanceRepository.findByDate(LocalDate.now());
    }

    // ✅ Hodim qancha vaqt ishlagan (soatlarda)
    public long getWorkedHours(Attendance attendance) {
        if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
            return java.time.Duration.between(
                    attendance.getCheckIn(),
                    attendance.getCheckOut()
            ).toHours();
        }
        return 0;
    }

    // ✅ Kunlik hisobot
    public Map<String, Long> getDailyReport(LocalDate date) {
        return attendanceRepository.findByDate(date).stream()
                .collect(Collectors.toMap(
                        a -> a.getUser().getFullName(),
                        this::getWorkedHours
                ));
    }

    // ✅ Haftalik hisobot
    public Map<String, Long> getWeeklyReport(LocalDate weekStart, LocalDate weekEnd) {
        return attendanceRepository.findAll().stream()
                .filter(a -> !a.getDate().isBefore(weekStart) && !a.getDate().isAfter(weekEnd))
                .collect(Collectors.groupingBy(
                        a -> a.getUser().getFullName(),
                        Collectors.summingLong(this::getWorkedHours)
                ));
    }

    // ✅ Oylik hisobot
    public Map<String, Long> getMonthlyReport(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return attendanceRepository.findAll().stream()
                .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                .collect(Collectors.groupingBy(
                        a -> a.getUser().getFullName(),
                        Collectors.summingLong(this::getWorkedHours)
                ));
    }
}
