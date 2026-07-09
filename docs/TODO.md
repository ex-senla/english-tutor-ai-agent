# TODO — v1.0.0

## Фичи релиза — бот

| # | Фича | Статус | Как вызвать | Заметки |
|---|------|--------|-------------|---------|
| 1 | **Регистрация учителя** | ✅ done | `/register` → ввод имени | |
| 2 | **Добавление учеников** | ✅ done | кнопка `➕ Новый студент` или `/new` | Создаётся Student + Dictionary |
| 3 | **Список учеников** | ✅ done | кнопка `👥 Мои студенты` или `/list` | Inline-кнопки, редактирование сообщения |
| 4 | **Детали ученика** | ❌ stub | кнопка `📋 Details` | Показывает "Детали ученика (TODO)" |
| 5 | **Начало урока** | ✅ done | кнопка `▶ Start Lesson` | Создаёт Lesson, сохраняет в InMemoryLessonRepository |
| 6 | **Добавление слов на уроке** | ✅ done | кнопка `➕ Добавить слово` → слово → POS → переводы | 3 шага: AWAITING_WORD → AWAITING_POS → AWAITING_TRANSLATION |
| 7 | **Завершение урока** | ✅ done | кнопка `🏁 Завершить урок` | EndLesson, возврат в STUDENT_OPTIONS |
| 8 | **Генерация упражнений** | ❌ stub | кнопка `🎯 Exercise` | Выбор типа → ввод темы (stub), ответ (stub) |
| 9 | **Обработка дубляжа слова** | ❌ | добавление слова, которое уже есть в словаре | Сейчас бросает исключение, нужно переиспользовать существующее

## Chatbot — реализация v2

| # | Компонент | Статус | Заметки |
|---|-----------|--------|---------|
| C1 | State machine: 14 состояний | ✅ done | Enum State, все 14 |
| C2 | StateMachine entity + repository | ✅ done | StateMachineId, InMemoryStateMachineRepository |
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
| I1 | JPA entities + репозитории | ❌ | Замена всех InMemory*Repository |
| I2 | PostgreSQL + Flyway миграции | ❌ | |
| I3 | Spring AI интеграция | ❌ | Замена скелета SpringAiExerciseGenerator на реальный LLM |
| I4 | `/debug` endpoint | ✅ done | DebugController, SnapshotProvider |

## Технический долг

| # | Задача | Статус |
|---|--------|--------|
| D1 | `EtaApplicationTests` — восстановить | ❌ |
| D2 | InMemoryDictionaryRepository → JPA | ❌ |
| D3 | InMemoryStudentRepository → JPA | ❌ |
| D4 | InMemoryTeacherRepository → JPA | ❌ |
| D5 | InMemoryLessonRepository → JPA | ❌ |
| D6 | InMemoryExerciseRepository → JPA | ❌ |
| D7 | InMemoryStateMachineRepository → persistent | ❌ |
| D8 | Команды `/new` и `/list` вместо `/newstudent` и `/students` | ✅ done |

## История (done)

- [x] Запуск приложения — Spring Modulith 1.1.10 → 2.1.0
- [x] `ModuleVerificationTest.verifyModularity` — исправлено
- [x] Миграция teacher → `api/teacher/`, `domain/teacher/`
- [x] Миграция exercise → `api/exercise/`, `domain/exercise/`
- [x] Student — factory + validation
- [x] Teacher — CreateStudentWithDictionary (оркестрация Dictionary → Student)
- [x] Lesson — Entity внутри агрегата Student
- [x] Exercise — GenerateExercise + CheckExercise API/use cases
- [x] Старый модуль chatbot удалён. Дизайн v2: state machine, 14 состояний
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
