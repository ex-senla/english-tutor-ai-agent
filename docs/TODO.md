# TODO — v1.0.0

> Open issues (single source of truth): https://github.com/ex-senla/english-tutor-ai-agent/issues

## Фичи релиза — бот

| # | Фича | Статус | Как вызвать | Заметки |
|---|------|--------|-------------|---------|
| 1 | **Регистрация учителя** | ✅ done | `/register` → ввод имени | |
| 2 | **Добавление учеников** | ✅ done | кнопка `➕ Новый студент` или `/new` | Создаётся Student + Dictionary |
| 3 | **Список учеников** | ✅ done | кнопка `👥 Мои студенты` или `/list` | Inline-кнопки, редактирование сообщения |
| 4 | **Детали ученика** | ✅ done | кнопка `📋 Details` | Имя, CEFR-уровень, статистика словаря, признак активного урока; кнопки «Изменить уровень» / «Удалить» / «Назад» |
| 5 | **Начало урока** | ✅ done | кнопка `▶ Start Lesson` | Создаёт Lesson, сохраняет в PostgreSQL (LessonJpa) |
| 6 | **Добавление слов на уроке** | ✅ done | в `IN_LESSON` просто отправьте слово → POS → AI-переводы/свой перевод | `IN_LESSON` — режим ввода; кнопка «➕ Добавить слово» и `/addword` удалены (#42) |
| 7 | **Завершение урока** | ✅ done | кнопка `🏁 Завершить урок` | EndLesson, возврат в STUDENT_OPTIONS |
| 8 | **Генерация упражнений** | ✅ done | кнопка `🎯 Exercise` | Выбор типа → выбор грамматической темы (дерево/поиск/«Своя тема») → выбор слов → генерация + PDF |
| 9 | **Обработка дубляжа слова** | 🟡 частично | добавление слова, которое уже есть в словаре | Бот ловит `WordAlreadyExistsException` и показывает «Слово уже есть в словаре» + переводы, но НЕ переиспользует/обновляет существующее слово |
| 10 | **Админка (#44)** | ✅ done | `/admin` или кнопка `🛡 Админка` (только админам) | Список учителей, ученики выбранного учителя, имперсонация «войти как учитель», выход по `/cancel`/`/start` |
| 11 | **Обратная связь (#45)** | ✅ done | `/support` или ЛК → Поддержка | Один OPEN-тред на учителя, история TEACHER/ADMIN, ответ админа с push-доставкой в чат учителя |
| 12 | **Личный кабинет учителя (#48)** | ✅ done | кнопка `👤 Личный кабинет` | Профиль (имя + активные ученики), Поддержка, заглушка подписок |

## Chatbot — реализация v2

| # | Компонент | Статус | Заметки |
|---|-----------|--------|---------|
| C1 | State machine: 30 состояний | ✅ done | Enum State, все 30 |
| C2 | Chat entity + repository | ✅ done | ChatId, ChatState, context (jsonb) — JPA (`ChatJpa`/`JpaChatRepository`) |
| C3 | `EnglishTutorBot` | ✅ done | Обработка SendMessage / EditMessageText / DeleteMessage / ReplyKeyboardRemove |
| C4 | `ActionHandler` | ✅ done | Центральный обработчик, switch по состояниям |
| C5 | `Action` / `ActionResult` sealed types | ✅ done | Command, InputParam, Callback; TextResponse, TextWithInlineKeyboard, TextWithReplyKeyboard, EditMessageText, DeleteMessage |
| C6 | `UpdateParser` | ✅ done | Парсинг Update → Action |
| C7 | `SendMessageConverter` | ✅ done | Конвертация ActionResult → Telegram API objects |
| C8 | Reply-клавиатура | ✅ done | Главное меню: `➕ Новый студент` / `👥 Мои студенты`. Убирается при уходе из ACTIVE |
| C9 | Inline-клавиатуры + редактирование | ✅ done | EditMessageText при навигации по меню учеников |

## Инфраструктура

| # | Задача | Статус | Заметки |
|---|--------|--------|---------|
| I1 | JPA entities + репозитории | ✅ done | Все `InMemory*Repository` заменены JPA-адаптерами (`*Jpa` + `*DataRepository` + `Jpa*Repository`) |
| I2 | PostgreSQL + Flyway миграции | ✅ done | `V1__init.sql` (12 таблиц), `ddl-auto: validate`, Testcontainers |
| I3 | Spring AI интеграция | ✅ done | Реальные LLM-вызовы: `SpringAiExerciseGenerator` (генерация), `SpringAiTopicValidator` (валидация темы), `SpringAiWordTranslator` (POS+переводы), все с fallback |
| I4 | `/debug` endpoint | ✅ done | Удалён вместе с `DebugController`/`SnapshotProvider` при переходе на JPA |
| I5 | E2E UI-тесты бота | 🟡 частично | #50 часть 1: in-process `BotTester` + 6 E2E-классов (регистрация, ученики, /start-сброс, урок, генерация упражнения, smoke). Часть 2 (Cucumber/UI-транспорт) — отложена |
| I6 | Релизная политика + тест-пайплайн (#46) | 🟡 in progress | `docs/release-policy.md`, `Dockerfile`, `.dockerignore`, `docker-compose.test.yml`, `.env.test.example`, `.github/workflows/test-deploy.yml`, `telegram.bot.username` env-дефолт |

## Технический долг

| # | Задача | Статус |
|---|--------|--------|
| D1 | `EtaApplicationTests` — восстановить | ✅ done | Smoke `@SpringBootTest` + Testcontainers |
| D2 | InMemoryDictionaryRepository → JPA | ✅ done | `JpaDictionaryRepository` |
| D3 | InMemoryStudentRepository → JPA | ✅ done | `JpaStudentRepository` (+ `findAllActiveByIds`) |
| D4 | InMemoryTeacherRepository → JPA | ✅ done | `JpaTeacherRepository` |
| D5 | InMemoryLessonRepository → JPA | ✅ done | `JpaLessonRepository` |
| D6 | InMemoryExerciseRepository → JPA | ✅ done | `JpaExerciseRepository` |
| D7 | InMemoryStateMachineRepository → persistent | ✅ done | `JpaChatRepository` (Chat = персистентный контекст стейт-машины) |
| D8 | Команды `/new` и `/list` вместо `/newstudent` и `/students` | ✅ done |
| D9 | Уровень CEFR как входной параметр генерации упражнений (сейчас захардкожен `A2` в `SpringAiExerciseGenerator`) | ✅ done |

## История (done)

- [x] Запуск приложения — Spring Modulith 1.1.10 → 2.1.0
- [x] `ModuleVerificationTest.verifyModularity` — исправлено
- [x] Миграция teacher → `api/teacher/`, `domain/teacher/`
- [x] Миграция exercise → `api/exercise/`, `domain/exercise/`
- [x] Student — factory + validation
- [x] Teacher — CreateStudentWithDictionary (оркестрация Dictionary → Student)
- [x] Lesson — Entity внутри агрегата Student
- [x] Exercise — GenerateExercise + CheckExercise API/use cases
- [x] Старый модуль chatbot удалён. Дизайн v2: chatState machine, 14 состояний
- [x] ActionHandler — полная реализация S1-S11, S12-S14 stubs
- [x] EditMessageText — редактирование сообщений при навигации (inline-меню)
- [x] ReplyKeyboardRemove — убирание reply-клавиатуры при уходе из ACTIVE
- [x] Кнопки главного меню: `➕ Новый студент`, `👥 Мои студенты` (без команд на кнопках)
- [x] Кнопки урока: `➕ Добавить слово`, `🏁 Завершить урок` (reply-клавиатура вместо команд)
- [x] Прогрессивный ввод слова: слово → часть речи → переводы (с накоплением контекста)
- [x] Итоги урока: дата, длительность, слова, ученик
- [x] Кнопка «Назад» в списке студентов
- [x] Удаление старых inline-меню (cleanupMessageId)
- [x] WARN-логи для каждой спецификации валидации слов
- [x] #27 Движение слов по статусам (NEW→IN_PROGRESS→LEARNED): инкремент currentRepetitions за реально использованные слова упражнения, `resetCount`, сброс обучения только для LEARNED, меню «Словарь» с пагинацией
- [x] #29 Выбор слов для упражнения: шаг выбора после темы (фильтр по статусу, toggle, пагинация), генерация строго по выбранным `WordInfoId`, алерт при 0 выбранных с fallback-генерацией
- [x] #32 Удаление ученика (soft delete): флаг `deleted` + `markDeleted()`, use case `DeleteStudent` с защитой активного урока, фильтрация удалённых из read-модели, флоу подтверждения в чат-боте и graceful-обработка устаревших `student:<id>`
- [x] #49 Каталог грамматических тем: `GrammarTopicCatalog` (JSON-ресурс), выбор темы (дерево/поиск/«Своя тема»), промпт-фрагменты по slug через `{topicGuidance}`, отображение пути темы в PDF
- [x] #39 LLM-валидация «Своей темы»: `ValidateCustomTopicUseCase`, анти-галлюцинации по активному каталогу, экран подтверждения, fail-open
- [x] #40 One-shot POS+переводы: `PartOfSpeech.EXPRESSION`, `WordTranslator.suggestPosAndTranslations`, экран подтверждения, fallback на ручное меню из 4 кнопок
