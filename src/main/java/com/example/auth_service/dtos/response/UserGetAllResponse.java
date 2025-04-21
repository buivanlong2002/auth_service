package com.example.auth_service.dtos.response;

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
