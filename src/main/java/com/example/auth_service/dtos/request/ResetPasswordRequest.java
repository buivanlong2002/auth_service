package com.example.auth_service.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ResetPasswordRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "OTP không được để trống")
    private String otp;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    private String newPassword;
}
