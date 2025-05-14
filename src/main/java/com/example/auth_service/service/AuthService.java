package com.example.auth_service.service;

import com.example.auth_service.dtos.response.ApiResponse;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.utils.Constants;
import com.example.auth_service.components.JwtTokenUtil;
import com.example.auth_service.dtos.request.LoginRequest;
import com.example.auth_service.dtos.request.RegisterRequest;
import com.example.auth_service.dtos.request.ResetPasswordRequest;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.entity.UserPasswordHistory;
import com.example.auth_service.entity.ValidatePassword;
import com.example.auth_service.repositories.RoleRepository;
import com.example.auth_service.repositories.UserPasswordHistoryRepository;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.dtos.response.UserResponse;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserPasswordHistoryRepository userPasswordHistoryRepository;

    @Value("${google.client-id}")
    private String googleClientId;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public ApiResponse<String> login(LoginRequest request) throws Exception {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error("02", Constants.ErrorCode.INVALID_PASSWORD);
        }
        String token = jwtTokenUtil.generateToken(user);
        return ApiResponse.success("00", Constants.ErrorCode.LOGIN_SUCCESS, token);
    }

    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ApiResponse.error("01", "Email đã được sử dụng");
            }
            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                return ApiResponse.error("02", "Số diện thoại đã được sử dụng");
            }
            ValidatePassword.validatePassword(request.getPassword());
            Role role = request.getRoleId() != null
                    ? roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"))
                    : roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Vai trò mặc định USER không tồn tại"));
            User user = userMapper.toEntity(request, role, passwordEncoder);
            userRepository.save(user);
            return ApiResponse.success("00", "Đăng ký thành công", null);
        } catch (Exception ex) {
            logger.error("Lỗi khi đăng ký người dùng: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi hệ thống khi đăng ký người dùng");
        }
    }

    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        ValidatePassword.validatePassword(request.getNewPassword());
        List<UserPasswordHistory> historyList = userPasswordHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (UserPasswordHistory history : historyList) {
            if (passwordEncoder.matches(request.getNewPassword(), history.getPasswordHash())) {
                return ApiResponse.error("02", "Vui lòng không trùng mật khẩu cũ");
            }
        }
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        if (historyList.size() > 4) {
            List<UserPasswordHistory> toDelete = historyList.subList(4, historyList.size());
            userPasswordHistoryRepository.deleteAll(toDelete);
        }
        UserPasswordHistory history = new UserPasswordHistory(user.getId(), encodedPassword, LocalDateTime.now());
        userPasswordHistoryRepository.save(history);
        userRepository.save(user);
        return ApiResponse.success("00", "Đổi mật khẩu thành công", null);
    }

    @Transactional
    public ApiResponse<UserResponse> loginGoogle(@NotBlank String token) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        GoogleIdToken idToken = verifier.verify(token);
        if (idToken == null) {
            throw new RuntimeException("Token Google không hợp lệ");
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò mặc định"));

            String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

            user = User.builder()
                    .email(email)
                    .phone("0000000000")
                    .name(name)
                    .password(randomPassword)
                    .googleAccountId(1)
                    .facebookAccountId(0)
                    .active(true)
                    .role(defaultRole)
                    .build();

            userRepository.save(user);
        }
        return ApiResponse.success("00", "Đăng nhập Google thành công", userMapper.toResponse(user));
    }
}
