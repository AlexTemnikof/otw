package org.example.otp.service.notification;

import org.example.otp.service.notification.NotificationChannel;
import org.example.otp.service.notification.NotificationService;
import org.example.otp.service.notification.EmailNotificationService;
import org.example.otp.service.notification.SmsNotificationService;
import org.example.otp.service.notification.TelegramNotificationService;
import org.example.otp.service.notification.FileNotificationService;

/**
 * Фабрика для создания сервисов отправки уведомлений
 * Предоставляет соответствующую реализацию NotificationService в зависимости от выбранного канала
 */
public class NotificationServiceFactory {


    public NotificationService getService(NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                return new EmailNotificationService();
            case SMS:
                return new SmsNotificationService();
            case TELEGRAM:
                return new TelegramNotificationService();
            case FILE:
                return new FileNotificationService();
            default:
                throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
    }
}
