package com.example.auth_service.dtos.request.auth_req;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Login request")
public class LoginRequest {
    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @Schema(description = "Password", example = "123456")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
