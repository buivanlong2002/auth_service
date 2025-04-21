package com.example.auth_service.service.otp_service;

import com.example.auth_service.dtos.response.GeneralStatus;
import com.example.auth_service.dtos.response.auth_res.VerifyOtpResponse;
import org.springframework.stereotype.Service;

import java.util.Date;
@Service
public class OtpResponse {
    public VerifyOtpResponse buildVerifyOtpResponse(String code, String displayMessage) {
        GeneralStatus status = new GeneralStatus(code, true);
        status.setDisplayMessage(displayMessage);
        status.setResponseTime(new Date());
        return new VerifyOtpResponse(status);
    }
}
