package com.example.auth_service.dtos.response;


import lombok.*;

@Data // Lombok sẽ tự động tạo getter và setter cho các trường
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private GeneralStatus status;
    private String token;  // Token JWT hoặc null

}
