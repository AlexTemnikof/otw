package org.example.otp.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import org.example.otp.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Диспетчер HTTP запросов
 * Регистрирует обработчики для различных эндпоинтов API и настраивает фильтры авторизации
 */
public class Dispatcher {
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
    private final AuthController authController = new AuthController();
    private final UserController userController = new UserController();
    private final AdminController adminController = new AdminController();

    public void registerRoutes(HttpServer server) {
        server.createContext("/register", authController::handleRegister);
        server.createContext("/login",    authController::handleLogin);

        HttpContext genCtx = server.createContext("/otp/generate", userController::generateOtp);
        genCtx.getFilters().add(new UnifiedAuthFilter(UserRole.USER));
        HttpContext valCtx = server.createContext("/otp/validate", userController::validateOtp);
        valCtx.getFilters().add(new UnifiedAuthFilter(UserRole.USER));
        HttpContext configCtx = server.createContext("/admin/config", adminController::updateOtpConfig);
        configCtx.getFilters().add(new UnifiedAuthFilter(UserRole.ADMIN));
        HttpContext usersCtx = server.createContext("/admin/users", exchange -> {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                adminController.listUsers(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                adminController.deleteUser(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });
        usersCtx.getFilters().add(new UnifiedAuthFilter(UserRole.ADMIN));
    }
}
