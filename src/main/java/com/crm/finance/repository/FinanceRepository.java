package com.crm.finance.repository;

import com.crm.client.entity.Client;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinanceRepository extends JpaRepository<Client, Long> {

    // ✅ Bugungi tushum (totalPayment)
    @Query("SELECT COALESCE(SUM(c.totalPayment), 0) FROM Client c WHERE c.totalPaymentDate = CURRENT_DATE")
    Double getTodayIncome();

    // ✅ So‘nggi 7 kunlik tushum
    @Query("SELECT COALESCE(SUM(c.totalPayment), 0) FROM Client c WHERE c.totalPaymentDate BETWEEN :start AND :end")
    Double getWeeklyIncome(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // ✅ Joriy oydagi tushum
    @Query("SELECT COALESCE(SUM(c.totalPayment), 0) FROM Client c WHERE MONTH(c.totalPaymentDate) = :month AND YEAR(c.totalPaymentDate) = :year")
    Double getMonthlyIncome(@Param("month") int month, @Param("year") int year);

    // ✅ O‘tgan oy uchun tushum
    @Query("SELECT COALESCE(SUM(c.totalPayment), 0) FROM Client c WHERE MONTH(c.totalPaymentDate) = :month AND YEAR(c.totalPaymentDate) = :year")
    Double getPreviousMonthIncome(@Param("month") int month, @Param("year") int year);

    // ✅ Yillik tushum
    @Query("SELECT COALESCE(SUM(c.totalPayment), 0) FROM Client c WHERE YEAR(c.totalPaymentDate) = :year")
    Double getYearlyIncome(@Param("year") int year);

    // ✅ Region bo‘yicha tushum
    @Query("SELECT c.region, COALESCE(SUM(c.totalPayment), 0) FROM Client c GROUP BY c.region")
    List<Object[]> getIncomeByRegion();

    // ✅ Country bo‘yicha tushum
    @Query("SELECT c.targetCountry, COALESCE(SUM(c.totalPayment), 0) FROM Client c GROUP BY c.targetCountry")
    List<Object[]> getIncomeByCountry();

    // ✅ Foyda (totalPayment - initialPayment)
    @Query("SELECT COALESCE(SUM(c.totalPayment - c.initialPayment), 0) FROM Client c")
    Double getTotalProfit();

    // ✅ O‘rtacha tushum
    @Query("SELECT COALESCE(AVG(c.totalPayment), 0) FROM Client c WHERE c.totalPayment IS NOT NULL")
    Double getAveragePayment();
}
