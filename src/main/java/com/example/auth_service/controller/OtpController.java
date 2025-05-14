package com.example.auth_service.controller;

import com.example.auth_service.dtos.request.OtpSendRequest;
import com.example.auth_service.dtos.request.VerifyOtpRequest;
import com.example.auth_service.dtos.response.ApiResponse;
import com.example.auth_service.service.OtpService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    //  Gửi OTP
    @PostMapping("send")
    public ResponseEntity<ApiResponse<?>> sendOtp(@RequestBody OtpSendRequest emailRequest) throws MessagingException {
        return ResponseEntity.ok(otpService.generateOtp(emailRequest));
    }

    @PostMapping("verify")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest) throws MessagingException {
      boolean check = otpService.verifyOtp(verifyOtpRequest.getEmail(),verifyOtpRequest.getOtp());
      if (check) {
          return ResponseEntity.ok(ApiResponse.success("00","OTP hợp lệ",null));
      }else {
          return ResponseEntity.ok(ApiResponse.error("01","OTP đã hết hạn,vui lòng nhấn gửi lại OTP"));
      }

    }
}
