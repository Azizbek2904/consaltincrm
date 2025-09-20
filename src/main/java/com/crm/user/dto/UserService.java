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
import java.util.stream.Collectors;

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
                .active(true)
                .build();

        userRepository.save(user);
        return mapToResponse(user);
    }

    // ✅ 2. Barcha hodimlarni olish
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
    public UserResponse updateStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        user.setActive(active);
        userRepository.save(user);

        return mapToResponse(user);
    }

    // Default permissions
    private Set<Permission> defaultPermissionsForRole(Role role) {
        switch (role) {
            case ADMIN -> {
                return Set.of(Permission.CREATE_USERS, Permission.VIEW_USERS, Permission.ASSIGN_ROLES);
            }
            case FINANCE -> {
                return Set.of(Permission.VIEW_PAYMENTS, Permission.UPLOAD_PAYMENTS);
            }
            case DOCUMENTS -> {
                return Set.of(Permission.UPLOAD_DOCUMENTS, Permission.VIEW_DOCUMENTS);
            }
            case SALES_MANAGER -> {
                return Set.of(Permission.VIEW_LEADS, Permission.CREATE_LEADS);
            }
            case MANAGER -> {
                return Set.of(Permission.CONVERT_LEAD_TO_CLIENT, Permission.VIEW_LEADS);
            }
            case MANAGER_CONSULTANT -> {
                return Set.of(Permission.VIEW_CLIENTS, Permission.UPDATE_CLIENTS);
            }
            case RECEPTION -> {
                return Set.of(Permission.VIEW_CLIENTS);
            }
            default -> {
                return Set.of();
            }
        }
    }
    // ✅ 5. Hodimni o‘chirish
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        // Super Adminni o‘chirish mumkin emas
        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new CustomException("Cannot delete Super Admin", HttpStatus.FORBIDDEN);
        }

        userRepository.delete(user);
    }
    // ✅ Soft delete (faqat active=false)
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
        userRepository.save(user);
    }

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

    // ✅ Archived list
    public List<UserResponse> getArchivedUsers() {
        return userRepository.findByArchivedTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ✅ Deleted list
    public List<UserResponse> getDeletedUsers() {
        return userRepository.findByDeletedTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
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
                .build();
    }
}
