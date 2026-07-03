# Eta English tutor agent

## Version 1.0.0

This is application (AI agent) for helping English tutor in work. UI is implemented via telegram bot.
In version 1.0.0 we must have follow functionality:
1. Teacher can start session (lesson) in tg bot and send to bot unknown words during lesson. Bot saves these words and
make reference with student.
2. After lesson (and in any other moment) teacher can get and send dictionary with words to student.
3. Teacher can generate via an agent exercise according specified topic with using words from dictionary.

### Using technologies:
1. Spring Boot 4.1.0
2. Spring Modulith 2.1.0
3. JMolecules 2025.0.2
4. Spring AI (planned)
5. PostgreSQL + Flyway (planned)
6. Telegram Bots 6.9.7.1

### Software architecture:
1. Modular monolith
2. Domain driven design

### Bounded contexts (DDD Modules)
1. **Teacher** — учитель, управление учениками, оркестрация создания Student+Dictionary
2. **Student** — ученик, уроки (Lesson как часть агрегата)
3. **Dictionary** — словарь ученика
4. **Exercise** — упражнения (скелет)
5. **Chatbot** — Telegram бот, стейт-машина, команды
6. **Shared** — общие контракты: Specification, DomainException

### Features
1. Учитель может стартануть бота и зарегистрироваться (регистрация предполагает сохранение chatId и имени по которому 
бот будет обращаться к учителю).
2. Учитель может добавлять к себе уникальных учеников.
3. Учитель может просмотреть список всех своих учеников.
4. Учитель может просмотреть детали ученика (выбор ученика через клик по кнопке).
5. Учитель может начать урок для конкретного ученика (выбор ученика через клик по кнопке).
6. Учитель в рамках начатого урока добавляет слова ученику.
7. Учитель завершает урок, в ответ получаем список слов добавленных в рамках урока.
8. Учитель вне урока, генерирует упражнения для ученика. Для упражнения надо ввести тему и тип (сразу два типа, вставить
пропущенное слово, и выбрать вариант ответа). Для генерации упражнения берем все слова ученика в статусе на изучении.