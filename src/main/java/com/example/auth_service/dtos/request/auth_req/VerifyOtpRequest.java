package com.example.auth_service.dtos.request.auth_req;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class VerifyOtpRequest {
    private String email;
    private String otp;
}
