package com.example.auth_service.service;

import com.example.auth_service.components.JwtTokenUtil;
import com.example.auth_service.dtos.request.LoginRequest;
import com.example.auth_service.dtos.request.RegisterRequest;
import com.example.auth_service.dtos.request.ResetPasswordRequest;
import com.example.auth_service.dtos.response.ApiResponse;
import com.example.auth_service.dtos.response.UserResponse;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.entity.UserPasswordHistory;
import com.example.auth_service.entity.ValidatePassword;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.repositories.RoleRepository;
import com.example.auth_service.repositories.UserPasswordHistoryRepository;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.utils.StatusCode;
import com.example.auth_service.utils.MessageCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.validation.constraints.NotBlank;
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

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserMapper userMapper;
    @Autowired private JwtTokenUtil jwtTokenUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserPasswordHistoryRepository userPasswordHistoryRepository;

    @Value("${google.client-id}")
    private String googleClientId;

    public ApiResponse<String> login(LoginRequest request) throws Exception {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(MessageCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error(StatusCode.INVALID_PASSWORD, MessageCode.INVALID_PASSWORD);
        }
        String token = jwtTokenUtil.generateToken(user);
        return ApiResponse.success(StatusCode.SUCCESS, MessageCode.LOGIN_SUCCESS, token);
    }

    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {
        boolean isEmailAlreadyRegistered = userRepository.existsByEmail(request.getEmail());
        if (isEmailAlreadyRegistered) {
            return ApiResponse.error(StatusCode.EMAIL_ALREADY_EXISTS, MessageCode.EMAIL_ALREADY_EXISTS);
        }
        boolean isPhoneAlreadyRegistered = userRepository.findByPhone(request.getPhone()).isPresent();
        if (isPhoneAlreadyRegistered) {
            return ApiResponse.error(StatusCode.PHONE_ALREADY_EXISTS, MessageCode.PHONE_ALREADY_EXISTS);
        }
            ValidatePassword.validatePassword(request.getPassword());
        Role role = Optional.ofNullable(request.getRoleId())
                .flatMap(roleRepository::findById)
                .or(() -> roleRepository.findByName("USER"))
                .orElse(null);
        if (role == null) {
            return ApiResponse.error(StatusCode.ROLE_NOT_FOUND, MessageCode.ROLE_NOT_FOUND);
        }
        User user = userMapper.toEntity(request, role, passwordEncoder);
        userRepository.save(user);

        return ApiResponse.success(StatusCode.SUCCESS, MessageCode.REGISTER_SUCCESS, null);
    }


    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        if (user == null) {
            return ApiResponse.error(StatusCode.USER_NOT_FOUND, MessageCode.USER_NOT_FOUND);
        }
        ValidatePassword.validatePassword(request.getNewPassword());
        List<UserPasswordHistory> historyList =
                userPasswordHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (UserPasswordHistory history : historyList) {
            if (passwordEncoder.matches(request.getNewPassword(), history.getPasswordHash())) {
                return ApiResponse.error(StatusCode.DUPLICATE_OLD_PASSWORD, MessageCode.DUPLICATE_OLD_PASSWORD);
            }
        }
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        if (historyList.size() > 4) {
            List<UserPasswordHistory> toDelete = historyList.subList(4, historyList.size());
            userPasswordHistoryRepository.deleteAll(toDelete);
        }
        userPasswordHistoryRepository.save(
                new UserPasswordHistory(user.getId(), encodedPassword, LocalDateTime.now())
        );
        userRepository.save(user);
        return ApiResponse.success(StatusCode.SUCCESS, MessageCode.PASSWORD_CHANGED, null);
    }

    @Transactional
    public ApiResponse<UserResponse> loginGoogle(@NotBlank String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                return ApiResponse.error(StatusCode.TOKEN_INVALID, MessageCode.TOKEN_INVALID);
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                Role defaultRole = roleRepository.findByName("USER")
                        .orElseThrow(() -> new RuntimeException(MessageCode.ROLE_NOT_FOUND));

                return userRepository.save(User.builder()
                        .email(email)
                        .phone("0000000000")
                        .name(name)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .googleAccountId(1)
                        .facebookAccountId(0)
                        .active(true)
                        .role(defaultRole)
                        .build());
            });
            return ApiResponse.success(StatusCode.SUCCESS, MessageCode.GOOGLE_LOGIN_SUCCESS, userMapper.toResponse(user));
        } catch (Exception e) {
            return ApiResponse.error(StatusCode.TOKEN_INVALID, MessageCode.TOKEN_INVALID);
        }
    }
}
