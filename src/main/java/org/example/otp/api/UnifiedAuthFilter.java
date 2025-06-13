package org.example.otp.api;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import org.example.otp.model.User;
import org.example.otp.model.UserRole;
import org.example.otp.util.HttpUtils;
import org.example.otp.util.JwtUtils;
import org.example.otp.util.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Универсальный фильтр аутентификации
 * Поддерживает как обычные токены, так и JWT токены для аутентификации пользователей
 */
public class UnifiedAuthFilter extends Filter {
    private static final Logger logger = LoggerFactory.getLogger(UnifiedAuthFilter.class);
    private final UserRole requiredRole;

    public UnifiedAuthFilter(UserRole requiredRole) {
        this.requiredRole = requiredRole;
    }

    @Override
    public String description() {
        return "Unified authentication filter (ROLE >= " + requiredRole + ")";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header");
            HttpUtils.sendError(exchange, 401, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        User user = TokenManager.getUser(token);

        if (user == null) {
            if (JwtUtils.validateToken(token)) {
                String username = JwtUtils.extractUsername(token);
                logger.info("JWT token validated for user {}, but user lookup not implemented", username);
                HttpUtils.sendError(exchange, 401, "JWT authentication not fully implemented");
                return;
            } else {
                logger.warn("Invalid or expired token");
                HttpUtils.sendError(exchange, 401, "Invalid or expired token");
                return;
            }
        }

        if (user.getRole().ordinal() < requiredRole.ordinal()) {
            logger.warn("User {} has insufficient privileges (required: {})", user.getUsername(), requiredRole);
            HttpUtils.sendError(exchange, 403, "Forbidden");
            return;
        }

        exchange.setAttribute("user", user);
        logger.info("User {} authenticated successfully", user.getUsername());

        chain.doFilter(exchange);
    }
}
