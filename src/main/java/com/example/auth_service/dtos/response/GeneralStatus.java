package com.example.auth_service.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data // Lombok sẽ tự động tạo getter và setter cho các trường
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "General status information for API responses")
public class GeneralStatus implements Serializable {

    @Schema(description = "Status code", example = "00")
    private String code;

    @JsonProperty("message")
    @Schema(description = "Message associated with the status code", example = "Success")
    private String message;

    @JsonProperty("responseTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "Response time in ISO 8601 format", example = "2023-10-01T12:00:00Z")
    private Date responseTime = new Date();

    @JsonProperty("displayMessage")
    @Schema(description = "Message to be displayed to the user", example = "Operation completed successfully")
    private String displayMessage;

    // Constructor can be used for setting code and message implicitly
    public GeneralStatus(String code, boolean setMessageImplicitly) {
        setCode(code, setMessageImplicitly);
        this.responseTime = new Date();  // Set the current response time
    }

    // Set code and auto set message based on the code
    public void setCode(String code, boolean setMessageImplicitly) {
        this.code = code;
        if (setMessageImplicitly) {
            this.message = code.equals("00") ? "Success" : "Unknown Error";
        }
        this.displayMessage = this.message;
    }

}
