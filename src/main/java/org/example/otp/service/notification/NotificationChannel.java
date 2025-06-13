package org.example.otp.service.notification;

/**
 * Перечисление доступных каналов отправки уведомлений
 * Определяет способы доставки OTP кодов пользователям
 */
public enum NotificationChannel {
    EMAIL,
    SMS,
    TELEGRAM,
    FILE
}
