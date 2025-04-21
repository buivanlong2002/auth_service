package com.example.auth_service.dtos.response.User_res;

import com.example.auth_service.dtos.response.GeneralStatus;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class UserGetAllResponse {
    private GeneralStatus status;
    private List<UserDTO> userDTOList;
}
