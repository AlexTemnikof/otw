package org.example.otp.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Реализация сервиса уведомлений на основе электронной почты
 * Обрабатывает отправку OTP-кодов по электронной почте с использованием протокола SMTP
 */
public class EmailNotificationService implements NotificationService {
    // Логгер для этого класса
    private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationService.class);

    // Константы конфигурации
    private static final String CONFIG_FILE = "email.properties";
    private static final String SUBJECT = "Your OTP Code";
    private static final String MESSAGE_TEMPLATE = "Your one-time confirmation code is: %s";

    // Сессия электронной почты для отправки сообщений
    private final Session mailSession;
    // Адрес электронной почты отправителя
    private final String senderAddress;

    /**
     * Создает новый EmailNotificationService с конфигурацией из файла свойств
     */
    public EmailNotificationService() {
        // Загрузка конфигурации электронной почты
        Properties emailConfig = loadEmailConfiguration();
        this.senderAddress = emailConfig.getProperty("email.from");

        // Инициализация почтовой сессии с аутентификацией
        this.mailSession = Session.getInstance(emailConfig, createAuthenticator(emailConfig));
    }

    /**
     * Создает аутентификатор для SMTP-аутентификации
     * 
     * @param config свойства, содержащие имя пользователя и пароль
     * @return настроенный аутентификатор
     */
    private Authenticator createAuthenticator(final Properties config) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        config.getProperty("email.username"),
                        config.getProperty("email.password")
                );
            }
        };
    }

    /**
     * Загружает конфигурацию электронной почты из файла свойств
     * 
     * @return объект свойств с конфигурацией электронной почты
     * @throws RuntimeException если конфигурация не может быть загружена
     */
    private Properties loadEmailConfiguration() {
        try (InputStream configStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (configStream == null) {
                LOG.error("Configuration file not found: {}", CONFIG_FILE);
                throw new IllegalStateException(CONFIG_FILE + " not found in classpath");
            }

            Properties config = new Properties();
            config.load(configStream);
            LOG.debug("Email configuration loaded successfully");
            return config;
        } catch (IOException e) {
            LOG.error("Failed to load email configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Could not load email configuration", e);
        }
    }

    /**
     * Отправляет OTP-код на указанный адрес электронной почты
     * 
     * @param recipientEmail адрес электронной почты для отправки кода
     * @param code OTP-код для отправки
     * @throws RuntimeException если отправка не удалась
     */
    @Override
    public void sendCode(String recipientEmail, String code) {
        try {
            // Создание и настройка сообщения электронной почты
            Message message = createEmailMessage(recipientEmail, code);

            // Отправка сообщения
            Transport.send(message);
            LOG.info("OTP code successfully sent to {}", recipientEmail);
        } catch (MessagingException e) {
            LOG.error("Failed to send OTP email to {}: {}", recipientEmail, e.getMessage(), e);
            throw new RuntimeException("Email delivery failed", e);
        }
    }

    /**
     * Создает сообщение электронной почты с OTP-кодом
     * 
     * @param recipient адрес электронной почты получателя
     * @param otpCode OTP-код для включения в сообщение
     * @return настроенное сообщение электронной почты
     * @throws MessagingException если создание сообщения не удалось
     */
    private Message createEmailMessage(String recipient, String otpCode) throws MessagingException {
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(senderAddress));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject(SUBJECT);
        message.setText(String.format(MESSAGE_TEMPLATE, otpCode));
        return message;
    }
}
