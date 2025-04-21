package com.example.auth_service.service.user_service;

import com.example.auth_service.dtos.response.GeneralStatus;
import com.example.auth_service.dtos.response.User_res.UserAddResponse;
import com.example.auth_service.dtos.response.User_res.UserDTO;
import com.example.auth_service.dtos.response.User_res.UserDeleteResponse;
import com.example.auth_service.dtos.response.User_res.UserGetAllResponse;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class UserResponse {
    public UserGetAllResponse buildUserGetAllResponse(String code, String displayMessage , List<UserDTO> userDTOList) {
        GeneralStatus status = new GeneralStatus(code, true);
        status.setDisplayMessage(displayMessage);
        status.setResponseTime(new Date());
        return new UserGetAllResponse(status ,userDTOList);
    }
    public UserDeleteResponse buildDeleteUserResponse(String code, String displayMessage ) {
        GeneralStatus status = new GeneralStatus(code, true);
        status.setDisplayMessage(displayMessage);
        status.setResponseTime(new Date());
        return new UserDeleteResponse(status);
    }
    public UserAddResponse buildAddUserResponse(String code, String displayMessage ) {
        GeneralStatus status = new GeneralStatus(code, true);
        status.setDisplayMessage(displayMessage);
        status.setResponseTime(new Date());
        return new UserAddResponse(status);
    }
}
