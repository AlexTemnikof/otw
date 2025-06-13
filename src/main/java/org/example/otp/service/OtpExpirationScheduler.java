package org.example.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Планировщик для автоматического обнаружения и маркировки просроченных OTP кодов
 * Запускает периодическую задачу по очистке неактуальных кодов в системе
 */
public class OtpExpirationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(OtpExpirationScheduler.class);

    private final OtpService otpService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final long intervalMinutes;

    public OtpExpirationScheduler(OtpService otpService, long intervalMinutes) {
        this.otpService = otpService;
        this.intervalMinutes = intervalMinutes;
    }

    public void start() {
        logger.info("Starting OTP-expiration scheduler, interval={} min", intervalMinutes);
        scheduler.scheduleAtFixedRate(
                this::run,
                intervalMinutes,
                intervalMinutes,
                TimeUnit.MINUTES
        );
    }

    public void run() {
        try {
            otpService.markExpiredOtps();
            logger.debug("OtpExpirationScheduler run(): expired codes processed");
        } catch (Exception e) {
            logger.error("Error in OTP-expiration task", e);
        }
    }

    public void stop() {
        logger.info("Stopping OTP-expiration scheduler");
        scheduler.shutdownNow();
    }
}
