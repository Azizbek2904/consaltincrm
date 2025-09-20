package com.crm.client.repository;

import com.crm.client.dto.PaymentStatus;
import com.crm.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @Query("SELECT c FROM Client c " +
            "WHERE (:query IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(c.phone1) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(c.phone2) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(c.region) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(c.targetCountry) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "  AND (:status IS NULL OR c.paymentStatus = :status)")
    List<Client> searchClients(@Param("query") String query,
                               @Param("status") PaymentStatus status);

    @Query("SELECT c FROM Client c WHERE c.deleted = false AND c.archived = false")
    List<Client> findAllActive();

    @Query("SELECT c FROM Client c WHERE c.archived = true")
    List<Client> findAllArchived();

    @Query("SELECT c FROM Client c WHERE c.deleted = true")
    List<Client> findAllDeleted();

}
