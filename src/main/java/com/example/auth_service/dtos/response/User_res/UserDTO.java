package com.example.auth_service.dtos.response.User_res;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private String address;
    private String phone;
    private boolean active;
    private long role;
}
