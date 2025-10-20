package com.crm.dashboard.controller;

import com.crm.common.util.ApiResponse;
import com.crm.dashboard.dto.DashboardResponse;
import com.crm.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 🔹 Asosiy dashboard statistikasi (barcha)
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        DashboardResponse data = dashboardService.getStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("📊 Dashboard stats fetched", data));
    }
    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrends(
            @RequestParam(defaultValue = "daily") String period
    ) {
        Map<String, Object> data = dashboardService.getTrends(period.toLowerCase());
        return ResponseEntity.ok(ApiResponse.ok("Trend data fetched", data));
    }

}
