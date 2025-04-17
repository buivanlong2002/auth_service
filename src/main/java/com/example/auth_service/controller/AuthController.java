package com.example.auth_service.controller;



import com.example.auth_service.dtos.request.LoginGoogleDTO;
import com.example.auth_service.dtos.request.LoginRequest;
import com.example.auth_service.dtos.request.RegisterRequest;
import com.example.auth_service.dtos.request.ResetPasswordRequest;
import com.example.auth_service.dtos.response.AuthResponse;
import com.example.auth_service.dtos.response.RegisterResponse;
import com.example.auth_service.dtos.response.ResetPasswordResponse;
import com.example.auth_service.model.User;
import com.example.auth_service.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")

public class AuthController {
     @Autowired
    private  AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    // xử lý login gg
    @PostMapping("/login-google")
    public ResponseEntity<?> loginGoogle(@Valid @RequestBody LoginGoogleDTO loginGoogleDTO,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            // Gọi service xử lý đăng nhập Google
            User user = authService.loginGoogle(loginGoogleDTO.getToken());

            // Trả về thông tin user hoặc token tùy bạn
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Xác thực thất bại: " + e.getMessage());
        }
    }
}

