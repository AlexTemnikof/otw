package org.example.otp.api.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * DTO для запроса валидации одноразового кода подтверждения
 * Используется при проверке корректности введенного пользователем OTP кода
 */
public class ValidateOtpRequestDto {
    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[0-9]{4,8}$", message = "Code must be a numeric string between 4 and 8 digits")
    private String code;

    public ValidateOtpRequestDto() {
    }

    public ValidateOtpRequestDto(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public void validate() {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code is required");
        }
        if (!code.matches("^[0-9]{4,8}$")) {
            throw new IllegalArgumentException("Code must be a numeric string between 4 and 8 digits");
        }
    }
}
