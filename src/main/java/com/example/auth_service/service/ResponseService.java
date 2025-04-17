package com.example.auth_service.service;

import com.example.auth_service.dtos.request.ResetPasswordRequest;
import com.example.auth_service.dtos.response.AuthResponse;
import com.example.auth_service.dtos.response.GeneralStatus;
import com.example.auth_service.dtos.response.ResetPasswordResponse;
import org.springframework.stereotype.Service;

import java.util.Date;
@Service
public class ResponseService {
    public ResetPasswordResponse buildResetPasswordResponse(String code, String displayMessage) {

        GeneralStatus status = new GeneralStatus(code, displayMessage);
        status.setResponseTime(new Date());
        return new ResetPasswordResponse(status);
    }
    public AuthResponse buildAuthResponse(String code, String displayMessage) {
        return buildAuthResponse(code, displayMessage, null);
    }


    public AuthResponse buildAuthResponse(String code, String displayMessage, String token) {
        GeneralStatus status = new GeneralStatus(code, true);
        status.setDisplayMessage(displayMessage);
        return new AuthResponse(status, token);
    }
}
