package com.h_a_s.patient_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "patients")
public class Patient {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Transient
    @Enumerated(EnumType.STRING)
    private Roles role;

    @Column(name = "contact", nullable = false)
    private String contact;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "active", nullable = false)
    private boolean active;

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
