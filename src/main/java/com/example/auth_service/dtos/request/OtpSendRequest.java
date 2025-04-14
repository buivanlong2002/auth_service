package com.example.auth_service.dtos.request;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendRequest {
    private String email ;
}
