package com.crm.performance.entity;

import com.crm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_performance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "date"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SalesPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🧑‍💼 Hodim
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    // 📅 Sana (kunlik yozuv)
    private LocalDate date;

    // 🔹 Kunlik natijalar
    private int convertedCount;
    private int paidClientsCount;

    // 💰 Bonus ma’lumotlari
    private double bonusPerPayment;
    private double totalBonus;
    private boolean qualifiedForBonus;   // 3 tadan ko‘p = true

    // 🕐 Qo‘shimcha
    private LocalDateTime updatedAt;
}
