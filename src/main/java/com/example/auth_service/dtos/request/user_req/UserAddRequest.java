package com.example.auth_service.dtos.request.user_req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserAddRequest {
    private String name;
    private String phone ;
    private String email;
    private String password;
    private Long roleId;
}
