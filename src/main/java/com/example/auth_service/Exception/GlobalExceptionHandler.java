package com.example.auth_service.Exception;

import com.example.auth_service.dtos.response.ApiResponse;
import com.example.auth_service.utils.StatusCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        logger.error("Lỗi hệ thống: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(StatusCode.INTERNAL_ERROR, "Đã xảy ra lỗi nội bộ"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> handleValidationError(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage()))
                .collect(Collectors.toList());

        logger.error("Lỗi dữ liệu không hợp lệ: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(StatusCode.VALIDATION_ERROR, "Dữ liệu không hợp lệ", errors));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(StatusCode.EMAIL_ALREADY_EXISTS, ex.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(StatusCode.USER_NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = resolveConstraintMessage(ex);
        logger.error("Lỗi ràng buộc dữ liệu: {}", message);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(StatusCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> Map.of(
                        "field", violation.getPropertyPath().toString(),
                        "message", violation.getMessage()))
                .collect(Collectors.toList());

        logger.error("Lỗi tham số không hợp lệ: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(StatusCode.VALIDATION_ERROR, "Tham số không hợp lệ", errors));
    }

    private String resolveConstraintMessage(DataIntegrityViolationException ex) {
        String message = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

        if (message.contains("users.email")) {
            return "Email đã tồn tại trong hệ thống.";
        }
        if (message.contains("users.phone")) {
            return "Số điện thoại đã tồn tại trong hệ thống.";
        }
        return "Dữ liệu không hợp lệ hoặc bị trùng.";
    }
}
