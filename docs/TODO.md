# TODO — v1.0.0

## Фичи релиза

| # | Фича | Статус | Команды бота | Что сделано / осталось |
|---|------|--------|-------------|----------------------|
| 1 | **Регистрация учителя** | ✅ | `/start`, `/register` | StartCmd (приветствие), RegisterCmd (chatId + имя) |
| 2 | **Добавление учеников** | ✅ | `/newstudent` | NewStudentCmd → CreateStudentWithDictionary (оркестрация Dictionary + Student) |
| 3 | **Список учеников** | ✅ | `/students` | StudentsCmd + inline-кнопки с именами учеников |
| 4 | **Детали ученика** | ✅ | click по кнопке | Inline-кнопка → callback → FindTeacher + StudentQuery → карточка: имя, статистика словаря, статус урока |
| 5 | **Начало урока** | ✅ | `/startlesson` | StartLessonCmd → StartLesson. Но выбор ученика — первый из списка, а не через кнопку ⚠️ |
| 6 | **Добавление слов на уроке** | ✅ | `/addword` | AddWordCmd: трёхшаговый ввод (word → POS → translation). Слово идёт и в Dictionary, и в Lesson |
| 7 | **Завершение урока** | ✅ | `/endlesson` | EndLessonCmd → EndLesson. Выводит список слов урока ⚠️ (проверить вывод) |
| 8 | **Генерация упражнений** | ⚠️ | `/exercise` | ExerciseCmd: `/exercise <TYPE> <topic>` → генерация → проверка ответа. Нюансы: (а) без Spring AI — скелет, (б) не фильтрует слова по `IN_PROGRESS`, (в) 4 типа в коде, roadmap просит 2: FILL_IN_THE_BLANK + MULTIPLE_CHOICE |

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
| D8 | Рефакторинг команд чат-бота (расширяемость) | ❌ |
| D9 | Выбор ученика через кнопки для startlesson | ❌ | Сейчас берёт первого. Нужно переделать на inline-кнопки выбора |

## История (done)

- [x] Запуск приложения — Spring Modulith 1.1.10 → 2.1.0
- [x] `ModuleVerificationTest.verifyModularity` — исправлено
- [x] Миграция teacher → `api/teacher/`, `domain/teacher/`
- [x] Миграция exercise → `api/exercise/`, `domain/exercise/`
- [x] Student — factory + validation
- [x] Teacher — CreateStudentWithDictionary (оркестрация Dictionary → Student)
- [x] Lesson — Entity внутри агрегата Student
- [x] Exercise — GenerateExercise + CheckExercise API/use cases, ExerciseType (4 типа), ExerciseStatus (3 статуса)
- [x] StateMachine: pendingCommand, Context key-value, CommandDispatcher, двухфазный/трёхшаговый ввод, Result.stay/transition
- [x] ExerciseCmd: двухфазная работа (генерация → проверка ответа)
