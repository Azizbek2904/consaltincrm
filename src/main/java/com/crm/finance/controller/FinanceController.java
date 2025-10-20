package com.crm.finance.controller;

import com.crm.finance.dto.FinanceSummaryDTO;
import com.crm.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/summary")
    public ResponseEntity<FinanceSummaryDTO> getFinanceSummary() {
        return ResponseEntity.ok(financeService.getFinanceSummary());
    }
}
