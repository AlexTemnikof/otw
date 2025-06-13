# OTP Service

Сервис для генерации и проверки одноразовых паролей (OTP) с поддержкой различных каналов доставки.

## Проверка работы
Тестирование сервиса с использованием Postman находятся в [postman_testing.md](postman_testing.md)
Также присутсвуют тесты в папке src/test/java/org/example/otp
## Обзор сервиса
OTP Service предоставляет API для:
- Регистрации и аутентификации пользователей
- Генерации одноразовых паролей (OTP)
- Отправки OTP через различные каналы (Email, SMS, Telegram, File)
- Проверки OTP кодов
- Управления конфигурацией OTP (длина кода, время жизни)
- Управления пользователями (для администраторов)

## Как пользоваться сервисом

### Установка и запуск

1. Клонируйте репозиторий
2. Настройте файлы конфигурации в `src/main/resources/`:
   - `application.properties` - основные настройки приложения
   - `email.properties` - настройки для отправки email
   - `sms.properties` - настройки для отправки SMS
   - `telegram.properties` - настройки для отправки через Telegram


Сервис запустится на порту 8080 (можно изменить в `application.properties`).

### Конфигурация

#### application.properties
```properties
server.port=8080
```

#### email.properties
```properties
email.host=smtp.example.com
email.port=587
email.username=your-email@example.com
email.password=your-password
email.from=noreply@example.com
mail.smtp.auth=true
mail.smtp.starttls.enable=true
```

#### sms.properties и telegram.properties
Настройте в соответствии с требованиями вашего SMS-провайдера или Telegram API.

## Поддерживаемые команды (API endpoints)

### Публичные эндпоинты

#### Регистрация пользователя
```
POST /register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123",
  "role": "USER"
}
```

#### Вход в систему
```
POST /login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123"
}
```
Ответ:
```json
{
  "token": "jwt-token"
}
```

### Пользовательские эндпоинты (требуют аутентификации)

#### Генерация OTP кода
```
POST /otp/generate
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "userId": 1,
  "operationId": "payment-123",
  "channel": "EMAIL"
}
```

#### Проверка OTP кода
```
POST /otp/validate
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "code": "123456"
}
```

### Административные эндпоинты (требуют роли ADMIN)

#### Обновление конфигурации OTP
```
PATCH /admin/config
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "length": 6,
  "ttlSeconds": 300
}
```

#### Получение списка пользователей
```
GET /admin/users
Authorization: Bearer <jwt-token>
```

#### Удаление пользователя
```
DELETE /admin/users/{userId}
Authorization: Bearer <jwt-token>
```

## Как протестировать код

### Запуск тестов

Для запуска всех тестов:
```
mvn test
```

Для запуска конкретного теста:
```
mvn test -Dtest=OtpServiceTest
```

### Ручное тестирование

1. Запустите сервис
2. Зарегистрируйте пользователя с ролью ADMIN:
   ```
   curl -X POST -H "Content-Type: application/json" -d '{"username":"admin@example.com","password":"admin123","role":"ADMIN"}' http://localhost:8080/register
   ```
3. Получите JWT токен:
   ```
   curl -X POST -H "Content-Type: application/json" -d '{"username":"admin@example.com","password":"admin123"}' http://localhost:8080/login
   ```
4. Используйте полученный токен для доступа к защищенным эндпоинтам:
   ```
   curl -X PATCH -H "Authorization: Bearer <jwt-token>" -H "Content-Type: application/json" -d '{"length":6,"ttlSeconds":300}' http://localhost:8080/admin/config
   ```

### Тестирование каналов доставки

#### Email
Для тестирования отправки по email:
1. Настройте `email.properties` с реальными данными SMTP-сервера
2. Зарегистрируйте пользователя с email-адресом
3. Сгенерируйте OTP с каналом EMAIL

#### File
Для тестирования без настройки реальных каналов доставки можно использовать канал FILE, который сохраняет OTP коды в лог-файл.

## Примечания по безопасности

- Пароли хранятся в хешированном виде
- JWT токены имеют ограниченный срок действия
- OTP коды имеют настраиваемое время жизни
- Для генерации OTP используется SecureRandom
- Неактивные OTP коды автоматически помечаются как истекшие
