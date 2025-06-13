package org.example.otp.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Менеджер для работы с базой данных
 * Обеспечивает подключение к БД и управление ресурсами соединения
 */
public class DatabaseManager {
    private static final String PROPS_FILE = "application.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream is = DatabaseManager.class
                .getClassLoader()
                .getResourceAsStream(PROPS_FILE)) {
            if (is == null) {
                throw new RuntimeException("Не найден файл " + PROPS_FILE + " в classpath");
            }
            Properties props = new Properties();
            props.load(is);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Ошибка загрузки параметров БД из " + PROPS_FILE + ": " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
