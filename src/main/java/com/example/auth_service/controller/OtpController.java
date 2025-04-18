package com.example.auth_service.controller;

import com.example.auth_service.dtos.request.OtpSendRequest;
import com.example.auth_service.dtos.request.VerifyOtpRequest;
import com.example.auth_service.dtos.response.OtpSendResponse;
import com.example.auth_service.dtos.response.VerifyOtpResponse;
import com.example.auth_service.service.OtpService;
import com.example.auth_service.service.ResponseService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;
    private final ResponseService responseService;

    // 🔹 Gửi OTP
    @PostMapping("send")
    public ResponseEntity<OtpSendResponse> sendOtp(@RequestBody OtpSendRequest emailRequest) throws MessagingException {
        return ResponseEntity.ok(otpService.generateOtp(emailRequest));
    }

    @PostMapping("verify")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest) throws MessagingException {
      boolean check = otpService.verifyOtp(verifyOtpRequest.getEmail(),verifyOtpRequest.getOtp());
      if (check) {
          return ResponseEntity.ok(responseService.buildVerifyOtpResponse("00","OTP hợp lệ"));
      }else {
          return ResponseEntity.ok(responseService.buildVerifyOtpResponse("01","OTP đã hết hạn,vui lòng nhấn gửi lại OTP"));
      }

    }
}
