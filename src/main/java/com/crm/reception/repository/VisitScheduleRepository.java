package com.crm.reception.repository;

import com.crm.reception.entity.VisitSchedule;
import com.crm.reception.entity.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitScheduleRepository extends JpaRepository<VisitSchedule, Long> {
    List<VisitSchedule> findByStatus(VisitStatus status);
    List<VisitSchedule> findByScheduledDateTimeBetween(LocalDateTime start, LocalDateTime end);

    Optional<VisitSchedule> findFirstByLeadIdAndStatusOrderByScheduledDateTimeAsc(Long leadId, VisitStatus status);
    Optional<VisitSchedule> findFirstByClientIdAndStatusOrderByScheduledDateTimeAsc(Long clientId, VisitStatus status);

}
