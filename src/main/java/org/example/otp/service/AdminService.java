package org.example.otp.service;

import org.example.otp.dao.OtpConfigDao;
import org.example.otp.dao.OtpCodeDao;
import org.example.otp.dao.UserDao;
import org.example.otp.model.OtpConfig;
import org.example.otp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Сервис для административных операций
 * Предоставляет методы для управления настройками OTP, пользователями и их кодами
 */
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final OtpConfigDao configDao;
    private final UserDao userDao;
    private final OtpCodeDao codeDao;

    public AdminService(OtpConfigDao configDao, UserDao userDao, OtpCodeDao codeDao) {
        this.configDao = configDao;
        this.userDao = userDao;
        this.codeDao = codeDao;
    }

    public void updateOtpConfig(int length, int ttlSeconds) {
        OtpConfig cfg = new OtpConfig(1L, length, ttlSeconds);
        configDao.updateConfig(cfg);
        logger.info("OTP config updated: length={}, ttlSeconds={}", length, ttlSeconds);
    }

    public List<User> getAllUsersWithoutAdmins() {
        return userDao.findAllUsersWithoutAdmins();
    }

    public void deleteUserAndCodes(Long userId) {
        codeDao.deleteAllByUserId(userId);
        userDao.delete(userId);
        logger.info("Deleted user {} and their OTP codes", userId);
    }
}
