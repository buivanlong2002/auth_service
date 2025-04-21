package com.example.auth_service.controller;

import com.example.auth_service.dtos.request.auth_req.OtpSendRequest;
import com.example.auth_service.dtos.request.auth_req.VerifyOtpRequest;
import com.example.auth_service.dtos.response.auth_res.OtpSendResponse;
import com.example.auth_service.dtos.response.auth_res.VerifyOtpResponse;
import com.example.auth_service.service.otp_service.OtpResponse;
import com.example.auth_service.service.otp_service.OtpService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;
    private final OtpResponse otpResponse;

    // 🔹 Gửi OTP
    @PostMapping("send")
    public ResponseEntity<OtpSendResponse> sendOtp(@RequestBody OtpSendRequest emailRequest) throws MessagingException {
        return ResponseEntity.ok(otpService.generateOtp(emailRequest));
    }

    @PostMapping("verify")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest) throws MessagingException {
      boolean check = otpService.verifyOtp(verifyOtpRequest.getEmail(),verifyOtpRequest.getOtp());
      if (check) {
          return ResponseEntity.ok(otpResponse.buildVerifyOtpResponse("00","OTP hợp lệ"));
      }else {
          return ResponseEntity.ok(otpResponse.buildVerifyOtpResponse("01","OTP đã hết hạn,vui lòng nhấn gửi lại OTP"));
      }

    }
}
