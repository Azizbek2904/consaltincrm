package com.crm.dashboard.service;

import com.crm.client.entity.Client;
import com.crm.client.entity.ClientFile;
import com.crm.client.entity.ClientPaymentHistory;
import com.crm.client.repository.ClientFileRepository;
import com.crm.client.repository.ClientPaymentHistoryRepository;
import com.crm.client.repository.ClientRepository;
import com.crm.dashboard.dto.DashboardResponse;
import com.crm.lead.entity.Lead;
import com.crm.lead.reposiroty.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LeadRepository leadRepository;
    private final ClientRepository clientRepository;
    private final ClientFileRepository fileRepository;
    private final ClientPaymentHistoryRepository paymentRepository;

    public DashboardResponse getStats(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        // 🧩 Leadlar
        List<Lead> leads = leadRepository.findAllByDeletedFalse();
        long totalLeads = leads.size();
        long convertedLeads = leads.stream().filter(Lead::isConvertedToClient).count();
        double conversionRate = totalLeads == 0 ? 0 : (convertedLeads * 100.0 / totalLeads);

        long todayLeads = leads.stream()
                .filter(l -> l.getLastContactDate() != null && l.getLastContactDate().isEqual(today))
                .count();

        // 👥 Clientlar
        List<Client> clients = clientRepository.findByArchivedFalseAndDeletedFalse();
        long totalClients = clients.size();

        long todayClients = clients.stream()
                .filter(c -> c.getInitialPaymentDate() != null && c.getInitialPaymentDate().isEqual(today))
                .count();

        // 💰 To‘lovlar (ClientPaymentHistory)
        List<ClientPaymentHistory> payments = paymentRepository.findAll();
        long todayPayments = payments.stream()
                .filter(p -> p.getPaymentDate() != null && p.getPaymentDate().toLocalDate().isEqual(today))
                .count();

        double totalPaymentAmount = payments.stream()
                .mapToDouble(p -> Optional.ofNullable(p.getAmount()).orElse(0.0))
                .sum();

        long fullPaidClients = clients.stream()
                .filter(c -> c.getPaymentStatus() != null && c.getPaymentStatus().name().equals("FULLY_PAID"))
                .count();

        long partiallyPaidClients = clients.stream()
                .filter(c -> c.getPaymentStatus() != null && c.getPaymentStatus().name().equals("PARTIALLY_PAID"))
                .count();

        long pendingPayments = clients.stream()
                .filter(c -> c.getPaymentStatus() != null && c.getPaymentStatus().name().equals("PENDING"))
                .count();

        // 📂 Hujjatlar
        long totalDocuments = fileRepository.count();

        // 📊 Region tahlili
        List<DashboardResponse.RegionStats> regionStats = leads.stream()
                .collect(Collectors.groupingBy(Lead::getRegion))
                .entrySet().stream()
                .map(e -> {
                    String region = e.getKey();
                    long leadCount = e.getValue().size();
                    long clientCount = clients.stream()
                            .filter(c -> region != null && region.equalsIgnoreCase(c.getRegion()))
                            .count();
                    double convRate = leadCount == 0 ? 0 : (clientCount * 100.0 / leadCount);
                    return DashboardResponse.RegionStats.builder()
                            .region(region)
                            .leads(leadCount)
                            .clients(clientCount)
                            .conversionRate(convRate)
                            .build();
                })
                .toList();

        // 🔥 Recent Activities (Lead, Client, Payment, Document)
        List<DashboardResponse.RecentActivity> activities = new ArrayList<>();

        leads.stream()
                .sorted(Comparator.comparing(Lead::getLastContactDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(l -> activities.add(DashboardResponse.RecentActivity.builder()
                        .type("LEAD")
                        .message("New lead: " + l.getFullName())
                        .date(l.getLastContactDate() != null ? l.getLastContactDate().toString() : "—")
                        .build()));

        clients.stream()
                .sorted(Comparator.comparing(Client::getInitialPaymentDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(c -> activities.add(DashboardResponse.RecentActivity.builder()
                        .type("CLIENT")
                        .message("Client: " + c.getFullName())
                        .date(c.getInitialPaymentDate() != null ? c.getInitialPaymentDate().toString() : "—")
                        .build()));

        payments.stream()
                .sorted(Comparator.comparing(p -> p.getPaymentDate(), Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(p -> activities.add(DashboardResponse.RecentActivity.builder()
                        .type("PAYMENT")
                        .message("Payment $" + p.getAmount() + " for " + p.getClient().getFullName())
                        .date(p.getPaymentDate() != null ? p.getPaymentDate().toString() : "—")
                        .build()));

        fileRepository.findAll().stream()
                .sorted(Comparator.comparing(ClientFile::getUploadDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(f -> activities.add(DashboardResponse.RecentActivity.builder()
                        .type("DOCUMENT")
                        .message("Uploaded: " + f.getFileName())
                        .date(f.getUploadDate() != null ? f.getUploadDate().toString() : "—")
                        .build()));

        // 🔹 So‘nggi 10 ta faollik
        List<DashboardResponse.RecentActivity> recent = activities.stream()
                .sorted(Comparator.comparing(DashboardResponse.RecentActivity::getDate, Comparator.reverseOrder()))
                .limit(10)
                .toList();

        return DashboardResponse.builder()
                .totalLeads(totalLeads)
                .convertedLeads(convertedLeads)
                .conversionRate(conversionRate)
                .totalClients(totalClients)
                .totalDocuments(totalDocuments)
                .todayLeads(todayLeads)
                .todayClients(todayClients)
                .todayPayments(todayPayments)
                .fullPaidClients(fullPaidClients)
                .partiallyPaidClients(partiallyPaidClients)
                .pendingPayments(pendingPayments)
                .totalPaymentAmount(totalPaymentAmount)
                .regionStats(regionStats)
                .recentActivities(recent)
                .build();
    }


    public Map<String, Object> getTrends(String period) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> trends = new ArrayList<>();

        int range = switch (period) {
            case "weekly" -> 4;
            case "monthly" -> 6;
            default -> 7;
        };

        for (int i = range - 1; i >= 0; i--) {
            LocalDate start, end;
            String label;

            if (period.equals("weekly")) {
                start = today.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                end = start.plusDays(6);
                label = "Week " + start.get(WeekFields.ISO.weekOfWeekBasedYear());
            } else if (period.equals("monthly")) {
                start = today.minusMonths(i).withDayOfMonth(1);
                end = start.withDayOfMonth(start.lengthOfMonth());
                label = start.getMonth().name().substring(0, 3);
            } else {
                start = today.minusDays(i);
                end = start;
                label = start.getMonth().name().substring(0, 3) + " " + start.getDayOfMonth();
            }

            long leads = leadRepository.countByCreatedAtBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
            long clients = clientRepository.countByInitialPaymentDateBetween(start, end);

            trends.add(Map.of("label", label, "leads", leads, "clients", clients));
        }

        return Map.of(
                "period", period,
                "points", trends
        );
    }

}
