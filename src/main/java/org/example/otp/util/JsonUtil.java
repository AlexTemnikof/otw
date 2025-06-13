package org.example.otp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

/**
 * Утилитарный класс для работы с JSON
 * Предоставляет методы для сериализации и десериализации объектов
 */
public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T fromJson(InputStream is, Class<T> clazz) throws IOException {
        return MAPPER.readValue(is, clazz);
    }

    public static String toJson(Object obj) throws IOException {
        return MAPPER.writeValueAsString(obj);
    }
}
