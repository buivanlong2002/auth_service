package com.example.auth_service.dtos.request.auth_req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String name;
    private String phone ;
    private String email;
    private String password;
    private Long roleId;
}
