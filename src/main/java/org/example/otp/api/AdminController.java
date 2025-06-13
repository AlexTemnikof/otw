package org.example.otp.api;

import com.sun.net.httpserver.HttpExchange;
import org.example.otp.api.dto.ConfigRequestDto;
import org.example.otp.dao.OtpCodeDaoImpl;
import org.example.otp.dao.OtpConfigDaoImpl;
import org.example.otp.dao.UserDaoImpl;
import org.example.otp.model.User;
import org.example.otp.service.AdminService;
import org.example.otp.util.JsonUtil;
import org.example.otp.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Обработчик конечных точек управления для административных операций
 * Обрабатывает HTTP-запросы для управления конфигурацией OTP, администрирования пользователей и других административных задач
 */
public class AdminController {
    // Экземпляр логгера для этого класса
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    // Зависимость сервисного слоя для бизнес-логики
    private final AdminService managementService;

    /**
     * Создает новый AdminController с необходимыми зависимостями
     */
    public AdminController() {
        this.managementService = new AdminService(new OtpConfigDaoImpl(), new UserDaoImpl(), new OtpCodeDaoImpl());
    }

    /**
     * Обновляет настройки конфигурации OTP
     *
     * @param exchange HTTP обмен, содержащий запрос
     * @throws IOException если происходит ошибка ввода-вывода
     */
    public void updateOtpConfig(HttpExchange exchange) throws IOException {
        User user = (User) exchange.getAttribute("user");
        LOG.info("Request received - Method: PATCH, Path: /admin/config, User: {}", user != null ? user.getId() : "anonymous");

        // Проверка метода запроса
        if (!validateMethod(exchange, "PATCH")) {
            return;
        }

        // Проверка типа содержимого
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            HttpUtils.sendError(exchange, 415, "Content-Type must be application/json");
            return;
        }

        try {
            // Разбор и обработка запроса
            ConfigRequestDto configRequest = JsonUtil.fromJson(exchange.getRequestBody(), ConfigRequestDto.class);
            configRequest.validate();

            // Обновление конфигурации
            managementService.updateOtpConfig(configRequest.getLength(), configRequest.getTtlSeconds());

            // Отправка успешного ответа
            HttpUtils.sendEmptyResponse(exchange, 204);
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            LOG.error("Error updating OTP config", e);
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }

    /**
     * Проверяет, что запрос использует ожидаемый HTTP-метод
     *
     * @param exchange       HTTP обмен для проверки
     * @param expectedMethod ожидаемый HTTP-метод
     * @return true, если метод действителен, false в противном случае
     * @throws IOException если происходит ошибка ввода-вывода
     */
    private boolean validateMethod(HttpExchange exchange, String expectedMethod) throws IOException {
        if (!expectedMethod.equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendError(exchange, 405, "Method Not Allowed");
            return false;
        }
        return true;
    }

    /**
     * Получает список всех пользователей, не являющихся администраторами
     *
     * @param exchange HTTP обмен, содержащий запрос
     * @throws IOException если происходит ошибка ввода-вывода
     */
    public void listUsers(HttpExchange exchange) throws IOException {
        User user = (User) exchange.getAttribute("user");
        LOG.info("Request received - Method: GET, Path: /admin/users, User: {}", user != null ? user.getId() : "anonymous");

        // Проверка метода запроса
        if (!validateMethod(exchange, "GET")) {
            return;
        }

        try {
            // Получение пользователей и преобразование в JSON
            List<User> users = managementService.getAllUsersWithoutAdmins();
            String jsonResponse = JsonUtil.toJson(users);

            // Отправка успешного ответа
            HttpUtils.sendJsonResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            LOG.error("Error retrieving users", e);
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }

    /**
     * Удаляет пользователя и связанные с ним OTP-коды
     *
     * @param exchange HTTP обмен, содержащий запрос
     * @throws IOException если происходит ошибка ввода-вывода
     */
    public void deleteUser(HttpExchange exchange) throws IOException {
        User user = (User) exchange.getAttribute("user");
        LOG.info("Request received - Method: DELETE, Path: /admin/users/{id}, User: {}", user != null ? user.getId() : "anonymous");

        // Проверка метода запроса
        if (!validateMethod(exchange, "DELETE")) {
            return;
        }

        try {
            // Извлечение ID пользователя из пути
            URI uri = exchange.getRequestURI();
            String[] pathSegments = uri.getPath().split("/");
            Long userId = Long.valueOf(pathSegments[pathSegments.length - 1]);

            // Удаление пользователя и связанных данных
            managementService.deleteUserAndCodes(userId);

            // Отправка успешного ответа
            HttpUtils.sendEmptyResponse(exchange, 204);
        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Invalid user ID format");
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 404, e.getMessage());
        } catch (Exception e) {
            LOG.error("Error deleting user", e);
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }

}
