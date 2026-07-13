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
1. Учитель регистрируется в боте (сохраняется chatId и имя учителя).
2. Учитель может добавлять к себе уникальных учеников.
3. Учитель может просмотреть список всех своих учеников.
4. Учитель может просмотреть детали ученика (выбор ученика через клик по кнопке).
5. Учитель может начать урок для конкретного ученика (выбор ученика через клик по кнопке).
6. Учитель в рамках начатого урока добавляет слова ученику.
7. Учитель завершает урок, в ответ получает список слов добавленных в рамках урока.
8. Учитель вне урока, генерирует упражнения для ученика. Для упражнения надо ввести тему и тип (сразу два типа, вставить
пропущенное слово, и выбрать вариант ответа). Для генерации упражнения берем все слова ученика в статусе на изучении 
(выбор ученика через клик по кнопке).

### ChatBot commands && callbacks

#### Текстовые команды

| # | Команда | Описание |
|---|---------|----------|
| 1 | `/register` | Регистрация учителя (ввод имени) |
| 2 | `/new` | Добавление нового ученика (ошибка по уникальности имени) |
| 3 | `/list` | Список учеников в виде inline-кнопок |
| 4 | `/addword` | Добавить слово в активном уроке (трёхшаговый ввод: слово → часть речи → перевод) |
| 5 | `/finishlesson` | Завершить активный урок |
| 6 | `/help` | Описание бота, список доступных команд |

#### Callback'и (inline-кнопки)

| # | Действие | Описание | Callback data |
|---|----------|----------|---------------|
| C1 | Выбор ученика | Клик по кнопке с именем ученика → меню действий | `student:<uuid>` |
| C2 | Меню ученика | После выбора ученика: Start Lesson, Details, Exercise, Назад | (автоматически, S6) |
| C3 | Start Lesson | Начать урок для выбранного ученика | `action:startlesson` |
| C4 | Details | Показать карточку ученика (имя, статистика словаря, статус урока) | `action:details` |
| C5 | Exercise | Сгенерировать упражнение (многоступенчатый ввод: тип → тема → ответ) | `action:exercise` |
| C6 | Назад к списку | Вернуться от меню ученика к списку учеников | `action:back` |
| C7 | Назад к меню | Вернуться от карточки ученика к меню | `details:back` |
| C8 | Тип упражнения | FILL_IN_THE_BLANK или MULTIPLE_CHOICE | `exercise:FILL_IN_THE_BLANK` / `exercise:MULTIPLE_CHOICE` |
| C9 | Часть речи | Выбор NOUN, VERB или ADJECTIVE (шаг 2 ввода слова) | `pos:NOUN`, `pos:VERB`, `pos:ADJECTIVE` |