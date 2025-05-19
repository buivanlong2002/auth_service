package com.example.auth_service.controller;

import com.example.auth_service.dtos.request.OtpSendRequest;
import com.example.auth_service.dtos.request.VerifyOtpRequest;
import com.example.auth_service.dtos.response.ApiResponse;
import com.example.auth_service.service.OtpService;

import com.example.auth_service.utils.MessageCode;
import com.example.auth_service.utils.StatusCode;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @PostMapping("send")
    public ResponseEntity<ApiResponse<?>> sendOtp(@Valid @RequestBody OtpSendRequest emailRequest) {
        return ResponseEntity.ok(otpService.generateOtp(emailRequest));
    }

    @PostMapping("verify")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        boolean check = otpService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());
        if (check) {
            return ResponseEntity.ok(ApiResponse.success(StatusCode.SUCCESS, MessageCode.OTP_VALID, null));
        } else {
            return ResponseEntity.ok(ApiResponse.error(StatusCode.TOKEN_INVALID, MessageCode.OTP_EXPIRED));
        }
    }
}
