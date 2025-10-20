package com.crm.performance.controller;

import com.crm.common.util.ApiResponse;
import com.crm.performance.dto.UserContractStatsResponse;
import com.crm.performance.entity.SalesPerformance;
import com.crm.performance.service.SalesPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/performance")
@RequiredArgsConstructor
public class SalesPerformanceController {

    private final SalesPerformanceService service;
    @GetMapping("/stats/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserContractStatsResponse>>> getMonthlyContractStats() {
        return ResponseEntity.ok(ApiResponse.ok("Monthly employee stats", service.getMonthlyContractStats()));
    }

    // 🔹 Bugungi statistika
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<SalesPerformance>>> getToday() {
        return ResponseEntity.ok(ApiResponse.ok("Today performance", service.getTodayPerformances()));
    }

    // 🔹 Oylik statistika
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<SalesPerformance>>> getMonthly() {
        return ResponseEntity.ok(ApiResponse.ok("Monthly performance", service.getMonthlyPerformances()));
    }

    // 🔹 Bitta hodim bo‘yicha hisobot
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<SalesPerformance>>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("User performance", service.getUserPerformance(userId)));
    }

    // 🔹 Qo‘lda bonus kiritish (CRUD)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SalesPerformance>> save(@RequestBody SalesPerformance perf) {
        return ResponseEntity.ok(ApiResponse.ok("Saved", service.manualSave(perf)));
    }

    // 🔹 O‘chirish
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        service.deletePerformance(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    // 🔹 Kunlik hisobni yangilash
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateDaily(@RequestParam(defaultValue = "100000") Double bonus) {
        service.updateDailyPerformance(bonus);
        return ResponseEntity.ok(ApiResponse.ok("Daily performance updated", null));
    }

    // 🔹 Bonus formulasini yangilash
    @PutMapping("/bonus/formula")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateFormula(@RequestParam Double newBonus) {
        service.updateBonusFormula(newBonus);
        return ResponseEntity.ok(ApiResponse.ok("Bonus formula updated", null));
    }
}
