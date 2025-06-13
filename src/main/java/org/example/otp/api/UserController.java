package org.example.otp.api;

import com.sun.net.httpserver.HttpExchange;
import org.example.otp.api.dto.GenerateOtpRequestDto;
import org.example.otp.api.dto.ValidateOtpRequestDto;
import org.example.otp.dao.OtpCodeDaoImpl;
import org.example.otp.dao.OtpConfigDaoImpl;
import org.example.otp.dao.UserDaoImpl;
import org.example.otp.model.User;
import org.example.otp.service.OtpService;
import org.example.otp.service.notification.NotificationChannel;
import org.example.otp.service.notification.NotificationServiceFactory;
import org.example.otp.util.JsonUtil;
import org.example.otp.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Контроллер для работы с одноразовыми паролями
 * Обрабатывает запросы на генерацию и проверку OTP кодов
 */
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final OtpService otpService = new OtpService(
            new OtpCodeDaoImpl(),
            new OtpConfigDaoImpl(),
            new UserDaoImpl(),
            new NotificationServiceFactory()
    );

    public void generateOtp(HttpExchange exchange) throws IOException {
        User user = (User) exchange.getAttribute("user");
        logger.info("Method: POST, Path: /otp/generate, UserId: {}", user != null ? user.getId() : "unknown");

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            HttpUtils.sendError(exchange, 415, "Content-Type must be application/json");
            return;
        }

        try {
            GenerateOtpRequestDto req = JsonUtil.fromJson(exchange.getRequestBody(), GenerateOtpRequestDto.class);
            req.validate();
            otpService.sendOtpToUser(req.getUserId(), req.getOperationId(),
                    NotificationChannel.valueOf(req.getChannel()));
            HttpUtils.sendEmptyResponse(exchange, 202);
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }

    public void validateOtp(HttpExchange exchange) throws IOException {
        User user = (User) exchange.getAttribute("user");
        logger.info("Method: POST, Path: /otp/validate, UserId: {}", user != null ? user.getId() : "unknown");

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            HttpUtils.sendError(exchange, 415, "Content-Type must be application/json");
            return;
        }

        try {
            ValidateOtpRequestDto req = JsonUtil.fromJson(exchange.getRequestBody(), ValidateOtpRequestDto.class);
            req.validate();
            boolean valid = otpService.validateOtp(req.getCode());
            if (valid) {
                HttpUtils.sendEmptyResponse(exchange, 200);
            } else {
                HttpUtils.sendError(exchange, 400, "Invalid or expired code");
            }
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }

}
