package com.example.auth_service.service;

import com.example.auth_service.dtos.request.UserAddRequest;
import com.example.auth_service.dtos.response.ApiResponse;
import com.example.auth_service.dtos.response.UserResponse;
import com.example.auth_service.entity.Address;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.repositories.AddressRepository;
import com.example.auth_service.repositories.RoleRepository;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.utils.StatusCode;
import com.example.auth_service.utils.MessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userDTOS = users.stream().map(user -> {
            List<Address> addresses = addressRepository.findByUserId(user.getId());
            UserResponse userDTO = new UserResponse();
            userDTO.setId(user.getId());
            userDTO.setName(user.getName());
            userDTO.setEmail(user.getEmail());
            userDTO.setPhone(user.getPhone());
            userDTO.setAddress(addresses.isEmpty() ? null : addresses.get(0).getStreet());
            userDTO.setActive(user.isActive());
            userDTO.setRole(user.getRole() != null ? user.getRole().getId() : 0);
            return userDTO;
        }).toList();
        return ApiResponse.success(StatusCode.SUCCESS, MessageCode.USER_FOUND, userDTOS);
    }

    // Lưu trữ file ảnh, trả về tên file lưu trên server
    public String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID() + "_" + fileName;

        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path destination = uploadDir.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    @Transactional
    public ApiResponse<String> addUser(UserAddRequest request) {
        try {
            // Kiểm tra email đã tồn tại
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ApiResponse.error(StatusCode.EMAIL_ALREADY_EXISTS, MessageCode.EMAIL_ALREADY_EXISTS);
            }
            // Kiểm tra số điện thoại đã tồn tại
            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                return ApiResponse.error(StatusCode.PHONE_ALREADY_EXISTS, MessageCode.PHONE_ALREADY_EXISTS);
            }

            // Lấy role theo id hoặc mặc định USER
            Role role = request.getRoleId() != null
                    ? roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException(MessageCode.ROLE_NOT_FOUND))
                    : roleRepository.findByName("USER")
                    .orElseThrow(() -> new IllegalArgumentException(MessageCode.ROLE_NOT_FOUND));

            // Tạo user mới
            User user = new User();
            user.setPhone(request.getPhone());
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(role);
            user.setActive(true);
            user.setFacebookAccountId(0);
            user.setGoogleAccountId(0);
            user.setAvatarUrl("default.png");
            userRepository.save(user);

            return ApiResponse.success(StatusCode.SUCCESS, MessageCode.REGISTER_SUCCESS, null);

        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(StatusCode.ROLE_NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ApiResponse.error(StatusCode.INTERNAL_ERROR, MessageCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    public ApiResponse<String> updateUser(String userId, UserAddRequest request) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ApiResponse.error(StatusCode.USER_NOT_FOUND, MessageCode.USER_NOT_FOUND);
        }

        try {
            User user = optionalUser.get();

            user.setPhone(request.getPhone());
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            if (request.getRoleId() != null) {
                Role role = roleRepository.findById(request.getRoleId())
                        .orElseThrow(() -> new IllegalArgumentException(MessageCode.ROLE_NOT_FOUND));
                user.setRole(role);
            }
            userRepository.save(user);
            return ApiResponse.success(StatusCode.SUCCESS, "Cập nhật thành công", null);

        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(StatusCode.ROLE_NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ApiResponse.error(StatusCode.INTERNAL_ERROR, MessageCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    public ApiResponse<String> deleteUserById(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ApiResponse.error(StatusCode.USER_NOT_FOUND, MessageCode.USER_NOT_FOUND);
        }

        try {
            User user1 = user.get();
            user1.setActive(false);
            userRepository.save(user1);
            return ApiResponse.success(StatusCode.SUCCESS, "Xóa người dùng thành công", null);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ApiResponse.error(StatusCode.INTERNAL_ERROR, MessageCode.INTERNAL_ERROR);
        }
    }

    public ApiResponse<UserResponse> getUserById(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ApiResponse.error(StatusCode.USER_NOT_FOUND, MessageCode.USER_NOT_FOUND);
        }

        User foundUser = user.get();
        UserResponse userResponse = new UserResponse();
        userResponse.setId(foundUser.getId());
        userResponse.setName(foundUser.getName());
        userResponse.setEmail(foundUser.getEmail());
        userResponse.setPhone(foundUser.getPhone());
        userResponse.setActive(foundUser.isActive());
        userResponse.setRole(foundUser.getRole() != null ? foundUser.getRole().getId() : 0);

        return ApiResponse.success(StatusCode.SUCCESS, "Lấy thông tin người dùng thành công", userResponse);
    }
}
