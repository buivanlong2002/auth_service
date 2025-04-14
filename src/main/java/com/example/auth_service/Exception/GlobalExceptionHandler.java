package com.example.auth_service.Exception;


import com.example.auth_service.dtos.response.AuthResponse;
import com.example.auth_service.dtos.response.GeneralStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Exception handler cho RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AuthResponse> handleRuntimeException(RuntimeException ex) {
        // Tạo một đối tượng GeneralStatus với code "99" cho lỗi không xác định
        GeneralStatus status = new GeneralStatus("99", false);
        status.setMessage(ex.getMessage()); // Đặt message từ exception
        status.setDisplayMessage("Đã xảy ra lỗi. Vui lòng thử lại."); // Đặt message hiển thị cho người dùng
        status.setResponseTime(new java.util.Date()); // Đặt thời gian phản hồi cho lỗi

        // Trả về response với status và token là null (hoặc có thể thay đổi theo nhu cầu)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(status, null));
    }
}
