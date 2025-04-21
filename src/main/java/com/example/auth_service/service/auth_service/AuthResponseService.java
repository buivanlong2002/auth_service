package com.example.auth_service.service.auth_service;

import com.example.auth_service.dtos.response.GeneralStatus;
import com.example.auth_service.dtos.response.auth_res.AuthResponse;
import com.example.auth_service.dtos.response.auth_res.RegisterResponse;
import com.example.auth_service.dtos.response.auth_res.ResetPasswordResponse;
import com.example.auth_service.dtos.response.auth_res.VerifyOtpResponse;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthResponseService {
    public ResetPasswordResponse buildResetPasswordResponse(String code, String displayMessage) {
        GeneralStatus status = new GeneralStatus(code, true);
        status.setResponseTime(new Date());
        status.setDisplayMessage(displayMessage);
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
    public RegisterResponse buildRegisterResponse(String code, String displayMessage) {
        GeneralStatus status = new GeneralStatus(code, true);
        status.setDisplayMessage(displayMessage);
        status.setResponseTime(new Date());
        return new RegisterResponse(status);
    }

}
