package com.crm.user.repository;
import com.crm.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Hodimni faolligi bo‘yicha qidirish
    Optional<User> findByEmailAndActiveTrue(String email);

    List<User> findByArchivedTrue();

    List<User> findByDeletedTrue();
    // UserRepository.java
    List<User> findByActiveTrueAndDeletedFalseAndArchivedFalse();

    // ✅ Bloklangan (active=false) foydalanuvchilarni olish
    List<User> findByActiveFalseAndDeletedFalseAndArchivedFalse();


}