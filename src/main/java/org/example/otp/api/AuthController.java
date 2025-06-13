package org.example.otp.api;

import com.sun.net.httpserver.HttpExchange;
import org.example.otp.api.dto.LoginRequestDto;
import org.example.otp.api.dto.RegisterRequestDto;
import org.example.otp.dao.UserDaoImpl;
import org.example.otp.model.UserRole;
import org.example.otp.service.UserService;
import org.example.otp.util.JsonUtil;
import org.example.otp.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Контроллер для обработки запросов аутентификации
 * Предоставляет методы для регистрации и входа пользователей в систему
 */
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService = new UserService(new UserDaoImpl());

    public void handleRegister(HttpExchange exchange) throws IOException {
        logger.info("Method: POST, Path: /register, UserId: anonymous");

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
            RegisterRequestDto req = JsonUtil.fromJson(exchange.getRequestBody(), RegisterRequestDto.class);
            req.validate();

            if ("ADMIN".equals(req.getRole()) && userService.adminExists()) {
                HttpUtils.sendError(exchange, 409, "Admin already exists");
                return;
            }

            userService.register(req.getUsername(), req.getPassword(), UserRole.valueOf(req.getRole()));
            HttpUtils.sendEmptyResponse(exchange, 201);
        } catch (IllegalArgumentException | IllegalStateException e) {
            HttpUtils.sendError(exchange, 409, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
        logger.info("Method: POST, Path: /login, UserId: anonymous");

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
            LoginRequestDto req = JsonUtil.fromJson(exchange.getRequestBody(), LoginRequestDto.class);
            req.validate();
            String token = userService.login(req.getUsername(), req.getPassword());
            if (token == null) {
                HttpUtils.sendError(exchange, 401, "Unauthorized");
                return;
            }
            String json = JsonUtil.toJson(Map.of("token", token));
            HttpUtils.sendJsonResponse(exchange, 200, json);
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 401, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }

}
