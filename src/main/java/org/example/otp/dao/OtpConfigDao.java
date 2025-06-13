package org.example.otp.dao;

import org.example.otp.model.OtpConfig;

/**
 * Интерфейс доступа к данным для работы с конфигурацией OTP
 * Предоставляет методы для получения и обновления настроек одноразовых паролей
 */
public interface OtpConfigDao {

    OtpConfig getConfig();

    void updateConfig(OtpConfig config);

    void initDefaultConfigIfEmpty();
}
