package com.crm.user.controller;
import com.crm.common.util.ApiResponse;
import com.crm.user.dto.PermissionUpdateRequest;
import com.crm.user.dto.UserRequest;
import com.crm.user.dto.UserResponse;
import com.crm.user.dto.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    // ✅ 1. Hodim qo‘shish (faqat SUPER_ADMIN va ADMIN)
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('CREATE_USERS')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User created", userService.createUser(request)));
    }

    // ✅ 2. Hodimni yangilash
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('UPDATE_USERS')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("User updated", userService.updateUser(id, request)));
    }

    // ✅ 3. Barcha hodimlarni olish
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('VIEW_USERS')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok("All users", userService.getAllUsers()));
    }

    // ✅ 4. Permission yangilash
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('MANAGE_PERMISSIONS')")
    public ResponseEntity<ApiResponse<UserResponse>> updatePermissions(@PathVariable Long id,
                                                                       @RequestBody PermissionUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Permissions updated", userService.updatePermissions(id, request)));
    }

    // ✅ 5. Soft delete
    @DeleteMapping("/{id}/soft")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('DELETE_USERS')")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 6. Archive
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> archiveUser(@PathVariable Long id) {
        userService.archiveUser(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 7. Blocked foydalanuvchilar ro‘yxati
    @GetMapping("/blocked")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getBlockedUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Blocked users", userService.getBlockedUsers()));
    }

    // ✅ 8. Status (block/unblock)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable Long id,
                                                                  @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", userService.updateStatus(id, active)));
    }

    // ✅ 9. Restore (o‘chirilgan yoki arxivdan qaytarish)
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 10. Permanent delete (faqat SUPER_ADMIN)
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> permanentDeleteUser(@PathVariable Long id) {
        userService.permanentDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 11. Archived users
    @GetMapping("/archived")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getArchivedUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Archived users", userService.getArchivedUsers()));
    }

    // ✅ 12. Deleted users
    @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getDeletedUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Deleted users", userService.getDeletedUsers()));
    }
}
