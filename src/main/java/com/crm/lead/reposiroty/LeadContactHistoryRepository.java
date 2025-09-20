package com.crm.lead.reposiroty;

import com.crm.lead.dto.LeadContactHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadContactHistoryRepository extends JpaRepository<LeadContactHistory, Long> {
    List<LeadContactHistory> findByLeadId(Long leadId);
}
