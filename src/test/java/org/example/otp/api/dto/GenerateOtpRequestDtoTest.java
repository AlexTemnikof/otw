package org.example.otp.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса GenerateOtpRequestDto
 */
public class GenerateOtpRequestDtoTest {

    /**
     * Тест проверяет корректное создание объекта с валидными данными
     */
    @Test
    public void testValidDto() {
        // Подготовка тестовых данных
        Long userId = 1L;
        String operationId = "operation123";
        String channel = "EMAIL";

        // Выполнение тестируемого метода
        GenerateOtpRequestDto dto = new GenerateOtpRequestDto(userId, operationId, channel);

        // Проверка результатов
        assertEquals(userId, dto.getUserId(), "ID пользователя должен соответствовать заданному значению");
        assertEquals(operationId, dto.getOperationId(), "ID операции должен соответствовать заданному значению");
        assertEquals(channel, dto.getChannel(), "Канал должен соответствовать заданному значению");

        // Проверка, что валидация не выбрасывает исключений
        assertDoesNotThrow(() -> dto.validate(), "Валидация должна проходить без ошибок для корректных данных");
    }

    /**
     * Тест проверяет, что валидация выбрасывает исключение при null значении userId
     */
    @Test
    public void testValidateNullUserId() {
        // Подготовка тестовых данных
        GenerateOtpRequestDto dto = new GenerateOtpRequestDto(null, "operation123", "EMAIL");

        // Проверка, что валидация выбрасывает ожидаемое исключение
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> dto.validate(), "Должно быть выброшено исключение при null значении userId");

        // Проверка сообщения об ошибке
        assertEquals("User ID is required", exception.getMessage(), "Сообщение об ошибке должно указывать на отсутствие userId");
    }


    /**
     * Тест проверяет валидацию с пустым значением operationId
     */
    @Test
    public void testValidateEmptyOperationId() {
        // Подготовка тестовых данных
        GenerateOtpRequestDto dto = new GenerateOtpRequestDto(1L, "", "EMAIL");

        // Проверка, что валидация выбрасывает ожидаемое исключение
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> dto.validate(), "Должно быть выброшено исключение при пустом значении operationId");

        // Проверка сообщения об ошибке
        assertEquals("Operation ID is required", exception.getMessage(), "Сообщение об ошибке должно указывать на пустое значение operationId");
    }

    /**
     * Тест проверяет валидацию с null значением channel
     */
    @Test
    public void testValidateNullChannel() {
        // Подготовка тестовых данных
        GenerateOtpRequestDto dto = new GenerateOtpRequestDto(1L, "operation123", null);

        // Проверка, что валидация выбрасывает ожидаемое исключение
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> dto.validate(), "Должно быть выброшено исключение при null значении channel");

        // Проверка сообщения об ошибке
        assertEquals("Channel is required", exception.getMessage(), "Сообщение об ошибке должно указывать на отсутствие channel");
    }

    /**
     * Тест проверяет валидацию с некорректным значением channel "PHONE"
     */
    @Test
    public void testValidateInvalidChannel() {
        // Подготовка тестовых данных
        GenerateOtpRequestDto dto = new GenerateOtpRequestDto(1L, "operation123", "PHONE");

        // Проверка, что валидация выбрасывает ожидаемое исключение
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> dto.validate(), "Должно быть выброшено исключение при некорректном значении channel");

        // Проверка сообщения об ошибке
        assertEquals("Channel must be one of: SMS, EMAIL, TELEGRAM, FILE", exception.getMessage(), "Сообщение об ошибке должно указывать на некорректное значение channel");
    }


}
