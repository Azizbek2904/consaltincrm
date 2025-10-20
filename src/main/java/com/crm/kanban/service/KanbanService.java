package com.crm.kanban.service;

import com.crm.common.exception.CustomException;
import com.crm.kanban.entity.*;
import com.crm.kanban.repository.*;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KanbanService {

    private final KanbanBoardRepository boardRepository;
    private final KanbanColumnRepository columnRepository;
    private final TaskCardRepository cardRepository;
    private final TaskCommentRepository commentRepository;
    private final UserRepository userRepository;

    // 🧩 BOARD YARATISH
    @Transactional
    public KanbanBoard createBoard(String name) {
        if (name == null || name.isBlank())
            throw new CustomException("Board nomi bo‘sh bo‘lmasligi kerak", HttpStatus.BAD_REQUEST);

        KanbanBoard board = KanbanBoard.builder()
                .name(name.trim())
                .createdAt(LocalDateTime.now())
                .build();
        boardRepository.save(board);

        // Default columns
        List<KanbanColumn> columns = List.of(
                KanbanColumn.builder().title("To Do").position(1).board(board).build(),
                KanbanColumn.builder().title("In Progress").position(2).board(board).build(),
                KanbanColumn.builder().title("Done").position(3).board(board).build()
        );
        columnRepository.saveAll(columns);
        board.setColumns(columns);
        return board;
    }

    // 📋 BARCHA BOARDLAR
    public List<KanbanBoard> getAllBoards() {
        return boardRepository.findAllByOrderByCreatedAtDesc();
    }

    // 📋 BOARD + COLUMN + TASKLAR
    @Transactional(readOnly = true)
    public KanbanBoard getBoard(Long id) {
        KanbanBoard board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException("Board topilmadi", HttpStatus.NOT_FOUND));

        List<KanbanColumn> columns = columnRepository.findByBoardId(id);
        columns.forEach(col -> {
            List<TaskCard> tasks = cardRepository.findByColumnIdOrderByPositionAsc(col.getId());
            tasks.sort(Comparator.comparingInt(TaskCard::getPosition));
            col.setCards(tasks);
        });
        board.setColumns(columns);
        return board;
    }

    // 🆕 TASK YARATISH
    @Transactional
    public TaskCard createTask(String title, String description, Long userId, Long columnId) {
        if (title == null || title.isBlank())
            throw new CustomException("Task nomi bo‘sh bo‘lmasligi kerak", HttpStatus.BAD_REQUEST);

        KanbanColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new CustomException("Ustun topilmadi", HttpStatus.NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Foydalanuvchi topilmadi", HttpStatus.NOT_FOUND));

        int nextPosition = cardRepository.findByColumnIdOrderByPositionAsc(columnId).size() + 1;

        TaskCard task = TaskCard.builder()
                .title(title.trim())
                .description(description != null ? description.trim() : "")
                .assignedUser(user)
                .column(column)
                .status(TaskStatus.valueOf(column.getTitle().replace(" ", "_").toUpperCase()))
                .position(nextPosition)
                .completed(false)
                .date(LocalDate.now())
                .time(LocalTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return cardRepository.save(task);
    }

    // ✏️ TASK YANGILASH
    @Transactional
    public TaskCard updateTask(Long id, String title, String description, Long userId, TaskStatus status) {
        TaskCard task = cardRepository.findById(id)
                .orElseThrow(() -> new CustomException("Task topilmadi", HttpStatus.NOT_FOUND));

        if (title != null && !title.isBlank()) task.setTitle(title.trim());
        if (description != null) task.setDescription(description.trim());

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException("Foydalanuvchi topilmadi", HttpStatus.NOT_FOUND));
            task.setAssignedUser(user);
        }

        if (status != null) {
            task.setStatus(status);
            task.setCompleted(status == TaskStatus.DONE);
            KanbanColumn targetColumn = columnRepository
                    .findByBoardIdAndTitleIgnoreCase(task.getColumn().getBoard().getId(), status.getDisplayName())
                    .orElseThrow(() -> new CustomException("Ustun topilmadi", HttpStatus.NOT_FOUND));
            task.setColumn(targetColumn);
        }

        task.setUpdatedAt(LocalDateTime.now());
        return cardRepository.save(task);
    }

    // 🔄 DRAG-DROP
    @Transactional
    public TaskCard moveTask(Long id, TaskStatus newStatus, int newPosition) {
        TaskCard task = cardRepository.findById(id)
                .orElseThrow(() -> new CustomException("Task topilmadi", HttpStatus.NOT_FOUND));

        KanbanColumn target = columnRepository
                .findByBoardIdAndTitleIgnoreCase(task.getColumn().getBoard().getId(), newStatus.getDisplayName())
                .orElseThrow(() -> new CustomException("Ustun topilmadi", HttpStatus.NOT_FOUND));

        task.setColumn(target);
        task.setStatus(newStatus);
        task.setPosition(newPosition);
        task.setCompleted(newStatus == TaskStatus.DONE);
        task.setUpdatedAt(LocalDateTime.now());
        return cardRepository.save(task);
    }

    // ✅ COMPLETE
    @Transactional
    public TaskCard completeTask(Long id, boolean completed) {
        TaskCard task = cardRepository.findById(id)
                .orElseThrow(() -> new CustomException("Task topilmadi", HttpStatus.NOT_FOUND));

        task.setCompleted(completed);
        task.setStatus(completed ? TaskStatus.DONE : TaskStatus.IN_PROGRESS);
        task.setUpdatedAt(LocalDateTime.now());
        return cardRepository.save(task);
    }

    // 💬 COMMENT QO‘SHISH
    @Transactional
    public TaskComment addComment(Long taskId, String message, Long userId) {
        TaskCard task = cardRepository.findById(taskId)
                .orElseThrow(() -> new CustomException("Task topilmadi", HttpStatus.NOT_FOUND));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Foydalanuvchi topilmadi", HttpStatus.NOT_FOUND));

        TaskComment comment = TaskComment.builder()
                .task(task)
                .author(author)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        return commentRepository.save(comment);
    }

    // 💬 COMMENTLARNI OLISH
    @Transactional(readOnly = true)
    public List<TaskComment> getComments(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    // 👤 USER TASKLARI
    @Transactional(readOnly = true)
    public List<TaskCard> getTasksByUser(Long userId) {
        if (!userRepository.existsById(userId))
            throw new CustomException("Foydalanuvchi topilmadi", HttpStatus.NOT_FOUND);
        return cardRepository.findByAssignedUserIdOrderByDateDesc(userId);
    }
    // ✅ BOARDNI O‘CHIRISH
    @Transactional
    public void deleteBoard(Long boardId) {
        KanbanBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException("Board topilmadi", HttpStatus.NOT_FOUND));

        // Oldin ustunlar (columns) va tasklarni o‘chiramiz (CascadeType.ALL bo‘lsa ham barqarorlik uchun)
        List<KanbanColumn> columns = columnRepository.findByBoardId(boardId);
        for (KanbanColumn col : columns) {
            cardRepository.deleteAll(col.getCards());
        }
        columnRepository.deleteAll(columns);

        // Keyin boardni o‘chiramiz
        boardRepository.delete(board);
    }

    // ❌ O‘CHIRISH
    @Transactional
    public void deleteTask(Long id) {
        if (!cardRepository.existsById(id))
            throw new CustomException("Task topilmadi", HttpStatus.NOT_FOUND);
        cardRepository.deleteById(id);
    }
}
