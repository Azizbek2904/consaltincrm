package com.crm.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private long totalLeads;
    private long convertedLeads;
    private double conversionRate;

    private long totalClients;
    private long totalDocuments;

    private long todayLeads;
    private long todayClients;
    private long todayPayments;

    private long fullPaidClients;
    private long partiallyPaidClients;
    private long pendingPayments;
    private double totalPaymentAmount;

    private List<RegionStats> regionStats;
    private List<RecentActivity> recentActivities;

    @Data
    @Builder
    public static class RegionStats {
        private String region;
        private long leads;
        private long clients;
        private double conversionRate;
    }

    @Data
    @Builder
    public static class RecentActivity {
        private String type;
        private String message;
        private String date;
        private String user;
    }
}
