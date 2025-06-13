package org.example.otp.api.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * DTO для настройки параметров OTP кодов
 * Содержит информацию о длине кода и времени его действия
 */
public class ConfigRequestDto {
    @NotNull(message = "Length is required")
    @Min(value = 4, message = "Length must be at least 4")
    private Integer length;

    @NotNull(message = "TTL is required")
    @Min(value = 30, message = "TTL must be at least 30 seconds")
    private Integer ttlSeconds;

    public ConfigRequestDto() {
    }

    public ConfigRequestDto(Integer length, Integer ttlSeconds) {
        this.length = length;
        this.ttlSeconds = ttlSeconds;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Integer ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public void validate() {
        if (length == null) {
            throw new IllegalArgumentException("Length is required");
        }
        if (length < 4) {
            throw new IllegalArgumentException("Length must be at least 4");
        }
        if (ttlSeconds == null) {
            throw new IllegalArgumentException("TTL is required");
        }
        if (ttlSeconds < 30) {
            throw new IllegalArgumentException("TTL must be at least 30 seconds");
        }
    }
}
