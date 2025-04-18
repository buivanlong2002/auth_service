package com.example.auth_service.service;

import com.example.auth_service.dtos.request.OtpSendRequest;
import com.example.auth_service.dtos.request.VerifyOtpRequest;
import com.example.auth_service.dtos.response.GeneralStatus;
import com.example.auth_service.dtos.response.OtpSendResponse;
import com.example.auth_service.dtos.response.RegisterResponse;
import com.example.auth_service.dtos.response.VerifyOtpResponse;
import com.example.auth_service.model.OtpToken;
import com.example.auth_service.model.User;
import com.example.auth_service.repositories.OtpTokenRepository;
import com.example.auth_service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {
    private final OtpTokenRepository otpTokenRepository;
    private final Random random = new Random();


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ResponseService responseService;

    public OtpService(OtpTokenRepository otpTokenRepository) {
        this.otpTokenRepository = otpTokenRepository;
    }

    //  Tạo OTP mới
    public OtpSendResponse generateOtp(OtpSendRequest emailRequest)  {
        String email = emailRequest.getEmail();
        if (email == null || email.trim().isEmpty()) {
            GeneralStatus status = new GeneralStatus("01", true);
            status.setDisplayMessage("Email không được để trống");
            status.setResponseTime(new Date());
            return new OtpSendResponse(status , null);
        }
        Optional<User> emailUser = userRepository.findByEmail(email);
        if (emailUser.isEmpty()) {
            GeneralStatus status = new GeneralStatus("02", true);
            status.setDisplayMessage("Email chưa được đăng ký");
            status.setResponseTime(new Date());
            return new OtpSendResponse(status , null);
        }
        otpTokenRepository.deleteByEmail(email); // Nếu chưa có method này, sẽ lỗi

        String otp = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

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

        // Gửi email
        emailService.sendEmail(email, subject, body);

        // Lưu lại OTP
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(email); // 👈 PHẢI có dòng này
        otpToken.setOtp(otp);
        otpToken.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpToken.setCreatedAt(LocalDateTime.now());
        otpToken.setUpdatedAt(LocalDateTime.now());


        try {
            otpTokenRepository.save(otpToken);
        }catch (Exception e){
            e.printStackTrace();
        }


        GeneralStatus status = new GeneralStatus("00", true);
        status.setDisplayMessage("otp đã gửi qua gmail của bạn");
        status.setResponseTime(new Date());
        return new OtpSendResponse(status ,otp);
    }


    //  Kiểm tra OTP
    public boolean verifyOtp(String email, String otp) {

        Optional<OtpToken> otpTokenOptional = otpTokenRepository.findByEmail(email);
        if (otpTokenOptional.isPresent()) {
            OtpToken otpToken = otpTokenOptional.get();
            if (!otpToken.isExpired() && otpToken.getOtp().equals(otp)) {
                return true;
            }
        }
        return false;
    }




}
