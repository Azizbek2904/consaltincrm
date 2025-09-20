package com.crm.leadassign.repository;
import com.crm.leadassign.entity.LeadAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface LeadAssignmentRepository extends JpaRepository<LeadAssignment, Long> {
    List<LeadAssignment> findBySalesManager_FullNameContainingIgnoreCase(String salesManagerName);
    List<LeadAssignment> findByAssignedAtBetween(LocalDateTime start, LocalDateTime end);

}
