package com.example.auth_service.controller;

import com.example.auth_service.dtos.request.OtpSendRequest;
import com.example.auth_service.dtos.response.OtpSendResponse;
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

    // 🔹 Gửi OTP
    @PostMapping("send")
    public ResponseEntity<OtpSendResponse> sendOtp(@RequestBody OtpSendRequest emailRequest) throws MessagingException {
        return ResponseEntity.ok(otpService.generateOtp(emailRequest));
    }

    @PostMapping("verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = otpService.verifyOtp(email, otp);
        return isValid ? ResponseEntity.ok("OTP hợp lệ!") : ResponseEntity.badRequest().body("OTP không hợp lệ!");
    }
}
