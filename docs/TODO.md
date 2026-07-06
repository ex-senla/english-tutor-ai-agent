# TODO — v1.0.0

## Фичи релиза

| # | Фича | Статус | Команды бота | Что сделано / осталось |
|---|------|--------|-------------|----------------------|
| 1 | **Регистрация учителя** | ❌ | `/register` | Переписывается в новом chatbot. Старый код удалён. |
| 2 | **Добавление учеников** | ❌ | `/newstudent` | Переписывается. |
| 3 | **Список учеников** | ❌ | `/students` | Inline-кнопки с именами учеников → S5 STUDENTS_LIST. |
| 4 | **Детали ученика** | ❌ | кнопка Details | S7 STUDENT_DETAILS: карточка (имя, статистика словаря, статус урока) + кнопка Back. |
| 5 | **Начало урока** | ❌ | кнопка Start Lesson | Выбор ученика через S5 → S6 → `action:startlesson` → S8 IN_LESSON. |
| 6 | **Добавление слов на уроке** | ❌ | `/addword` | Трёхшаговый ввод: S9 AWAITING_WORD → S10 AWAITING_POS → S11 AWAITING_TRANSLATION. |
| 7 | **Завершение урока** | ❌ | `/finishlesson` | EndLesson → список слов урока → S3 ACTIVE. |
| 8 | **Генерация упражнений** | ❌ | кнопка Exercise | S12 тип → S13 тема → S14 ответ. 2 типа: FILL_IN_THE_BLANK + MULTIPLE_CHOICE. |

## Chatbot — новый дизайн (v2)

| # | Компонент | Статус | Заметки |
|---|-----------|--------|---------|
| C1 | State machine: 14 состояний | ❌ | Документация: [chat-bot-state-machine.MD](chat-bot-state-machine.MD) |
| C2 | `StateMachineService.process(update)` | ❌ | Определить тип Update → конфиг состояния → действие → render |
| C3 | `EnglishTutorBot` | ❌ | Тонкий: onUpdateReceived → StateMachineService |
| C4 | `StateConfig` — конфиг на каждое состояние | ❌ | Разрешённые команды/параметр/КБ + действия + новые состояния |
| C5 | `InMemoryStateMachineRepository` | ❌ | Хранение StateMachine по chatId |

## Инфраструктура

| # | Задача | Статус | Заметки |
|---|--------|--------|---------|
| I1 | JPA entities + репозитории | ❌ | Замена всех InMemory*Repository |
| I2 | PostgreSQL + Flyway миграции | ❌ | |
| I3 | Spring AI интеграция | ❌ | Замена скелета SpringAiExerciseGenerator на реальный LLM |

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

## История (done)

- [x] Запуск приложения — Spring Modulith 1.1.10 → 2.1.0
- [x] `ModuleVerificationTest.verifyModularity` — исправлено
- [x] Миграция teacher → `api/teacher/`, `domain/teacher/`
- [x] Миграция exercise → `api/exercise/`, `domain/exercise/`
- [x] Student — factory + validation
- [x] Teacher — CreateStudentWithDictionary (оркестрация Dictionary → Student)
- [x] Lesson — Entity внутри агрегата Student
- [x] Exercise — GenerateExercise + CheckExercise API/use cases
- [x] Старый модуль chatbot удалён. Дизайн v2: state machine, 14 состояний, конфиг-ориентированный
- [x] Roadmap обновлён: таблица команд и callback'ов
- [x] Документация chat-bot-state-machine.MD: описание состояний, конфиг, render, граф переходов
