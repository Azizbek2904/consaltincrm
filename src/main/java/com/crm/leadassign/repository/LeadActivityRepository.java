package com.crm.leadassign.repository;

import com.crm.leadassign.entity.LeadActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadActivityRepository extends JpaRepository<LeadActivity, Long> {
    List<LeadActivity> findByAssignmentId(Long assignmentId);
    List<LeadActivity> findBySalesManagerId(Long userId);
    List<LeadActivity> findByLeadId(Long leadId);
}
