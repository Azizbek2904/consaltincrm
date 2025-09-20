package com.crm.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardResponse {

    private Map<String, Long> monthlyLeads;        // "2025-09" -> 15
    private Map<String, Long> clientByStatus;      // "FULLY_PAID" -> 7
    private Map<String, Double> paymentsByPeriod;  // "2025-09" -> 12500.0

    // 🆕 Operator bo‘yicha statistikalar
    private Map<String, Long> leadConversations;   // "Azizbek Qudratov" -> 6
    private Map<String, Long> clientConversations; // "Azizbek Qudratov" -> 3
}
