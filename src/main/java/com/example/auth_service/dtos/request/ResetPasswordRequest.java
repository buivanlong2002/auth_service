package com.example.auth_service.dtos.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ResetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
}
