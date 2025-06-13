package org.example.otp.service;

import org.example.otp.dao.OtpCodeDao;
import org.example.otp.dao.OtpConfigDao;
import org.example.otp.dao.UserDao;
import org.example.otp.model.OtpCode;
import org.example.otp.model.OtpConfig;
import org.example.otp.model.OtpStatus;
import org.example.otp.model.User;
import org.example.otp.service.notification.NotificationChannel;
import org.example.otp.service.notification.NotificationService;
import org.example.otp.service.notification.NotificationServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Тесты для класса OtpService
 */
public class OtpServiceTest {

    @Mock
    private OtpCodeDao otpCodeDao;

    @Mock
    private OtpConfigDao otpConfigDao;

    @Mock
    private UserDao userDao;

    @Mock
    private NotificationService notificationService;

    // Создаем тестовую реализацию фабрики вместо мока
    private TestNotificationServiceFactory notificationFactory;

    private OtpService otpService;

    private OtpConfig defaultConfig;
    private User testUser;

    /**
     * Тестовая реализация фабрики для тестирования
     */
    private static class TestNotificationServiceFactory extends NotificationServiceFactory {
        private final NotificationService mockService;

        public TestNotificationServiceFactory(NotificationService mockService) {
            this.mockService = mockService;
        }

        @Override
        public NotificationService getService(NotificationChannel channel) {
            return mockService;
        }
    }

    /**
     * Настройка перед каждым тестом
     */
    @BeforeEach
    public void setUp() {
        // Инициализация моков
        MockitoAnnotations.openMocks(this);

        // Инициализация фабрики с моком сервиса уведомлений
        notificationFactory = new TestNotificationServiceFactory(notificationService);

        // Создание тестируемого сервиса
        otpService = new OtpService(otpCodeDao, otpConfigDao, userDao, notificationFactory);

        // Настройка конфигурации OTP по умолчанию
        defaultConfig = new OtpConfig();
        defaultConfig.setLength(6);
        defaultConfig.setTtlSeconds(300); // 5 минут
        when(otpConfigDao.getConfig()).thenReturn(defaultConfig);

        // Настройка тестового пользователя
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@example.com");
        when(userDao.findById(1L)).thenReturn(testUser);
    }

    /**
     * Тест генерации OTP кода
     */
    @Test
    public void testGenerateOtp() {
        // Подготовка
        Long userId = 1L;
        String operationId = "test-operation";

        // Выполнение
        String code = otpService.generateOtp(userId, operationId);

        // Проверка
        assertNotNull(code, "Код не должен быть null");
        assertEquals(defaultConfig.getLength(), code.length(), "Длина кода должна соответствовать конфигурации");
        assertTrue(code.matches("\\d+"), "Код должен состоять только из цифр");

        // Проверка сохранения кода в DAO
        ArgumentCaptor<OtpCode> otpCaptor = ArgumentCaptor.forClass(OtpCode.class);
        verify(otpCodeDao).save(otpCaptor.capture());

        OtpCode savedOtp = otpCaptor.getValue();
        assertEquals(userId, savedOtp.getUserId(), "ID пользователя должен совпадать");
        assertEquals(operationId, savedOtp.getOperationId(), "ID операции должен совпадать");
        assertEquals(code, savedOtp.getCode(), "Код должен совпадать");
        assertEquals(OtpStatus.ACTIVE, savedOtp.getStatus(), "Статус должен быть ACTIVE");
        assertNotNull(savedOtp.getCreatedAt(), "Дата создания не должна быть null");
    }

    /**
     * Тест отправки OTP кода пользователю
     */
    @Test
    public void testSendOtpToUser() {
        // Подготовка
        Long userId = 1L;
        String operationId = "test-operation";
        NotificationChannel channel = NotificationChannel.EMAIL;

        // Выполнение
        otpService.sendOtpToUser(userId, operationId, channel);

        // Проверка
        verify(userDao).findById(userId);
        // Не проверяем вызов notificationFactory.getService, так как это не мок
        verify(notificationService).sendCode(eq(testUser.getUsername()), anyString());
    }

    /**
     * Тест отправки OTP кода несуществующему пользователю
     */
    @Test
    public void testSendOtpToNonExistentUser() {
        // Подготовка
        Long userId = 999L;
        String operationId = "test-operation";
        NotificationChannel channel = NotificationChannel.EMAIL;

        when(userDao.findById(userId)).thenReturn(null);

        // Выполнение и проверка
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> otpService.sendOtpToUser(userId, operationId, channel),
            "Должно быть выброшено исключение при отправке кода несуществующему пользователю"
        );

        assertEquals("User not found", exception.getMessage(), 
            "Сообщение об ошибке должно указывать на отсутствие пользователя");

        verify(userDao).findById(userId);
        verify(notificationService, never()).sendCode(anyString(), anyString());
    }

    /**
     * Тест успешной валидации OTP кода
     */
    @Test
    public void testValidateOtpSuccess() {
        // Подготовка
        String code = "123456";
        OtpCode otpCode = new OtpCode();
        otpCode.setId(1L);
        otpCode.setCode(code);
        otpCode.setStatus(OtpStatus.ACTIVE);
        otpCode.setCreatedAt(LocalDateTime.now());

        when(otpCodeDao.findByCode(code)).thenReturn(otpCode);

        // Выполнение
        boolean result = otpService.validateOtp(code);

        // Проверка
        assertTrue(result, "Валидация должна быть успешной");
        verify(otpCodeDao).markAsUsed(otpCode.getId());
    }

    /**
     * Тест неудачной валидации OTP кода
     */
    @Test
    public void testValidateNonExistentOtp() {
        // Подготовка
        String code = "123456";
        when(otpCodeDao.findByCode(code)).thenReturn(null);

        // Выполнение
        boolean result = otpService.validateOtp(code);

        // Проверка
        assertFalse(result, "Валидация должна быть неуспешной");
        verify(otpCodeDao, never()).markAsUsed(anyLong());
    }

    /**
     * Тест неудачной валидации просроченного OTP кода
     */
    @Test
    public void testValidateExpiredOtp() {
        // Подготовка
        String code = "123456";
        OtpCode otpCode = new OtpCode();
        otpCode.setId(1L);
        otpCode.setCode(code);
        otpCode.setStatus(OtpStatus.ACTIVE);
        otpCode.setCreatedAt(LocalDateTime.now().minusMinutes(10)); // Код создан 10 минут назад

        when(otpCodeDao.findByCode(code)).thenReturn(otpCode);

        // Выполнение
        boolean result = otpService.validateOtp(code);

        // Проверка
        assertFalse(result, "Валидация должна быть неуспешной");
        verify(otpCodeDao).markAsExpiredOlderThan(any(Duration.class));
        verify(otpCodeDao, never()).markAsUsed(anyLong());
    }

    /**
     * Тест маркировки просроченных OTP кодов
     */
    @Test
    public void testMarkExpiredOtps() {
        // Выполнение
        otpService.markExpiredOtps();

        // Проверка
        verify(otpCodeDao).markAsExpiredOlderThan(Duration.ofSeconds(defaultConfig.getTtlSeconds()));
    }
}
