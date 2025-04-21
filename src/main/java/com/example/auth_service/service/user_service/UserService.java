package com.example.auth_service.service.user_service;

import com.example.auth_service.dtos.request.user_req.UserAddRequest;
import com.example.auth_service.dtos.response.User_res.UserAddResponse;
import com.example.auth_service.dtos.response.User_res.UserDTO;
import com.example.auth_service.dtos.response.User_res.UserDeleteResponse;
import com.example.auth_service.dtos.response.User_res.UserGetAllResponse;
import com.example.auth_service.model.Address;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repositories.AddressRepository;
import com.example.auth_service.repositories.RoleRepository;
import com.example.auth_service.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserResponse userResponse;


    @Override
    public UserGetAllResponse getAllUsers() {
        List<User> users = userRepository.findAll();
//
            List<UserDTO> userDTOS = users.stream().map(user -> {
                List<Address> addresses = addressRepository.findByUserId(user.getId());
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setName(user.getName());
                userDTO.setEmail(user.getEmail());
                userDTO.setPhone(user.getPhone());
                if (!addresses.isEmpty()) {
                    userDTO.setAddress(addresses.get(0).getStreet());
                }else {
                    userDTO.setAddress(null);
                }
                userDTO.setActive(user.isActive());
                userDTO.setRole(user.getRole() != null ? user.getRole().getId() : 0);
                return userDTO;
            }).toList();
        return userResponse.buildUserGetAllResponse("00","Danh sách tất cả các người dùng",userDTOS);
    }


    public String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path destination = uploadDir.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }


    @Override
    public UserAddResponse addUser(UserAddRequest request) {
        try {
            // 1. Kiểm tra email đã tồn tại chưa
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return userResponse.buildAddUserResponse("01","Email đã được sử dụng");
            }
            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                return userResponse.buildAddUserResponse("02","Số điện thoại đã được sử dụng");
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
            user.setFacebookAccountId(0);
            user.setGoogleAccountId(0);
            user.setAvatarUrl("default.png");
            // 4. Lưu user
            userRepository.save(user);
            // 5. Trả về AuthResponse


        } catch (Exception ex) {
            ex.printStackTrace();
           return userResponse.buildAddUserResponse("99","Đã xảy ra lỗi trong quá trình xử lý. Vui lòng thử lại sau.");
        }
        return userResponse.buildAddUserResponse("00","Đăng ký thành công");
    }

    @Override
    public Object updateUser(Object user) {
        return null;
    }

    @Override
    public UserDeleteResponse deleteUserById(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return userResponse.buildDeleteUserResponse("01","Không tìm thấy ngươ dùng với id ");
        }
        User user1 = user.get();
        user1.setActive(false);
        userRepository.save(user1);
         return userResponse.buildDeleteUserResponse("00","Xóa người dùng thành công");
    }

    @Override
    public Object getUserById(String id) {
        return null;
    }



}
