package com.crm.dashboard.service;

import com.crm.client.entity.Client;
import com.crm.client.repository.ClientRepository;
import com.crm.client.dto.PaymentStatus;
import com.crm.lead.entity.Lead;
import com.crm.lead.reposiroty.LeadContactHistoryRepository;
import com.crm.lead.reposiroty.LeadRepository;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LeadRepository leadRepository;
    private final ClientRepository clientRepository;
    private final LeadContactHistoryRepository leadHistoryRepository;

    // ✅ Oylik lead soni
    public Map<String, Long> getMonthlyLeads(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Lead> leads = leadRepository.findAll().stream()
                .filter(l -> l.getLastContactDate() != null &&
                        !l.getLastContactDate().isBefore(start) &&
                        !l.getLastContactDate().isAfter(end))
                .toList();

        return Map.of(ym.toString(), (long) leads.size());
    }

    // ✅ Client status bo‘yicha soni
    public Map<PaymentStatus, Long> getClientByStatus() {
        return clientRepository.findAll().stream()
                .collect(Collectors.groupingBy(Client::getPaymentStatus, Collectors.counting()));
    }

    // ✅ To‘lov summalari (oylik)
    public Map<String, Double> getPaymentsByMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        double total = clientRepository.findAll().stream()
                .filter(c -> c.getTotalPaymentDate() != null &&
                        !c.getTotalPaymentDate().isBefore(start) &&
                        !c.getTotalPaymentDate().isAfter(end))
                .mapToDouble(c -> c.getTotalPayment() != null ? c.getTotalPayment() : 0.0)
                .sum();

        return Map.of(ym.toString(), total);
    }

    // ✅ Operator faoliyati (faqat LeadContactHistory asosida)
    public Map<String, Long> getOperatorActivity(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return leadHistoryRepository.findAll().stream()
                .filter(h -> h.getContactDate() != null)
                .filter(h -> {
                    LocalDate date = h.getContactDate().toLocalDate();
                    return !date.isBefore(start) && !date.isAfter(end);
                })
                .collect(Collectors.groupingBy(
                        h -> h.getOperator().getFullName(),
                        Collectors.counting()
                ));
    }
}
