package com.example.auth_service.service.user_service;

import com.example.auth_service.dtos.request.user_req.UserAddRequest;
import com.example.auth_service.dtos.response.User_res.UserAddResponse;

public interface IUserService<E> {
    UserAddResponse addUser(UserAddRequest request);

    E updateUser(E e);

    E deleteUserById(String id);

    E getUserById(String id);

    E getAllUsers();

}
