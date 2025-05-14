package com.example.auth_service.entity;

import java.util.ArrayList;
import java.util.List;

public class ValidatePassword {
    public static void validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password.length() < 8) {
            errors.add("có ít nhất 8 ký tự.");
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.add("có ít nhất một chữ cái viết hoa.");
        }

        if (!password.matches(".*[a-z].*")) {
            errors.add(" có ít nhất một chữ cái viết thường.");
        }

        if (!password.matches(".*\\d.*")) {
            errors.add("có ít nhất một chữ số.");
        }

        if (!password.matches(".*[@$!%*?&].*")) {
            errors.add("có ít nhất một ký tự đặc biệt (@$!%*?&).");
        }

        if (!errors.isEmpty()) {
            String fullMessage = "Mật khẩu của bạn phải thỏa mãn các điều kiện sau:" + String.join("", errors);
            throw new IllegalArgumentException(fullMessage);
        }
    }
}
