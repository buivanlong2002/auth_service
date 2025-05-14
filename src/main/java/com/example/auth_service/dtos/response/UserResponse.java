package com.example.auth_service.dtos.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String address;
    private String phone;
    private boolean active;
    private long role;
}
