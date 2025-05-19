package com.example.auth_service.service;

import com.example.auth_service.dtos.request.OtpSendRequest;
import com.example.auth_service.dtos.response.ApiResponse;
import com.example.auth_service.entity.OtpToken;
import com.example.auth_service.repositories.OtpTokenRepository;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.infrastructure.email_service.EmailService;
import com.example.auth_service.utils.StatusCode;
import com.example.auth_service.utils.MessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {
    private final OtpTokenRepository otpTokenRepository;
    private final Random random = new Random();

    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;

    public OtpService(OtpTokenRepository otpTokenRepository) {
        this.otpTokenRepository = otpTokenRepository;
    }

    public ApiResponse<?> generateOtp(OtpSendRequest emailRequest) {
        String email = emailRequest.getEmail();

        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error(StatusCode.EMAIL_EMPTY, MessageCode.EMAIL_EMPTY);
        }

        return userRepository.findByEmail(email)
                .map(user -> {
                    otpTokenRepository.deleteByEmail(email);
                    String otp = generateOtpCode();
                    LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);
                    sendOtpEmail(email, otp);
                    saveOtpToken(email, otp, expiryTime);
                    return ApiResponse.success(StatusCode.SUCCESS, MessageCode.OTP_SENT_SUCCESS, null);
                })
                .orElse(ApiResponse.error(StatusCode.EMAIL_NOT_REGISTERED, MessageCode.EMAIL_NOT_REGISTERED));
    }

    private String generateOtpCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    // Gửi email OTP với nội dung HTML
    private void sendOtpEmail(String email, String otp) {
        String subject = "Mã OTP của bạn";
        String body = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; border-radius: 10px; max-width: 500px; margin: auto; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #4CAF50; text-align: center;">Xác thực OTP</h2>
                    <p style="font-size: 16px;">Xin chào,</p>
                    <p style="font-size: 16px;">Mã OTP của bạn là:</p>
                    <p style="font-size: 24px; font-weight: bold; color: #e91e63; text-align: center;">%s</p>
                    <p style="font-size: 14px; color: #555;">Mã này có hiệu lực trong <b>5 phút</b>. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
                    <hr style="margin-top: 20px; margin-bottom: 20px;">
                    <p style="font-size: 12px; color: #999; text-align: center;">Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>
                </div>
                """, otp);

        emailService.sendEmail(email, subject, body);
    }

    // Lưu OTP token vào database
    private void saveOtpToken(String email, String otp, LocalDateTime expiryTime) {
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(email);
        otpToken.setOtp(otp);
        otpToken.setExpiryTime(expiryTime);
        otpToken.setCreatedAt(LocalDateTime.now());
        otpToken.setUpdatedAt(LocalDateTime.now());

        otpTokenRepository.save(otpToken);
    }

    public boolean verifyOtp(String email, String otp) {
        return otpTokenRepository.findByEmail(email)
                .filter(token -> !token.isExpired() && token.getOtp().equals(otp))
                .isPresent();
    }
}
