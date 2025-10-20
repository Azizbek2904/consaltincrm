package com.crm.user.dto;

import com.crm.common.exception.CustomException;
import com.crm.user.entity.Permission;
import com.crm.user.entity.Role;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // ✅ 1. Hodim yaratish
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email already exists", HttpStatus.BAD_REQUEST);
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .department(request.getDepartment())
                .permissions(defaultPermissionsForRole(request.getRole()))
                .active(true) // default = true
                .deleted(false)
                .archived(false)
                .build();

        userRepository.save(user);
        return mapToResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null && !request.getRole().equals(user.getRole())) {
            user.setRole(request.getRole());
            user.setPermissions(defaultPermissionsForRole(request.getRole())); // 🔑 yangi role uchun default permissions
        }

        user.setDepartment(request.getDepartment());

        User updated = userRepository.save(user);
        return mapToResponse(updated);
    }


    // ✅ 2. Faqat active hodimlarni olish
    public List<UserResponse> getAllUsers() {
        return userRepository.findByActiveTrueAndDeletedFalseAndArchivedFalse()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ✅ 3. Hodimga permission update qilish
    public UserResponse updatePermissions(Long userId, PermissionUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        user.setPermissions(request.getPermissions());
        userRepository.save(user);

        return mapToResponse(user);
    }

    // ✅ 4. Hodimni active / block qilish
    // ✅ Status update (to‘g‘rilangan)
    public UserResponse updateStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        user.setActive(active);

        // 🔹 Agar unblock qilinsa → deleted va archived flaglarini ham tozalaymiz
        if (active) {
            user.setDeleted(false);
            user.setArchived(false);
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    // 🔹 Default permissions
    // 🔹 Default permissions
// 🔹 Default permissions
// ✅ Default permissionlar har bir role uchun
    private Set<Permission> defaultPermissionsForRole(Role role) {
        return switch (role) {

            // 🔰 1️⃣ SUPER_ADMIN — barcha ruxsatlar
            case SUPER_ADMIN -> Set.of(Permission.values());

            // 🔰 2️⃣ ADMIN — keng vakolat, lekin cheklangan delete
            case ADMIN -> Set.of(
                    // 👤 USER ruxsatlari
                    Permission.USER_VIEW,
                    Permission.USER_CREATE,
                    Permission.USER_UPDATE,
                    Permission.USER_DELETE,
                    Permission.USER_ASSIGN_ROLES,
                    Permission.USER_MANAGE_PERMISSIONS,

                    // 🧾 CLIENT ruxsatlari
                    Permission.CLIENT_VIEW,
                    Permission.CLIENT_CREATE,
                    Permission.CLIENT_UPDATE,
                    Permission.CLIENT_DELETE,
                    Permission.CLIENT_IMPORT,
                    Permission.CLIENT_EXPORT,

                    // 🎯 LEAD ruxsatlari
                    Permission.LEAD_VIEW,
                    Permission.LEAD_CREATE,
                    Permission.LEAD_UPDATE,
                    Permission.LEAD_DELETE,
                    Permission.LEAD_CONVERT_TO_CLIENT,
                    Permission.LEAD_IMPORT,
                    Permission.LEAD_EXPORT,

                    // 🔄 LEAD STATUS
                    Permission.LEAD_STATUS_CREATE,
                    Permission.LEAD_STATUS_VIEW,
                    Permission.LEAD_STATUS_DELETE,

                    // 📋 LEAD ASSIGN
                    Permission.LEAD_ASSIGN_CREATE,
                    Permission.LEAD_ASSIGN_VIEW,
                    Permission.LEAD_ASSIGN_DELETE,
                    Permission.LEAD_ASSIGN_REASSIGN,

                    // 📂 DOCUMENT & PAYMENT
                    Permission.DOCUMENT_VIEW,
                    Permission.DOCUMENT_UPLOAD,
                    Permission.PAYMENT_VIEW,
                    Permission.PAYMENT_UPLOAD,

                    // 📊 KANBAN
                    Permission.KANBAN_BOARD_CREATE,
                    Permission.KANBAN_BOARD_VIEW,
                    Permission.KANBAN_TASK_CREATE,
                    Permission.KANBAN_TASK_UPDATE,
                    Permission.KANBAN_TASK_MOVE,
                    Permission.KANBAN_TASK_COMPLETE,
                    Permission.KANBAN_TASK_VIEW_USER
            );

            // 🔰 3️⃣ MANAGER — jarayonlarni boshqaradi
            case MANAGER -> Set.of(
                    Permission.LEAD_VIEW,
                    Permission.LEAD_UPDATE,
                    Permission.LEAD_ASSIGN_VIEW,
                    Permission.LEAD_ASSIGN_CREATE,
                    Permission.LEAD_ASSIGN_DELETE,
                    Permission.CLIENT_VIEW,
                    Permission.CLIENT_UPDATE,

                    // 📊 KANBAN
                    Permission.KANBAN_BOARD_VIEW,
                    Permission.KANBAN_TASK_MOVE,
                    Permission.KANBAN_TASK_COMPLETE,
                    Permission.KANBAN_TASK_VIEW_USER
            );

            // 🔰 4️⃣ SALES_MANAGER — mijoz va lead bilan ishlaydi
            case SALES_MANAGER -> Set.of(
                    Permission.LEAD_VIEW,
                    Permission.LEAD_UPDATE,
                    Permission.LEAD_ACTIVITY_ADD,
                    Permission.LEAD_ACTIVITY_VIEW,
                    Permission.CLIENT_VIEW,
                    Permission.PAYMENT_VIEW,

                    // 📊 KANBAN
                    Permission.KANBAN_BOARD_VIEW,
                    Permission.KANBAN_TASK_MOVE,
                    Permission.KANBAN_TASK_COMPLETE,
                    Permission.KANBAN_TASK_VIEW_USER
            );

            // 🔰 5️⃣ DOCUMENTS — hujjatlar bilan ishlovchi
            case DOCUMENTS -> Set.of(
                    Permission.DOCUMENT_UPLOAD,
                    Permission.DOCUMENT_VIEW,
                    Permission.LEAD_VIEW,
                    Permission.CLIENT_VIEW,

                    // 📊 KANBAN
                    Permission.KANBAN_TASK_COMPLETE,
                    Permission.KANBAN_TASK_VIEW_USER
            );

            // 🔰 6️⃣ FINANCE — to‘lovlar bilan ishlaydi
            case FINANCE -> Set.of(
                    Permission.PAYMENT_VIEW,
                    Permission.PAYMENT_UPLOAD,
                    Permission.CLIENT_VIEW,

                    // 📊 KANBAN
                    Permission.KANBAN_TASK_COMPLETE,
                    Permission.KANBAN_TASK_VIEW_USER
            );

            // 🔰 7️⃣ MANAGER_CONSULTANT — mijoz bilan bevosita ishlaydi
            case MANAGER_CONSULTANT -> Set.of(
                    Permission.CLIENT_VIEW,
                    Permission.CLIENT_UPDATE,
                    Permission.LEAD_VIEW,
                    Permission.LEAD_CREATE,

                    // 📊 KANBAN
                    Permission.KANBAN_TASK_COMPLETE,
                    Permission.KANBAN_TASK_VIEW_USER
            );

            // 🔰 8️⃣ RECEPTION — tashriflar, yo‘qlamalar
            case RECEPTION -> Set.of(
                    Permission.CLIENT_VIEW,
                    Permission.LEAD_VIEW,

                    // 🕓 Reception faoliyatlari
                    Permission.RECEPTION_VIEW_ATTENDANCE,
                    Permission.RECEPTION_VIEW_VISITS,
                    Permission.RECEPTION_CHECK_IN,
                    Permission.RECEPTION_CHECK_OUT,
                    Permission.RECEPTION_SCHEDULE_LEAD,
                    Permission.RECEPTION_SCHEDULE_CLIENT,
                    Permission.RECEPTION_MARK_CAME,
                    Permission.RECEPTION_MARK_MISSED,

                    // 📊 KANBAN
                    Permission.KANBAN_TASK_COMPLETE,
                    Permission.KANBAN_TASK_VIEW_USER
            );

            // 🔰 9️⃣ DEFAULT — boshqa holatlar
            default -> Set.of();
        };
    }


    // ✅ 5. Hodimni o‘chirish
    // ✅ Soft delete
    // ✅ Archive
    // ✅ Restore

    public void restoreUser(Long id) {
        User user = getUserOrThrow(id);
        user.setDeleted(false);
        user.setArchived(false);
        user.setActive(true);
        userRepository.save(user);
    }

    // ❌ Permanent delete
    public void permanentDeleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }


    public List<UserResponse> getArchivedUsers() {
        return userRepository.findAll().stream()
                .filter(User::isArchived) // faqat archived = true
                .map(this::mapToResponse)
                .toList();
    }

    // ✅ Blocked list
    public List<UserResponse> getBlockedUsers() {
        return userRepository.findAll().stream()
                .filter(u -> !u.isActive() && !u.isDeleted() && !u.isArchived())
                .map(this::mapToResponse)
                .toList();
    }
    // ✅ Deleted list
    public List<UserResponse> getDeletedUsers() {
        return userRepository.findByDeletedTrue()
                .stream().map(this::mapToResponse).toList();
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
    }
    // ✅ Soft delete
    public void softDeleteUser(Long id) {
        User user = getUserOrThrow(id);
        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);
    }

    // ✅ Archive
    public void archiveUser(Long id) {
        User user = getUserOrThrow(id);
        user.setArchived(true);
        user.setActive(false);
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .permissions(user.getPermissions())
                .active(user.isActive())
                .department(user.getDepartment())
                .archived(user.isArchived())
                .deleted(user.isDeleted())
                .build();
    }
}
