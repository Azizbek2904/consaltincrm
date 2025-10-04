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
    private Set<Permission> defaultPermissionsForRole(Role role) {
        return switch (role) {
            case SUPER_ADMIN -> Set.of(Permission.values()); // 🔑 hamma narsaga huquq

            case ADMIN -> Set.of(
                    Permission.USER_VIEW,
                    Permission.USER_CREATE,
                    Permission.USER_UPDATE,
                    Permission.USER_DELETE,
                    Permission.USER_ASSIGN_ROLES,
                    Permission.USER_MANAGE_PERMISSIONS,

                    Permission.CLIENT_VIEW,
                    Permission.CLIENT_UPDATE,

                    Permission.LEAD_VIEW,
                    Permission.LEAD_UPDATE,
                    Permission.LEAD_DELETE,

                    Permission.LEAD_STATUS_CREATE,
                    Permission.LEAD_STATUS_VIEW,
                    Permission.LEAD_STATUS_DELETE
            );

            case FINANCE -> Set.of(
                    Permission.PAYMENT_VIEW,
                    Permission.PAYMENT_UPLOAD,
                    Permission.CLIENT_VIEW,
                    Permission.CLIENT_UPDATE,
                    Permission.DOCUMENT_UPLOAD
            );

            case DOCUMENTS -> Set.of(
                    Permission.DOCUMENT_UPLOAD,
                    Permission.DOCUMENT_VIEW,
                    Permission.LEAD_VIEW,
                    Permission.CLIENT_VIEW,
                    Permission.CLIENT_UPDATE,
                    Permission.LEAD_UPDATE
            );

            case SALES_MANAGER -> Set.of(
                    Permission.LEAD_VIEW,
                    Permission.LEAD_CREATE,
                    Permission.LEAD_UPDATE,
                    Permission.LEAD_DELETE,

                    Permission.CLIENT_VIEW,
                    Permission.PAYMENT_VIEW,
                    Permission.DOCUMENT_VIEW
            );

            case MANAGER -> Set.of(
                    Permission.LEAD_VIEW,
                    Permission.LEAD_CONVERT_TO_CLIENT,
                    Permission.CLIENT_VIEW,
                    Permission.CLIENT_UPDATE,
                    Permission.CLIENT_DELETE,

                    Permission.DOCUMENT_UPLOAD,
                    Permission.PAYMENT_UPLOAD
            );

            case MANAGER_CONSULTANT -> Set.of(
                    Permission.CLIENT_VIEW,
                    Permission.CLIENT_UPDATE,
                    Permission.LEAD_VIEW,
                    Permission.LEAD_CREATE
            );

            case RECEPTION -> Set.of(
                    Permission.CLIENT_VIEW,
                    Permission.LEAD_VIEW,
                    Permission.LEAD_UPDATE,

                    Permission.RECEPTION_CHECK_IN,
                    Permission.RECEPTION_CHECK_OUT,
                    Permission.RECEPTION_VIEW_ATTENDANCE,
                    Permission.RECEPTION_DAILY_REPORT,
                    Permission.RECEPTION_WEEKLY_REPORT,
                    Permission.RECEPTION_MONTHLY_REPORT,
                    Permission.RECEPTION_SCHEDULE_LEAD,
                    Permission.RECEPTION_SCHEDULE_CLIENT,
                    Permission.RECEPTION_MARK_CAME,
                    Permission.RECEPTION_MARK_MISSED,
                    Permission.RECEPTION_VIEW_PLANNED,
                    Permission.RECEPTION_VIEW_VISITS
            );

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
