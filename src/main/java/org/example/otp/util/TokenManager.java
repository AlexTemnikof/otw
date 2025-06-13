package org.example.otp.util;

import org.example.otp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для управления аутентификационными токенами
 * Предоставляет функциональность для генерации, проверки и отзыва токенов доступа
 */
public final class TokenManager {
    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);

    private static final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    private static final long TTL_MINUTES = 30;

    private TokenManager() { }

    public static String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(TTL_MINUTES, ChronoUnit.MINUTES);
        tokens.put(token, new TokenInfo(user, expiry));
        logger.info("Generated token {} for user {} (expires at {})", token, user.getUsername(), expiry);
        return token;
    }

    public static boolean validate(String token) {
        TokenInfo info = tokens.get(token);
        if (info == null) {
            logger.warn("Token validation failed: token not found");
            return false;
        }
        if (Instant.now().isAfter(info.expiry)) {
            tokens.remove(token);
            logger.warn("Token {} expired at {}, removed from store", token, info.expiry);
            return false;
        }
        return true;
    }

    public static User getUser(String token) {
        if (!validate(token)) {
            return null;
        }
        return tokens.get(token).user;
    }

    public static void revoke(String token) {
        if (tokens.remove(token) != null) {
            logger.info("Token {} revoked", token);
        }
    }

    private static class TokenInfo {
        final User user;
        final Instant expiry;

        TokenInfo(User user, Instant expiry) {
            this.user = user;
            this.expiry = expiry;
        }
    }
}
