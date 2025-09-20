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

        // ✅ 1. Hodim qo‘shish (faqat Super Admin va Admin)
        @PostMapping("/create")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
        public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
            return ResponseEntity.ok(ApiResponse.ok("User created", userService.createUser(request)));
        }

        // ✅ 2. Barcha hodimlarni olish
        @GetMapping
        @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
        public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
            return ResponseEntity.ok(ApiResponse.ok("All users", userService.getAllUsers()));
        }

        // ✅ 3. Permission yangilash
        @PutMapping("/{id}/permissions")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
        public ResponseEntity<ApiResponse<UserResponse>> updatePermissions(@PathVariable Long id,
                                                                           @RequestBody PermissionUpdateRequest request) {
            return ResponseEntity.ok(ApiResponse.ok("Permissions updated", userService.updatePermissions(id, request)));
        }

        // ✅ 4. Hodimni bloklash yoki faollashtirish
        @PutMapping("/{id}/status")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
        public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable Long id,
                                                                      @RequestParam boolean active) {
            return ResponseEntity.ok(ApiResponse.ok("Status updated", userService.updateStatus(id, active)));
        }

        // ✅ 5. Hodimni o‘chirish (faqat Super Admin va Admin)
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
        }

        @DeleteMapping("/{id}/soft")
        public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
            userService.softDeleteUser(id);
            return ResponseEntity.noContent().build();
        }

        // ✅ Archive (alohida belgilash)
        @PutMapping("/{id}/archive")
        public ResponseEntity<Void> archiveUser(@PathVariable Long id) {
            userService.archiveUser(id);
            return ResponseEntity.noContent().build();
        }

        // ✅ Restore (o‘chirilgan yoki arxivdan qaytarish)
        @PutMapping("/{id}/restore")
        public ResponseEntity<Void> restoreUser(@PathVariable Long id) {
            userService.restoreUser(id);
            return ResponseEntity.noContent().build();
        }

        // ❌ Permanent delete (faqat SUPER_ADMIN ruxsat)
        @DeleteMapping("/{id}/permanent")
        public ResponseEntity<Void> permanentDeleteUser(@PathVariable Long id) {
            userService.permanentDeleteUser(id);
            return ResponseEntity.noContent().build();
        }

        // ✅ Archived foydalanuvchilar ro‘yxati
        @GetMapping("/archived")
        public ResponseEntity<List<UserResponse>> getArchivedUsers() {
            return ResponseEntity.ok(userService.getArchivedUsers());
        }

        // ✅ Deleted foydalanuvchilar ro‘yxati
        @GetMapping("/deleted")
        public ResponseEntity<List<UserResponse>> getDeletedUsers() {
            return ResponseEntity.ok(userService.getDeletedUsers());
        }


    }
