package com.crm.kanban.repository;

import com.crm.kanban.entity.KanbanColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KanbanColumnRepository extends JpaRepository<KanbanColumn, Long> {

    List<KanbanColumn> findByBoardId(Long boardId);
    Optional<KanbanColumn> findByBoardIdAndTitleIgnoreCase(Long boardId, String title);
}
