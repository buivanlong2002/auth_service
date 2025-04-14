package com.example.auth_service.service;

import com.example.auth_service.components.JwtTokenUtil;
import com.example.auth_service.dtos.request.LoginRequest;
import com.example.auth_service.dtos.request.RegisterRequest;
import com.example.auth_service.dtos.response.AuthResponse;
import com.example.auth_service.dtos.response.GeneralStatus;
import com.example.auth_service.dtos.response.RegisterResponse;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repositories.OtpTokenRepository;
import com.example.auth_service.repositories.RoleRepository;
import com.example.auth_service.repositories.UserRepository;
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
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


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

    public AuthResponse login(LoginRequest request) {
        GeneralStatus status;
        ExecutorService executor = Executors.newFixedThreadPool(10); // Tạo pool cho 10 luồng
        try {
            Callable<AuthResponse> task = () -> {
                // Tìm người dùng theo email
                User user = userRepository.findByEmail(request.getEmail()).orElse(null);

                if (user == null) {
                    return buildAuthResponse("01", "Email hoặc số điện thoại chưa được đăng ký");
                }
                // Kiểm tra mật khẩu
                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                    return buildAuthResponse("02", "Mật khẩu không chính xác");
                }
                // Tạo token
                String token = jwtTokenUtil.generateToken(user);
                return buildAuthResponse("00", "Đăng nhập thành công", token);
            };

            Future<AuthResponse> future = executor.submit(task);
            return future.get(); // Chờ kết quả từ tác vụ

        } catch (Exception ex) {
            ex.printStackTrace();
            return buildAuthResponse("99", "Đã xảy ra lỗi trong quá trình xử lý. Vui lòng thử lại sau.");
        } finally {
            executor.shutdown(); // Đảm bảo đóng executor khi kết thúc
        }
    }




    // đăng người dùng
    public RegisterResponse register(RegisterRequest request) {
        try {
            // 1. Kiểm tra email đã tồn tại chưa
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                GeneralStatus status = new GeneralStatus("01", true);
                status.setDisplayMessage("Email đã được sử dụng");
                status.setResponseTime(new Date());
                return new RegisterResponse(status);
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
//            user.setId(UUID.randomUUID().toString());
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword())); // mã hóa mật khẩu
            user.setRole(role);
            user.setActive(true);
            // 4. Lưu user
            userRepository.save(user);
            // 5. Trả về AuthResponse
            GeneralStatus status = new GeneralStatus("00", true);
            status.setDisplayMessage("Đăng ký thành công");
            status.setResponseTime(new Date());

            return new RegisterResponse(status);

        } catch (Exception ex) {
            ex.printStackTrace();
            GeneralStatus status = new GeneralStatus("99", true);
            status.setDisplayMessage("Đã xảy ra lỗi trong quá trình xử lý. Vui lòng thử lại sau.");
            status.setResponseTime(new Date());
            return new RegisterResponse(status);
        }
    }

    public void resetPassword(String email, String otp, String newPassword) {
        // Xác thực OTP
        if (!otpService.verifyOtp(email, otp)) {
            throw new RuntimeException("OTP không hợp lệ hoặc đã hết hạn!");
        }
        otpTokenRepository.deleteByEmail(email);

        // Lấy thông tin user
        User user = (User) userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

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

    private AuthResponse buildAuthResponse(String code, String displayMessage) {
        return buildAuthResponse(code, displayMessage, null);
    }

    private AuthResponse buildAuthResponse(String code, String displayMessage, String token) {
        GeneralStatus status = new GeneralStatus(code, true);
        status.setDisplayMessage(displayMessage);
        return new AuthResponse(status, token);
    }

}
