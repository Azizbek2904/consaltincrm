package com.crm.kanban.repository;

import com.crm.kanban.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KanbanBoardRepository extends JpaRepository<KanbanBoard, Long> {
    List<KanbanBoard> findAllByOrderByCreatedAtDesc();

}
