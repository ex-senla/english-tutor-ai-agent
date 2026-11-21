# Eta English tutor agent

> Актуальные открытые задачи: https://github.com/ex-senla/english-tutor-ai-agent/issues
> Состояние workflow: `.pi/agentcraft-state.md`

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
4. Spring AI 2.0.0 (реальные LLM-вызовы: генерация, валидация темы, POS+переводы)
5. PostgreSQL + Flyway (planned)
6. Telegram Bots 6.9.7.1
7. OpenHTMLtoPDF + Thymeleaf (экспорт упражнений в PDF)

### Software architecture:
1. Modular monolith
2. Domain driven design

### Bounded contexts (DDD Modules)
1. **Teacher** — учитель, управление учениками, оркестрация создания Student+Dictionary, demo-seeder
2. **Student** — ученик, уроки (Lesson как часть агрегата), CEFR-уровень
3. **Dictionary** — словарь ученика, WordInfo агрегат (слово + переводы + POS + статус)
4. **Exercise** — генерация/проверка упражнений (Spring AI), каталог грамматических тем
5. **Generator** — рендеринг упражнений в PDF (student sheet + teacher answers)
6. **Chatbot** — Telegram бот, стейт-машина, команды, view-слой
7. **Shared** — общие контракты: Specification, DomainException, SnapshotProvider
8. **Debug** — временные REST-эндпоинты (удалить при переходе на JPA/PostgreSQL)

### Features
1. Учитель регистрируется в боте (сохраняется chatId и имя учителя).
2. Учитель может добавлять к себе уникальных учеников.
3. Учитель может просмотреть список всех своих учеников.
4. Учитель может просмотреть детали ученика (выбор ученика через клик по кнопке).
5. Учитель может начать урок для конкретного ученика (выбор ученика через клик по кнопке).
6. Учитель в рамках начатого урока добавляет слова ученику.
7. Учитель завершает урок, в ответ получает список слов добавленных в рамках урока.
8. Учитель вне урока генерирует упражнения для ученика: тип (fill-in-the-blank / multiple choice) →
грамматическая тема (каталог/поиск/своя тема) → выбор слов из словаря → генерация + PDF.

### ChatBot commands && callbacks

#### Текстовые команды

| # | Команда | Описание |
|---|---------|----------|
| 1 | `/start` | Старт / приветствие |
| 2 | `/register` | Регистрация учителя (ввод имени) |
| 3 | `/new` | Добавление нового ученика (ошибка по уникальности имени) |
| 4 | `/list` | Список учеников в виде inline-кнопок |
| 5 | `/find` | Поиск ученика по имени |
| 6 | `/finishlesson` | Завершить активный урок |
| 7 | `/cancel` | Отмена текущего флоу, возврат в главное меню |
| 8 | `/help` | Описание бота, список доступных команд |

#### Callback'и (inline-кнопки)

Callback data имеет формат `prefix:payload`. Основные префиксы (`Callbacks.java`):

| Префикс | Назначение |
|---------|-----------|
| `student` | Выбор ученика → меню действий |
| `action` | Действие в меню ученика: startlesson / details / dictionary / exercise / back |
| `pos` / `poschange` | Выбор части речи (noun / verb / adjective / expression) |
| `level` / `levelchange` | CEFR-уровень (A1–C2) / изменение уровня |
| `studentdel` / `studentdelok` | Подтверждение удаления ученика |
| `translation` | Выбор AI-перевода или «свой перевод» |
| `topic` / `topicnav` / `topicsearch` / `topicpg` | Навигация по каталогу грам.тем (дерево/поиск/пагинация) |
| `topicself` / `topicconfirm` / `topiccatalog` / `topicskip` | «Своя тема», подтверждение, возврат в каталог, пропуск |
| `exword` / `exfilter` / `expg` / `exdone` / `excont` / `exback` / `exselback` | Выбор слов для упражнения (toggle/фильтр/пагинация/готово) |
| `wdpg` / `wdreset` / `wdback` | Словарь: пагинация, сброс прогресса, назад |
| `back` / `prev` / `next` / `main` | Универсальная навигация |