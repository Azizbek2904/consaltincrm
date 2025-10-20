package com.crm.kanban.controller;

import com.crm.common.util.ApiResponse;
import com.crm.kanban.entity.KanbanBoard;
import com.crm.kanban.entity.TaskCard;
import com.crm.kanban.entity.TaskComment;
import com.crm.kanban.entity.TaskStatus;
import com.crm.kanban.service.KanbanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kanban")
@RequiredArgsConstructor
public class KanbanController {

    private final KanbanService service;

    // ✅ 1️⃣ BOARD YARATISH — SUPER_ADMIN, ADMIN
    @PostMapping("/board")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('KANBAN_BOARD_CREATE')")
    public ResponseEntity<ApiResponse<KanbanBoard>> createBoard(@RequestParam String name) {
        KanbanBoard board = service.createBoard(name);
        return ResponseEntity.ok(ApiResponse.ok("Board yaratildi", board));
    }

    // ✅ 2️⃣ BARCHA BOARDLARNI OLISH
    @GetMapping("/boards")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER') or hasAuthority('KANBAN_BOARD_VIEW')")
    public ResponseEntity<ApiResponse<List<KanbanBoard>>> getAllBoards() {
        return ResponseEntity.ok(ApiResponse.ok("Barcha boardlar", service.getAllBoards()));
    }

    // ✅ 3️⃣ BITTA BOARDNI OLISH (column va tasklar bilan)
    @GetMapping("/board/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','SALES_MANAGER') or hasAuthority('KANBAN_BOARD_VIEW')")
    public ResponseEntity<ApiResponse<KanbanBoard>> getBoard(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Board yuklandi", service.getBoard(id)));
    }
    // ✅ 13️⃣ BOARDNI O‘CHIRISH — faqat SUPER_ADMIN yoki ADMIN
    @DeleteMapping("/board/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('KANBAN_BOARD_DELETE')")
    public ResponseEntity<ApiResponse<String>> deleteBoard(@PathVariable Long id) {
        service.deleteBoard(id);
        return ResponseEntity.ok(ApiResponse.ok("Board o‘chirildi", "Deleted"));
    }

    // ✅ 4️⃣ TASK YARATISH
    @PostMapping("/task")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('KANBAN_TASK_CREATE')")
    public ResponseEntity<ApiResponse<TaskCard>> createTask(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Long userId,
            @RequestParam Long columnId
    ) {
        TaskCard task = service.createTask(title, description, userId, columnId);
        return ResponseEntity.ok(ApiResponse.ok("Task yaratildi", task));
    }

    // ✅ 5️⃣ TASK YANGILASH (nom, description, foydalanuvchi, status)
    @PutMapping("/task/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER') or hasAuthority('KANBAN_TASK_UPDATE')")
    public ResponseEntity<ApiResponse<TaskCard>> updateTask(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) TaskStatus status
    ) {
        TaskCard updated = service.updateTask(id, title, description, userId, status);
        return ResponseEntity.ok(ApiResponse.ok("Task yangilandi", updated));
    }

    // ✅ 6️⃣ TASKGA FOYDALANUVCHI BIRIKTIRISH
    @PutMapping("/task/{id}/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TaskCard>> assignUserToTask(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        TaskCard updated = service.updateTask(id, null, null, userId, null);
        return ResponseEntity.ok(ApiResponse.ok("Foydalanuvchi biriktirildi", updated));
    }

    // ✅ 7️⃣ DRAG-DROP ORQALI STATUS O‘ZGARTIRISH
    @PutMapping("/task/{id}/move")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<TaskCard>> moveTask(
            @PathVariable Long id,
            @RequestParam TaskStatus newStatus,
            @RequestParam(defaultValue = "1") int newPosition
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Task ko‘chirildi", service.moveTask(id, newStatus, newPosition)));
    }

    // ✅ 8️⃣ TASKNI BAJARILGAN DEB BELGILASH
    @PutMapping("/task/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','SALES_MANAGER','FINANCE','DOCUMENTS','RECEPTION')")
    public ResponseEntity<ApiResponse<TaskCard>> completeTask(@PathVariable Long id, @RequestParam boolean completed) {
        return ResponseEntity.ok(ApiResponse.ok("Task yangilandi", service.completeTask(id, completed)));
    }

    // ✅ 9️⃣ TASKGA COMMENT QO‘SHISH
    @PostMapping("/task/{id}/comment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','SALES_MANAGER','FINANCE','DOCUMENTS','RECEPTION')")
    public ResponseEntity<ApiResponse<TaskComment>> addComment(
            @PathVariable Long id,
            @RequestParam String message,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Izoh qo‘shildi", service.addComment(id, message, userId)));
    }

    // ✅ 🔟 TASK COMMENTLARNI OLISH
    @GetMapping("/task/{id}/comments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','SALES_MANAGER','FINANCE','DOCUMENTS','RECEPTION')")
    public ResponseEntity<ApiResponse<List<TaskComment>>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Task izohlari", service.getComments(id)));
    }

    // ✅ 11️⃣ HODIM TASKLARI
    @GetMapping("/tasks/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<List<TaskCard>>> getTasksByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Foydalanuvchi tasklari", service.getTasksByUser(userId)));
    }

    // ✅ 12️⃣ TASKNI O‘CHIRISH
    @DeleteMapping("/task/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable Long id) {
        service.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.ok("Task o‘chirildi", "Deleted"));
    }
}
