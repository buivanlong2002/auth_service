package com.example.auth_service.mapper;

import com.example.auth_service.dtos.request.RegisterRequest;
import com.example.auth_service.dtos.response.UserResponse;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", expression = "java(encoder.encode(request.getPassword()))")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "avatarUrl", constant = "default.png")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "name", source = "request.name")
    User toEntity(RegisterRequest request, Role role, PasswordEncoder encoder);

    public default UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(Long.parseLong(user.getRole().getName()))
                .build();
    }
}
