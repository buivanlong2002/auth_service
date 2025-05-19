package com.example.auth_service.entity;

import com.example.auth_service.Exception.InvalidPasswordException;

import java.util.ArrayList;
import java.util.List;

public class ValidatePassword {

    public static void validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.trim().isEmpty()) {
            throw new InvalidPasswordException("Mật khẩu không được để trống.");
        }

        if (password.length() < 8) {
            errors.add("• Ít nhất 8 ký tự.");
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.add("• Ít nhất một chữ cái viết hoa.");
        }

        if (!password.matches(".*[a-z].*")) {
            errors.add("• Ít nhất một chữ cái viết thường.");
        }

        if (!password.matches(".*\\d.*")) {
            errors.add("• Ít nhất một chữ số.");
        }

        if (!password.matches(".*[@$!%*?&].*")) {
            errors.add("• Ít nhất một ký tự đặc biệt (@$!%*?&).");
        }

        if (!errors.isEmpty()) {
            String fullMessage = "Mật khẩu của bạn phải thỏa mãn các điều kiện sau:\n" + String.join("\n", errors);
            throw new InvalidPasswordException(fullMessage);
        }
    }
}

