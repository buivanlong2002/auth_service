package com.example.auth_service.service;

import com.example.auth_service.dtos.response.UserDTO;
import com.example.auth_service.dtos.response.UserGetAllResponse;
import com.example.auth_service.model.Address;
import com.example.auth_service.model.User;
import com.example.auth_service.repositories.AddressRepository;
import com.example.auth_service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private AddressRepository addressRepository;

    public UserGetAllResponse getAllUsers() {

            List<User> users = userRepository.findAll();

            List<UserDTO> userDTOS = users.stream().map(user -> {
                List<Address> addresses = addressRepository.findByUserId(user.getId());
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setName(user.getName());
                userDTO.setEmail(user.getEmail());
                userDTO.setPhone(user.getPhone());

                userDTO.setAddressList(addresses);
                userDTO.setRole(user.getRole() != null ? user.getRole().getId() : 0);
                return userDTO;
            }).toList();
        return responseService.buildUserGetAllResponse("00","Danh sách tất cả các người dùng",userDTOS);
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



}
