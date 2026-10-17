package com.auth_service.auth_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class authModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private int password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private roles role;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "is_password_changed", nullable = false)
    private boolean is_password_changed;

    @Column(name = "last_login_at")
    private LocalDateTime last_login_at;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @PrePersist
    protected void onCreate () {
        created_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate () {
        updated_at = LocalDateTime.now();
    }
}
