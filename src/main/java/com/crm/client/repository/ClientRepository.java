package com.crm.client.repository;

import com.crm.client.dto.PaymentStatus;
import com.crm.client.entity.Client;
import com.crm.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    // 🔍 Foydalanuvchiga mos filtrlash (query bilan)
    @Query("SELECT c FROM Client c " +
            "WHERE (:status IS NULL OR c.paymentStatus = :status) " +
            "AND (:targetCountry IS NULL OR LOWER(c.targetCountry) = LOWER(:targetCountry)) " +
            "AND (:start IS NULL OR (c.initialPaymentDate IS NOT NULL AND c.initialPaymentDate >= :start)) " +
            "AND (:end IS NULL OR (c.initialPaymentDate IS NOT NULL AND c.initialPaymentDate <= :end))")
    List<Client> filterClients(
            @Param("status") PaymentStatus status,
            @Param("targetCountry") String targetCountry,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // 🔥 Batch delete (filter asosida)
    @Modifying
    @Query("DELETE FROM Client c " +
            "WHERE (:status IS NULL OR c.paymentStatus = :status) " +
            "AND (:targetCountry IS NULL OR LOWER(c.targetCountry) = LOWER(:targetCountry)) " +
            "AND (:start IS NULL OR (c.initialPaymentDate IS NOT NULL AND c.initialPaymentDate >= :start)) " +
            "AND (:end IS NULL OR (c.initialPaymentDate IS NOT NULL AND c.initialPaymentDate <= :end))")
    void deleteFilteredClients(
            @Param("status") PaymentStatus status,
            @Param("targetCountry") String targetCountry,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // 🧩 Asosiy filterlar
    List<Client> findByArchivedFalseAndDeletedFalse();

    List<Client> findByArchivedTrueAndDeletedFalse();

    List<Client> findByDeletedTrue();

    List<Client> findByArchivedFalseAndDeletedFalseAndRegion(String region);

    List<Client> findByArchivedFalseAndDeletedFalseAndTargetCountry(String targetCountry);

    List<Client> findByArchivedFalseAndDeletedFalseAndRegionAndTargetCountry(String region, String targetCountry);

    List<Client> findByMainPaymentTrueAndDeletedFalse();


    // ===============================
    // 🚀 Performance va bonus tizimi uchun kerakli 3ta metod:
    // ===============================

    // 1️⃣ Hodim (convertedBy) nechta client yaratgan (Lead → Client)
    int countByConvertedByAndLead_ConvertedToClientTrue(User convertedBy);

    // 2️⃣ Hodim nechta main payment (asosiy to‘lov) qilgan
    int countByConvertedByAndMainPaymentTrueAndTotalPaymentIsNotNull(User convertedBy);

    // 3️⃣ Sana bo‘yicha clientlar (trendlarda ishlatiladi)
    long countByInitialPaymentDateBetween(LocalDate start, LocalDate end);

    int countByConvertedByAndContractNumberIsNotNull(User user);

    int countByConvertedByAndMainPaymentTrue(User user);
    long countByConvertedByAndContractNumberIsNotNullAndInitialPaymentDateBetween(
            com.crm.user.entity.User user, java.time.LocalDate start, java.time.LocalDate end);

    long countByConvertedByAndContractNumberIsNotNullAndMainPaymentTrueAndTotalPaymentDateBetween(
            com.crm.user.entity.User user, java.time.LocalDate start, java.time.LocalDate end);

}
