package com.crm.lead.reposiroty;

import com.crm.lead.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    @Query("SELECT l FROM Lead l " +
            "WHERE (:query IS NULL OR LOWER(l.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(l.phone) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(l.region) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(l.targetCountry) LIKE LOWER(CONCAT('%', :query, '%')) )")
    List<Lead> searchLeads(@Param("query") String query);

    @Query("SELECT l FROM Lead l WHERE l.deleted = false AND l.archived = false")
    List<Lead> findAllActive();

    @Query("SELECT l FROM Lead l WHERE l.archived = true")
    List<Lead> findAllArchived();

    @Query("SELECT l FROM Lead l WHERE l.deleted = true")
    List<Lead> findAllDeleted();


}
