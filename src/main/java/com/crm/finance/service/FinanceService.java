package com.crm.finance.service;

import com.crm.finance.dto.FinanceSummaryDTO;
import com.crm.finance.repository.FinanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FinanceRepository financeRepository;

    public FinanceSummaryDTO getFinanceSummary() {
        LocalDate now = LocalDate.now();

        Double daily = financeRepository.getTodayIncome();
        Double weekly = financeRepository.getWeeklyIncome(now.minusDays(7), now);
        Double monthly = financeRepository.getMonthlyIncome(now.getMonthValue(), now.getYear());
        Double yearly = financeRepository.getYearlyIncome(now.getYear());

        // 🧮 O‘tgan oy va yil uchun solishtirish
        int prevMonth = now.getMonthValue() == 1 ? 12 : now.getMonthValue() - 1;
        int prevYear = prevMonth == 12 ? now.getYear() - 1 : now.getYear();

        Double lastMonth = financeRepository.getPreviousMonthIncome(prevMonth, prevYear);
        Double lastYear = financeRepository.getYearlyIncome(now.getYear() - 1);

        double monthlyGrowth = (lastMonth != null && lastMonth > 0)
                ? ((monthly - lastMonth) / lastMonth) * 100
                : 0;

        double yearlyGrowth = (lastYear != null && lastYear > 0)
                ? ((yearly - lastYear) / lastYear) * 100
                : 0;

        // 🧾 Foyda va o‘rtacha tushum
        Double profit = financeRepository.getTotalProfit();
        Double avgPayment = financeRepository.getAveragePayment();

        // 🌍 Region va davlat bo‘yicha
        Map<String, Double> regionMap = new HashMap<>();
        for (Object[] obj : financeRepository.getIncomeByRegion()) {
            regionMap.put((String) obj[0], (Double) obj[1]);
        }

        Map<String, Double> countryMap = new HashMap<>();
        for (Object[] obj : financeRepository.getIncomeByCountry()) {
            countryMap.put((String) obj[0], (Double) obj[1]);
        }

        // ✅ Natija DTO orqali qaytariladi
        return FinanceSummaryDTO.builder()
                .dailyIncome(daily)
                .weeklyIncome(weekly)
                .monthlyIncome(monthly)
                .yearlyIncome(yearly)
                .monthlyGrowth(monthlyGrowth)
                .yearlyGrowth(yearlyGrowth)
                .totalProfit(profit)
                .averagePayment(avgPayment)
                .incomeByRegion(regionMap)
                .incomeByCountry(countryMap)
                .build();
    }
}
