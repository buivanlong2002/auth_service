package com.example.auth_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LoginGoogleDTO {
    @NotBlank(message = "Token không được để trống")
    private String token;
}
