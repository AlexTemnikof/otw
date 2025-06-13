package org.example.otp.model;

/**
 * Перечисление возможных статусов OTP кода
 * Определяет текущее состояние кода в системе
 */
public enum OtpStatus {
    ACTIVE,
    USED,
    EXPIRED
}
