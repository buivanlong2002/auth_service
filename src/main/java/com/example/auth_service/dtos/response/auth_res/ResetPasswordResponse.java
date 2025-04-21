package com.example.auth_service.dtos.response.auth_res;

import com.example.auth_service.dtos.response.GeneralStatus;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponse {
    private GeneralStatus status;

    public static class UserAddResponse {
        private GeneralStatus status;
    }
}
