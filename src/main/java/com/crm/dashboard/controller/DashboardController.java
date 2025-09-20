package com.crm.dashboard.controller;

import com.crm.client.dto.PaymentStatus;
import com.crm.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // ✅ Oylik lead soni
    @GetMapping("/leads/monthly")
    public ResponseEntity<Map<String, Long>> getMonthlyLeads(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(dashboardService.getMonthlyLeads(year, month));
    }

    // ✅ Client status bo‘yicha soni
    @GetMapping("/clients/status")
    public ResponseEntity<Map<PaymentStatus, Long>> getClientByStatus() {
        return ResponseEntity.ok(dashboardService.getClientByStatus());
    }

    // ✅ Oylik to‘lov summalari
    @GetMapping("/payments/monthly")
    public ResponseEntity<Map<String, Double>> getPaymentsByMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(dashboardService.getPaymentsByMonth(year, month));
    }

    // ✅ Operator faoliyati (LeadContactHistory asosida)
    @GetMapping("/operators/activity")
    public ResponseEntity<Map<String, Long>> getOperatorActivity(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(dashboardService.getOperatorActivity(year, month));
    }
}
