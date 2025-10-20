package com.crm.performance.service;

import com.crm.client.repository.ClientRepository;
import com.crm.common.exception.CustomException;
import com.crm.performance.dto.UserContractStatsResponse;
import com.crm.performance.entity.SalesPerformance;
import com.crm.performance.repository.SalesPerformanceRepository;
import com.crm.user.entity.Role;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesPerformanceService {

    private final SalesPerformanceRepository performanceRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    /**
     * 🔹 Kunlik statistikani hisoblaydi va saqlaydi
     */
    public void updateDailyPerformance(Double bonusPerPayment) {
        LocalDate today = LocalDate.now();
        List<User> managers = userRepository.findAllByRole(Role.SALES_MANAGER);

        for (User user : managers) {
            int convertedCount = clientRepository.countByConvertedByAndContractNumberIsNotNull(user);
            int paidCount      = clientRepository.countByConvertedByAndMainPaymentTrue(user);

            boolean qualified = convertedCount >= 3;

            SalesPerformance perf = performanceRepository
                    .findByEmployeeAndDate(user, today)
                    .orElse(SalesPerformance.builder()
                            .employee(user)
                            .date(today)
                            .build());

            perf.setConvertedCount(convertedCount);
            perf.setPaidClientsCount(paidCount);
            perf.setBonusPerPayment(bonusPerPayment);
            perf.setQualifiedForBonus(qualified);

            // Har bir paid client uchun bonus
            double baseBonus = paidCount * bonusPerPayment;

            // Agar 3 tadan ko‘p bo‘lsa qo‘shimcha bonus 500 000
            double extraBonus = qualified ? 500_000 : 0;

            perf.setTotalBonus(baseBonus + extraBonus);
            perf.setUpdatedAt(LocalDateTime.now());

            performanceRepository.save(perf);
        }
    }



    public List<UserContractStatsResponse> getMonthlyContractStats() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        List<User> employees = userRepository.findAll();
        List<UserContractStatsResponse> stats = new ArrayList<>();

        for (User user : employees) {
            long contracts = clientRepository.countByConvertedByAndContractNumberIsNotNullAndInitialPaymentDateBetween(user, start, end);
            long paid = clientRepository.countByConvertedByAndContractNumberIsNotNullAndMainPaymentTrueAndTotalPaymentDateBetween(user, start, end);
            long unpaid = contracts - paid;
            double successRate = contracts > 0 ? ((double) paid / contracts) * 100 : 0;

            stats.add(UserContractStatsResponse.builder()
                    .employeeId(user.getId())
                    .fullName(user.getFullName())
                    .contracts(contracts)
                    .paid(paid)
                    .unpaid(unpaid)
                    .successRate(Math.round(successRate * 100.0) / 100.0)
                    .build());
        }

        return stats;
    }

    /**
     * 🔹 Bugungi statistika
     */
    public List<SalesPerformance> getTodayPerformances() {
        return performanceRepository.findAllByDate(LocalDate.now());
    }

    /**
     * 🔹 Oylik statistika
     */
    public List<SalesPerformance> getMonthlyPerformances() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end   = LocalDate.now();
        return performanceRepository.findAllByDateBetween(start, end);
    }

    /**
     * 🔹 Hodim bo‘yicha barcha hisobot
     */
    public List<SalesPerformance> getUserPerformance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        return performanceRepository.findAllByEmployee(user);
    }

    /**
     * 🔹 Bonus formulasini o‘zgartirish
     */
    public void updateBonusFormula(Double newBonus) {
        LocalDate today = LocalDate.now();
        List<SalesPerformance> todayPerf = performanceRepository.findAllByDate(today);

        for (SalesPerformance p : todayPerf) {
            p.setBonusPerPayment(newBonus);
            double baseBonus  = p.getPaidClientsCount() * newBonus;
            double extraBonus = p.isQualifiedForBonus() ? 500_000 : 0;
            p.setTotalBonus(baseBonus + extraBonus);
            p.setUpdatedAt(LocalDateTime.now());
            performanceRepository.save(p);
        }
    }

    /**
     * 🔹 Qo‘lda CRUD (ozing bonus kiritish uchun)
     */
    public SalesPerformance manualSave(SalesPerformance perf) {
        if (perf.getEmployee() == null || perf.getDate() == null)
            throw new CustomException("Employee and date required", HttpStatus.BAD_REQUEST);
        perf.setUpdatedAt(LocalDateTime.now());
        return performanceRepository.save(perf);
    }

    public void deletePerformance(Long id) {
        if (!performanceRepository.existsById(id))
            throw new CustomException("Performance not found", HttpStatus.NOT_FOUND);
        performanceRepository.deleteById(id);
    }
}
