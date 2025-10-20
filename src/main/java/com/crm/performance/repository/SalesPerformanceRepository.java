package com.crm.performance.repository;

import com.crm.performance.entity.SalesPerformance;
import com.crm.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesPerformanceRepository extends JpaRepository<SalesPerformance, Long> {
    Optional<SalesPerformance> findByEmployeeAndDate(User employee, LocalDate date);
    List<SalesPerformance> findAllByDate(LocalDate date);
    List<SalesPerformance> findAllByDateBetween(LocalDate start, LocalDate end);
    List<SalesPerformance> findAllByEmployee(User employee);
}
