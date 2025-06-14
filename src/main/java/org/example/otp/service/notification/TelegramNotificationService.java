package org.example.otp.service.notification;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Сервис отправки уведомлений через Telegram
 * Реализует отправку OTP кодов через Telegram Bot API
 */
public class TelegramNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final String apiBaseUrl;
    private final String token;
    private final String defaultChatId;

    public TelegramNotificationService() {
        Properties props = loadConfig();
        this.apiBaseUrl    = props.getProperty("telegram.apiUrl");
        this.token         = props.getProperty("telegram.token");
        this.defaultChatId = props.getProperty("telegram.chatId");
    }

    private Properties loadConfig() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("telegram.properties")) {
            if (is == null) throw new IllegalStateException("telegram.properties not found in classpath");
            Properties props = new Properties();
            props.load(is);
            return props;
        } catch (Exception e) {
            logger.error("Failed to load telegram.properties", e);
            throw new RuntimeException("Could not load Telegram configuration", e);
        }
    }


    @Override
    public void sendCode(String recipientChatId, String code) {
        String chatId = (recipientChatId == null || recipientChatId.isBlank())
                ? defaultChatId
                : recipientChatId;
        String text = "Your one-time confirmation code is: " + code;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI uri = new URIBuilder(apiBaseUrl + token + "/sendMessage")
                    .addParameter("chat_id", chatId)
                    .addParameter("text", text)
                    .build();

            HttpGet request = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int status = response.getStatusLine().getStatusCode();
                if (status != 200) {
                    logger.error("Telegram API error. Status code: {}", status);
                    throw new RuntimeException("Telegram API returned " + status);
                }
                logger.info("OTP code sent via Telegram to chatId {}", chatId);
            }
        } catch (URISyntaxException e) {
            logger.error("Invalid URI for Telegram API", e);
            throw new RuntimeException("Invalid Telegram API URI", e);
        } catch (Exception e) {
            logger.error("Failed to send Telegram message to {}", chatId, e);
            throw new RuntimeException("Telegram sending failed", e);
        }
    }
}
