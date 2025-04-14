package com.example.auth_service.dtos.response;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendResponse {
    private GeneralStatus status;
    private String otp;
}
