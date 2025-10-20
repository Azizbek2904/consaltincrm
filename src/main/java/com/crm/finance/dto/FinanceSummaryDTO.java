package com.crm.finance.dto;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceSummaryDTO {

    private Double dailyIncome;
    private Double weeklyIncome;
    private Double monthlyIncome;
    private Double yearlyIncome;

    private Double monthlyGrowth;   // oylik o‘sish %
    private Double yearlyGrowth;    // yillik o‘sish %

    private Double totalProfit;     // foyda (total - initial)
    private Double averagePayment;  // o‘rtacha tushum

    private Map<String, Double> incomeByRegion;   // viloyat bo‘yicha
    private Map<String, Double> incomeByCountry;  // davlat bo‘yicha
}
