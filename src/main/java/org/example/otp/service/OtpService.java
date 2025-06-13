package org.example.otp.service;

import org.example.otp.dao.OtpCodeDao;
import org.example.otp.dao.OtpConfigDao;
import org.example.otp.dao.UserDao;
import org.example.otp.model.OtpCode;
import org.example.otp.model.OtpConfig;
import org.example.otp.model.OtpStatus;
import org.example.otp.model.User;
import org.example.otp.service.notification.NotificationChannel;
import org.example.otp.service.notification.NotificationService;
import org.example.otp.service.notification.NotificationServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления одноразовыми паролями (OTP)
 * Предоставляет функциональность для генерации, отправки и проверки OTP кодов
 */
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom random = new SecureRandom();

    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final UserDao userDao;
    private final NotificationServiceFactory notificationFactory;

    public OtpService(OtpCodeDao otpCodeDao,
                      OtpConfigDao otpConfigDao,
                      UserDao userDao,
                      NotificationServiceFactory notificationFactory) {
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
        this.userDao = userDao;
        this.notificationFactory = notificationFactory;
    }

    public String generateOtp(Long userId, String operationId) {
        OtpConfig config = otpConfigDao.getConfig();
        int length = config.getLength();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        String code = sb.toString();
        OtpCode otp = new OtpCode(
                null,
                userId,
                operationId,
                code,
                OtpStatus.ACTIVE,
                LocalDateTime.now()
        );
        otpCodeDao.save(otp);
        logger.info("Generated OTP {} for userId={}, operationId={}", code, userId, operationId);
        return code;
    }

    public OtpConfig getConfig() {
        return otpConfigDao.getConfig();
    }


    public void sendOtpToUser(Long userId, String operationId, NotificationChannel channel) {
        String code = generateOtp(userId, operationId);
        User user = userDao.findById(userId);
        if (user == null) {
            logger.error("sendOtpToUser: user not found, id={}", userId);
            throw new IllegalArgumentException("User not found");
        }

        String recipient = user.getUsername();
        NotificationService svc = notificationFactory.getService(channel);
        svc.sendCode(recipient, code);
        logger.info("Sent OTP code for userId={} via {}", userId, channel);
    }


    public boolean validateOtp(String inputCode) {
        OtpCode otp = otpCodeDao.findByCode(inputCode);
        if (otp == null) {
            logger.warn("validateOtp: code not found {}", inputCode);
            return false;
        }

        if (otp.getStatus() != OtpStatus.ACTIVE) {
            logger.warn("validateOtp: code {} is not active (status={})", inputCode, otp.getStatus());
            return false;
        }

        OtpConfig config = otpConfigDao.getConfig();
        LocalDateTime expiry = otp.getCreatedAt().plusSeconds(config.getTtlSeconds());
        if (LocalDateTime.now().isAfter(expiry)) {
            otpCodeDao.markAsExpiredOlderThan(Duration.ofSeconds(config.getTtlSeconds()));
            logger.warn("validateOtp: code {} expired at {}", inputCode, expiry);
            return false;
        }

        otpCodeDao.markAsUsed(otp.getId());
        logger.info("validateOtp: code {} validated and marked USED", inputCode);
        return true;
    }

    public void markExpiredOtps() {
        OtpConfig config = otpConfigDao.getConfig();
        Duration ttl = Duration.ofSeconds(config.getTtlSeconds());
        otpCodeDao.markAsExpiredOlderThan(ttl);
        logger.info("markExpiredOtps: expired codes older than {} seconds", config.getTtlSeconds());
    }
}
