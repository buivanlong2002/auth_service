package com.example.auth_service.controller;

import com.example.auth_service.components.JwtTokenUtil;
import com.example.auth_service.dtos.request.LoginGoogleDTO;
import com.example.auth_service.dtos.request.LoginRequest;
import com.example.auth_service.dtos.request.RegisterRequest;
import com.example.auth_service.dtos.request.ResetPasswordRequest;
import com.example.auth_service.dtos.response.ApiResponse;
import com.example.auth_service.dtos.response.UserResponse;
import com.example.auth_service.service.AuthService;

import com.example.auth_service.utils.MessageCode;
import com.example.auth_service.utils.StatusCode;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request) throws Exception {
        ApiResponse<String> response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        ApiResponse<String> response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ApiResponse<String> response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-google")
    public ResponseEntity<ApiResponse<UserResponse>> loginGoogle(@Valid @RequestBody LoginGoogleDTO loginGoogleDTO) {
        ApiResponse<UserResponse> response = authService.loginGoogle(loginGoogleDTO.getToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/decode")
    public ResponseEntity<ApiResponse<Claims>> decodeToken(@Valid @RequestBody Map<String, String> payload) {
        String token = payload.get("token");

        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Key key = jwtTokenUtil.getSigningKey();

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return ResponseEntity.ok(ApiResponse.success(StatusCode.SUCCESS, MessageCode.OTP_VALID, claims));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(StatusCode.TOKEN_INVALID,  MessageCode.OTP_EXPIRED + e.getMessage()));
        }
    }
}
