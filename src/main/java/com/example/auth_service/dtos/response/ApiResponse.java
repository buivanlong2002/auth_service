package com.example.auth_service.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "General status information")
    private GeneralStatus status;

    @Schema(description = "Payload data returned by the API")
    private T data;

    // Response thành công, cho phép chỉ truyền data và thông báo hiển thị
    public static <T> ApiResponse<T> success(String displayMessage, T data) {
        GeneralStatus status = new GeneralStatus("00", true);
        status.setDisplayMessage(displayMessage);
        return new ApiResponse<>(status, data);
    }

    // Response thành công với code cụ thể
    public static <T> ApiResponse<T> success(String code, String displayMessage, T data) {
        GeneralStatus status = new GeneralStatus(code, true);
        status.setDisplayMessage(displayMessage);
        return new ApiResponse<>(status, data);
    }

    // Response lỗi, cho phép custom code + message
    public static <T> ApiResponse<T> error(String code, String displayMessage) {
        GeneralStatus status = new GeneralStatus(code, false);
        status.setDisplayMessage(displayMessage);
        return new ApiResponse<>(status, null);
    }
}
