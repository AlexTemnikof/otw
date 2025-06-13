package org.example.otp.service.notification;

/**
 * Интерфейс для сервисов отправки уведомлений
 * Определяет контракт для отправки OTP кодов через различные каналы связи
 */
public interface NotificationService {
    void sendCode(String recipient, String code);
}
