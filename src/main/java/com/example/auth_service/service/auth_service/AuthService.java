package com.example.auth_service.service.auth_service;

import com.example.auth_service.Exception.GlobalExceptionHandler;
import com.example.auth_service.components.JwtTokenUtil;
import com.example.auth_service.dtos.request.auth_req.LoginRequest;
import com.example.auth_service.dtos.request.auth_req.RegisterRequest;
import com.example.auth_service.dtos.request.auth_req.ResetPasswordRequest;


import com.example.auth_service.dtos.response.auth_res.AuthResponse;
import com.example.auth_service.dtos.response.auth_res.RegisterResponse;
import com.example.auth_service.dtos.response.auth_res.ResetPasswordResponse;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repositories.OtpTokenRepository;
import com.example.auth_service.repositories.RoleRepository;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.service.otp_service.OtpService;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;


@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private OtpTokenRepository otpTokenRepository;
    @Autowired
    private OtpService otpService;
    @Autowired
    private AuthResponseService authResponseService;
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;



    public AuthResponse login(LoginRequest request) {
//        long startTime = System.currentTimeMillis();
        try {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user == null) {
                return authResponseService.buildAuthResponse("01", "Email hoặc số điện thoại chưa được đăng ký");
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return authResponseService.buildAuthResponse("02", "Mật khẩu không chính xác");
            }
            String token = jwtTokenUtil.generateToken(user);
            return authResponseService.buildAuthResponse("00", "Đăng nhập thành công", token);
        } catch (Exception ex) {
            ex.printStackTrace();
            return globalExceptionHandler.handleRuntimeException((RuntimeException) ex).getBody();
        }
    }

    // đăng người dùng
    public RegisterResponse register(RegisterRequest request) {
        try {
            // 1. Kiểm tra email đã tồn tại chưa
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return authResponseService.buildRegisterResponse("01", "Email đã được sử dụng");
            }
            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                return authResponseService.buildRegisterResponse("02", "Số diện thoại đã được sử dụng");
            }
//            // 2. Lấy Role (nếu roleId null thì set mặc định)
            Role role;
            if (request.getRoleId() != null) {
                role = roleRepository.findById(request.getRoleId())
                        .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));
            } else {
                role = roleRepository.findByName("USER")
                        .orElseThrow(() -> new RuntimeException("Vai trò mặc định USER không tồn tại"));
            }
            // 3. Tạo User mới
            User user = new User();
            user.setPhone(request.getPhone());
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(role);
            user.setActive(true);
            user.setAvatarUrl("default.png");
            // 4. Lưu user
            userRepository.save(user);
            // 5. Trả về AuthResponse
            return authResponseService.buildRegisterResponse("00", "Đăng ký thành công");

        } catch (Exception ex) {
            ex.printStackTrace();
            return authResponseService.buildRegisterResponse("99", "Đã xảy ra lỗi trong quá trình xử lý. Vui lòng thử lại sau.");
        }
    }

    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        // Xác thực OTP

        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            return authResponseService.buildResetPasswordResponse("01", "Otp đã hết hạn ");
        }
        otpTokenRepository.deleteByEmail(request.getEmail());

        // Lấy thông tin user
        User user = (User) userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return authResponseService.buildResetPasswordResponse("00", "Đổi mật khẩu thành công ");
    }

    public User loginGoogle(@NotBlank String token) throws Exception {
        // Tạo trình xác thực token Google
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList("583029729818-pjuun9f87kvpcoevf4icc8q5lme860j9.apps.googleusercontent.com")) // ← Thay bằng CLIENT_ID thật của bạn
                .build();

        // Xác thực token
        GoogleIdToken idToken = verifier.verify(token);
        if (idToken == null) {
            throw new RuntimeException("Token Google không hợp lệ");
        }

        // Lấy thông tin từ payload
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture"); // nếu cần
        String googleSub = payload.getSubject(); // ID unique của tài khoản Google

        Optional<User> optionalUser = userRepository.findByEmail(email);

        // Nếu user đã tồn tại, trả về luôn
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }

        // Nếu chưa có user → tạo mới
        Role defaultRole = roleRepository.findById(1L)
                .orElseThrow(() -> new Exception("Không tìm thấy vai trò mặc định"));

        // mã hóa mật khẩu fake
        String encodedPassword = passwordEncoder.encode("abcxyz");

        User newUser = User.builder()
                .email(email)
                .name(name)
                .password(encodedPassword) // hoặc có thể đặt "default" nếu cần (và encode nếu dùng spring security)
                .googleAccountId(1)
                .facebookAccountId(0)
                .active(true)
                .role(defaultRole)
                .build();

        userRepository.save(newUser);

        return newUser;
    }
}
