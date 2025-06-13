package org.example.otp.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Сервис отправки уведомлений в файл
 * Записывает OTP коды в указанный файл для тестирования и отладки
 */
public class FileNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FileNotificationService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    public void sendCode(String recipientPath, String code) {
        Path path = Paths.get(recipientPath);
        String entry = String.format("%s - OTP: %s%n",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                code);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, entry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.info("OTP code written to file {}", recipientPath);
        } catch (IOException e) {
            logger.error("Failed to write OTP to file {}", recipientPath, e);
            throw new RuntimeException("File write failed", e);
        }
    }
}
