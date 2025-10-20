package com.crm.lead.reposiroty;

import com.crm.lead.entity.Lead;
import com.crm.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    @Query("""
       SELECT l FROM Lead l
       WHERE (:query IS NULL OR :query = '' 
          OR LOWER(l.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
          OR l.phone LIKE CONCAT('%', :query, '%')
          OR LOWER(l.region) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(l.targetCountry) LIKE LOWER(CONCAT('%', :query, '%'))
       )
       AND l.deleted = false
       """)
    List<Lead> searchLeads(@Param("query") String query);

    List<Lead> findByAssignedToIsNull(); // ✅ bo'sh (unassigned)

    @Query("SELECT l FROM Lead l WHERE l.assigned = false AND l.archived = false AND l.deleted = false")
    List<Lead> findAllActiveUnassigned();

    // faqat unassigned va deleted = false leadlarni ko‘rish
    List<Lead> findAllByAssignedToIsNullAndDeletedFalseAndArchivedFalse();

    // faqat manager uchun
    List<Lead> findAllByAssignedToAndDeletedTrue(User assignedTo);
    List<Lead> findByAssignedTo(User user);

    List<Lead> findAllByAssignedFalseAndDeletedFalse();

    // Hodim o‘ziga biriktirilgan leadlarni ko‘radi
    List<Lead> findAllByAssignedToId(Long userId);

    // Unassigned ro‘yxat uchun
    List<Lead> findAllByAssignedFalseAndDeletedFalseAndArchivedFalse();

    // Sales managerga biriktirilganlar uchun



    @Query("SELECT l FROM Lead l WHERE l.deleted = true")
    List<Lead> findAllDeleted();


    List<Lead> findAllByDeletedFalse();   // faollari
    List<Lead> findAllByDeletedTrue();    // o‘chirilganlari

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
