package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;

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

    @NotBlank(message = "User ID không được để trống")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotBlank(message = "Password hash không được để trống")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
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
