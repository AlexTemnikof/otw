package org.example.otp.dao;

import org.example.otp.model.OtpCode;
import java.time.Duration;
import java.util.List;

/**
 * Интерфейс доступа к данным для работы с OTP кодами
 * Предоставляет методы для сохранения, поиска и управления статусами кодов
 */
public interface OtpCodeDao {

    void save(OtpCode code);


    OtpCode findByCode(String code);

    List<OtpCode> findAllByUser(Long userId);

    void markAsUsed(Long id);

    void markAsExpiredOlderThan(Duration ttl);

    void deleteAllByUserId(Long userId);
}
