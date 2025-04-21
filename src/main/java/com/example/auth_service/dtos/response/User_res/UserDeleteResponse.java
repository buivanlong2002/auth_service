package com.example.auth_service.dtos.response.User_res;

import com.example.auth_service.dtos.response.GeneralStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserDeleteResponse {
    private GeneralStatus status;

}
