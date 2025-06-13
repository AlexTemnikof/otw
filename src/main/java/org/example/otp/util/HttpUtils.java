package org.example.otp.util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Утилитарный класс для работы с HTTP запросами и ответами
 * Предоставляет методы для отправки JSON ответов и обработки ошибок
 */
public class HttpUtils {

    public static void sendJsonResponse(HttpExchange exch, int status, String json) throws IOException {
        exch.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exch.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exch.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendEmptyResponse(HttpExchange exch, int status) throws IOException {
        exch.sendResponseHeaders(status, -1);
    }

    public static void sendError(HttpExchange exch, int status, String message) throws IOException {
        String errorJson = String.format("{\"error\":\"%s\"}", message);
        sendJsonResponse(exch, status, errorJson);
    }
}
