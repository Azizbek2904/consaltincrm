package com.crm.performance.controller;

import com.crm.performance.service.SalesPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class SalesPerformanceScheduler {

    private final SalesPerformanceService service;

    // Har kuni soat 23:55 da avtomatik hisoblaydi
    @Scheduled(cron = "0 55 23 * * *")
    public void autoDaily() {
        service.updateDailyPerformance(100000.0);
    }
}
