package com.example.auth_service.controller;


import com.example.auth_service.components.JwtTokenUtil;
import com.example.auth_service.dtos.request.LoginGoogleDTO;
import com.example.auth_service.dtos.request.LoginRequest;
import com.example.auth_service.dtos.request.RegisterRequest;
import com.example.auth_service.dtos.request.ResetPasswordRequest;
import com.example.auth_service.dtos.response.ApiResponse;
import com.example.auth_service.dtos.response.UserResponse;
import com.example.auth_service.entity.User;
import com.example.auth_service.service.AuthService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.security.Key;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request) throws Exception {
        return ResponseEntity.ok(authService.login(request)).getBody();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Validated @RequestBody RegisterRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors()
                    .stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .findFirst()
                    .orElse("Invalid input");

            ApiResponse<String> response = ApiResponse.error("VALIDATION_ERROR", errorMessage);
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(authService.register(request));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
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
            ApiResponse<UserResponse> user = authService.loginGoogle(loginGoogleDTO.getToken());
            // Trả về thông tin user hoặc token tùy bạn
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Xác thực thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/decode")
    public ResponseEntity<ApiResponse<?>> decodeToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");

        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Key key = jwtTokenUtil.getSigningKey();

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return ResponseEntity.ok(ApiResponse.success("Token is valid", claims));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("01", "Token không hợp lệ hoặc đã hết hạn: " + e.getMessage()));
        }
    }

}

