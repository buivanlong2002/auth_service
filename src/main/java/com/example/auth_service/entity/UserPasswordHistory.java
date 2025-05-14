package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "user_password_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPasswordHistory {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public UserPasswordHistory(String userId, String newPassword, LocalDateTime now) {
        this.userId = userId;
        this.passwordHash = newPassword;

    }
}
