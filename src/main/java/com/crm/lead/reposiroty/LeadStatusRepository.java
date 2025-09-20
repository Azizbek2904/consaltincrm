package com.crm.lead.reposiroty;

import com.crm.lead.entity.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeadStatusRepository extends JpaRepository<LeadStatus, Long> {
    Optional<LeadStatus> findByNameIgnoreCase(String name);
}
