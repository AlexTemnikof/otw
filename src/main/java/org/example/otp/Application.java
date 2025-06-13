package org.example.otp;

import com.sun.net.httpserver.HttpServer;
import org.example.otp.api.Dispatcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Главный класс приложения
 * Содержит точку входа в приложение и отвечает за запуск HTTP сервера
 */
public class Application {
    public static void main(String[] args) {
        try {
            Properties config = new Properties();
            try (InputStream is = Application.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (is != null) {
                    config.load(is);
                }
            }
            int port = Integer.parseInt(config.getProperty("server.port", "8080"));

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            Dispatcher dispatcher = new Dispatcher();
            dispatcher.registerRoutes(server);

            server.start();
            System.out.println("Server started on http://localhost:" + port);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
