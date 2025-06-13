package org.example.otp.api;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Filter.Chain;
import org.example.otp.model.User;
import org.example.otp.model.UserRole;
import org.example.otp.util.HttpUtils;
import org.example.otp.util.TokenManager;

import java.io.IOException;

/**
 * Фильтр для аутентификации и авторизации пользователей
 * Проверяет наличие и валидность токена, а также соответствие роли пользователя требуемому уровню доступа
 */
public class AuthFilter extends Filter {
    private final UserRole requiredRole;

    public AuthFilter(UserRole requiredRole) {
        this.requiredRole = requiredRole;
    }

    @Override
    public String description() {
        return "Фильтр аутентификации и проверки роли (ROLE >= " + requiredRole + ")";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            HttpUtils.sendError(exchange, 401, "Missing or invalid Authorization header");
            return;
        }
        String token = authHeader.substring(7);
        User user = TokenManager.getUser(token);
        if (user == null) {
            HttpUtils.sendError(exchange, 401, "Invalid or expired token");
            return;
        }
        if (user.getRole().ordinal() < requiredRole.ordinal()) {
            HttpUtils.sendError(exchange, 403, "Forbidden");
            return;
        }
        exchange.setAttribute("user", user);
        chain.doFilter(exchange);
    }
}
