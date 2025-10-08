package com.crm.client.repository;

import com.crm.client.dto.PaymentStatus;
import com.crm.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    // 🔎 Asosiy filter (hammasini optional qildim)
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


    List<Client> findByArchivedFalseAndDeletedFalse();

    // Archived
    List<Client> findByArchivedTrueAndDeletedFalse();

    // Deleted
    List<Client> findByDeletedTrue();

    // Filter: Active by Region / Country
    List<Client> findByArchivedFalseAndDeletedFalseAndRegion(String region);

    List<Client> findByArchivedFalseAndDeletedFalseAndTargetCountry(String targetCountry);

    List<Client> findByArchivedFalseAndDeletedFalseAndRegionAndTargetCountry(String region, String targetCountry);


   // List<Client> findByConvertedToMainPaymentTrueAndDeletedFalse();
    List<Client> findByMainPaymentTrueAndDeletedFalse();


}
