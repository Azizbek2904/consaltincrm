package com.crm.kanban.repository;

import com.crm.kanban.entity.TaskCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCardRepository extends JpaRepository<TaskCard, Long> {
    List<TaskCard> findByColumnIdOrderByPositionAsc(Long columnId);
    List<TaskCard> findByAssignedUserIdOrderByDateDesc(Long userId);


}
