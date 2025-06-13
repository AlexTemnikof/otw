package org.example.otp.api.dto;

import org.example.otp.service.notification.NotificationChannel;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * DTO для запроса генерации одноразового кода подтверждения
 * Содержит информацию о пользователе, операции и канале доставки OTP кода
 */
public class GenerateOtpRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Operation ID is required")
    private String operationId;

    @NotNull(message = "Channel is required")
    @Pattern(regexp = "SMS|EMAIL|TELEGRAM|FILE", message = "Channel must be one of: SMS, EMAIL, TELEGRAM, FILE")
    private String channel;

    public GenerateOtpRequestDto() {
    }

    public GenerateOtpRequestDto(Long userId, String operationId, String channel) {
        this.userId = userId;
        this.operationId = operationId;
        this.channel = channel;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }


    public void validate() {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (operationId == null || operationId.isBlank()) {
            throw new IllegalArgumentException("Operation ID is required");
        }
        if (channel == null) {
            throw new IllegalArgumentException("Channel is required");
        }
        try {
            NotificationChannel.valueOf(channel);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Channel must be one of: SMS, EMAIL, TELEGRAM, FILE");
        }
    }
}
