# JavaAlmas20 — Quiz Pro: Система тестирования

> Clean Architecture • JWT/OAuth2 • RBAC • Audit Logs • GDPR • Docker • Kubernetes

---

## 📐 Архитектура

```
┌──────────────────────────────────────────────────────────────┐
│                      Client (REST API)                       │
└────────────────────────┬─────────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────────┐
│                    Controller Layer                           │
│   AuthController · UserController · AdminController          │
│   QuestionController · RoomController · QuizController       │
└────────────────────────┬─────────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────────┐
│                     Service Layer                             │
│   AuthService · UserService · AuditService · RefreshToken    │
│   QuestionService · RoomService · QuizService                │
│   EventPublisherService (Kafka)                              │
└────────────────────────┬─────────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────────┐
│                   Repository Layer (JPA)                      │
│   UserRepository · RoleRepository · AuditLogRepository       │
│   QuestionRepository · RoomRepository · QuizResultRepository │
└────────────────────────┬─────────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────────┐
│                    Domain Entities                             │
│   User · Role · AuditLog · RefreshToken                      │
│   Question · Room · QuizResult                               │
└──────────────────────────────────────────────────────────────┘
```

---

## 🛠 Технологический стек

| Слой          | Технология                          |
|---------------|-------------------------------------|
| Язык          | Java 17                             |
| Фреймворк     | Spring Boot 3.4.x                   |
| Безопасность   | Spring Security + JWT (jjwt 0.12)   |
| База данных    | PostgreSQL 16                       |
| Кэширование    | Redis 7                             |
| Сообщения      | Apache Kafka                        |
| Маппинг        | MapStruct 1.6                       |
| API Docs       | SpringDoc OpenAPI (Swagger UI)      |
| Тестирование   | JUnit 5, Mockito, Testcontainers    |
| Нагрузочные тесты | k6                              |
| Контейнеризация | Docker + Docker Compose            |
| Оркестрация    | Kubernetes (minikube/k3s/cloud)     |
| CI/CD          | GitHub Actions                      |

---

## 🚀 Быстрый старт

### Требования
- Java 17+
- Docker & Docker Compose
- Maven 3.9+

### Запуск через Docker Compose
```bash
docker compose up -d
```
- Приложение: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Запуск локально
```bash
# Запустить зависимости
docker compose up -d postgres redis kafka zookeeper

# Запустить приложение
./mvnw spring-boot:run
```

### Запуск тестов
```bash
./mvnw test -Dspring.profiles.active=test
```

### Учётная запись администратора по умолчанию
- **Логин:** `admin`
- **Пароль:** `Admin@123456`

---

## 📡 API — Примеры запросов

### Аутентификация

```bash
# Регистрация
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher1","email":"teacher@test.com","password":"Password123!","firstName":"Иван","lastName":"Петров"}'

# Вход
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher1","password":"Password123!"}'
```

### Вопросы (Преподаватель)

```bash
# Создать вопрос
curl -X POST http://localhost:8080/api/questions \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"questionText":"Столица Казахстана?","opt1":"Алматы","opt2":"Астана","opt3":"Шымкент","opt4":"Караганда","correctIndex":1}'

# Получить все вопросы
curl http://localhost:8080/api/questions -H "Authorization: Bearer <TOKEN>"
```

### Комнаты

```bash
# Создать комнату
curl -X POST http://localhost:8080/api/rooms \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Тест по теме 1"}'

# Проверить код комнаты
curl http://localhost:8080/api/rooms/validate/AB12CD -H "Authorization: Bearer <TOKEN>"

# Закрыть комнату
curl -X POST http://localhost:8080/api/rooms/AB12CD/close -H "Authorization: Bearer <TOKEN>"
```

### Тест (Студент)

```bash
# Сдать тест
curl -X POST http://localhost:8080/api/quiz/submit \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"studentName":"Алмас","roomCode":"AB12CD","answers":[1,0,2,3]}'

# Результаты по комнате (преподаватель)
curl http://localhost:8080/api/quiz/results/room/AB12CD -H "Authorization: Bearer <TOKEN>"
```

---

## 🔐 Безопасность

- **JWT** access + refresh токены с ротацией
- **RBAC** роли: `ROLE_USER`, `ROLE_MODERATOR`, `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **BCrypt** хеширование паролей (strength 12)
- **Аудит-логи** — каждое действие записывается
- **GDPR** — запрос на удаление данных + анонимизация

---

## 🗂 Структура проекта

```
src/main/java/org/example/javaalmas20/
├── config/           # Security, OpenAPI, Async, DataInitializer
├── controller/       # REST контроллеры (Auth, User, Admin, Question, Room, Quiz)
├── domain/entity/    # JPA сущности (User, Role, Question, Room, QuizResult, AuditLog)
├── dto/
│   ├── request/      # Входные DTO (Register, Login, Question, Room, Quiz)
│   └── response/     # Выходные DTO (User, JWT, Question, Room, Result, Error)
├── exception/        # Кастомные исключения + глобальный обработчик
├── mapper/           # MapStruct маперы
├── repository/       # Spring Data JPA репозитории
├── security/         # JWT провайдер, фильтр, точка входа
└── service/          # Бизнес-логика
```

---

## 📜 Лицензия

MIT License
