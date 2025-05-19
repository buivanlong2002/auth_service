package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "otp_tokens")
public class OtpToken {

    @Id
    @Column(updatable = false, nullable = false)
    private String id;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "OTP không được để trống")
    @Size(min = 6, max = 6, message = "OTP phải gồm 6 ký tự")
    @Column(nullable = false)
    private String otp;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public boolean isExpired() {
        return expiryTime == null || LocalDateTime.now().isAfter(expiryTime);
    }
}
