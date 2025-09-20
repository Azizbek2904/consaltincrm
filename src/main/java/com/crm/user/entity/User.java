package com.crm.user.entity;

import com.crm.user.entity.Permission;
import com.crm.user.entity.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
    private String department; // masalan: "Finance", "Sales", "Reception"

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = new HashSet<>();

    private boolean active = true;
    @Column(nullable = false)
    private boolean deleted = false;
    @Column(nullable = false)
    private boolean archived = false;


}
