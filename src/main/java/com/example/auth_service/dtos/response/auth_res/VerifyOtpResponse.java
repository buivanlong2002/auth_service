package com.example.auth_service.dtos.response.auth_res;

import com.example.auth_service.dtos.response.GeneralStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class VerifyOtpResponse {
    private GeneralStatus status;


}
