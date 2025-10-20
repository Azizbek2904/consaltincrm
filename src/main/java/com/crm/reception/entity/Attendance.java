package com.crm.reception.entity;
import com.crm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User user;
    private LocalDate date; // sana
    private LocalDateTime checkIn; // ishga kelgan vaqt
    private LocalDateTime checkOut; // ishni tugatgan vaqt
}
