# Chatbot State Machine

## Описание состояний

| # | Состояние | Назначение | Как попадаем | Как выходим |
|---|-----------|------------|--------------|-------------|
| S1 | `INITIAL` | Пользователь открыл бота, но не зарегистрирован | Автоматически при первом входе | `/register` → S2 |
| S2 | `AWAITING_REGISTRATION_NAME` | Ожидание имени учителя | Из S1 по `/register` | Ввод имени → S3 |
| S3 | `ACTIVE` | Зарегистрирован, без активного урока. Idle-состояние | Из S2, S4, S8, S14 | `/newstudent` → S4, `/students` → S5 |
| S4 | `AWAITING_STUDENT_NAME` | Ожидание имени нового ученика | Из S3 или S5 по `/newstudent` | Ввод имени → S3 |
| S5 | `STUDENTS_LIST` | Inline-кнопки со списком учеников | Из S3 по `/students`, из S6 по `action:back` | `student:<id>` → S6, `/newstudent` → S4 |
| S6 | `STUDENT_OPTIONS` | Меню действий для выбранного ученика | Из S5 по `student:<id>`, из S7 по `details:back` | `action:*` → S7/S8/S12/S5 |
| S7 | `STUDENT_DETAILS` | Карточка ученика (имя, статистика словаря, статус урока) | Из S6 по `action:details` | `details:back` → S6 |
| S8 | `IN_LESSON` | Активный урок для выбранного ученика | Из S6 по `action:startlesson` | `/addword` → S9, `/finishlesson` → S3 |
| S9 | `AWAITING_WORD` | Шаг 1: ожидание английского слова | Из S8 по `/addword` | Ввод слова → S10 |
| S10 | `AWAITING_POS` | Шаг 2: выбор части речи | Из S9 | `NOUN`/`VERB`/`ADJECTIVE` → S11 |
| S11 | `AWAITING_TRANSLATION` | Шаг 3: ввод перевода | Из S10 | Ввод перевода → S8 |
| S12 | `AWAITING_EXERCISE_TYPE` | Выбор типа упражнения (inline-кнопки) | Из S6 по `action:exercise` | `exercise:*` → S13 |
| S13 | `AWAITING_EXERCISE_TOPIC` | Ввод темы упражнения | Из S12 | Ввод темы → S14 |
| S14 | `AWAITING_EXERCISE_ANSWER` | Ожидание ответа на упражнение | Из S13 | Ввод ответа → S3 |

## Конфиг состояний

Каждое состояние определяет, какие типы Update разрешены, и действие для каждого.  
**Типы Update**: команда (начинается с `/`), параметр (текст без `/`), callback (inline-кнопка).  
В каждом состоянии **только один тип ввода является целевым**, остальные — ошибка или `/help`.

| # | Состояние | Команды | Параметр | Callback |
|---|-----------|---------|----------|----------|
| S1 | `INITIAL` | `/register` → S2, `/help` → S1 | ❌ ошибка | ❌ ошибка |
| S2 | `AWAITING_REGISTRATION_NAME` | `/help` → S2 | ✅ `RegisterTeacher` → S3 | ❌ ошибка |
| S3 | `ACTIVE` | `/newstudent` → S4, `/students` → S5, `/help` → S3 | ❌ ошибка | ❌ ошибка |
| S4 | `AWAITING_STUDENT_NAME` | `/help` → S4 | ✅ `CreateStudentWithDictionary` → S3 | ❌ ошибка |
| S5 | `STUDENTS_LIST` | `/newstudent` → S4, `/students` → S5, `/help` → S5 | ❌ ошибка | ✅ `student:<id>` → S6 |
| S6 | `STUDENT_OPTIONS` | `/help` → S6 | ❌ ошибка | ✅ `action:startlesson` → S8, `action:details` → S7, `action:exercise` → S12, `action:back` → S5 |
| S7 | `STUDENT_DETAILS` | `/help` → S7 | ❌ ошибка | ✅ `details:back` → S6 |
| S8 | `IN_LESSON` | `/addword` → S9, `/finishlesson` → S3, `/help` → S8 | ❌ ошибка | ❌ ошибка |
| S9 | `AWAITING_WORD` | `/help` → S9 | ✅ сохранить `word` → S10 | ❌ ошибка |
| S10 | `AWAITING_POS` | `/help` → S10 | ✅ `NOUN`/`VERB`/`ADJECTIVE` → S11 | ❌ ошибка |
| S11 | `AWAITING_TRANSLATION` | `/help` → S11 | ✅ `AddWordToDict` + `AddWordToLesson` → S8 | ❌ ошибка |
| S12 | `AWAITING_EXERCISE_TYPE` | `/help` → S12 | ❌ ошибка | ✅ `exercise:FILL_IN_THE_BLANK`, `exercise:MULTIPLE_CHOICE` → S13 |
| S13 | `AWAITING_EXERCISE_TOPIC` | `/help` → S13 | ✅ `GenerateExercise` → S14 | ❌ ошибка |
| S14 | `AWAITING_EXERCISE_ANSWER` | `/help` → S14 | ✅ `CheckExercise` → S3 | ❌ ошибка |

### Тексты ошибок

| Состояние | Ошибка: не та команда | Ошибка: не тот параметр | Ошибка: не тот callback |
|-----------|----------------------|------------------------|------------------------|
| S1 | — (все команды обработаны) | "/register для начала" | `answerCallbackQuery("Неактуально")` |
| S2 | "Введите ваше имя" | — (параметр разрешён) | `answerCallbackQuery("Неактуально")` |
| S3 | "Неизвестная команда. /help" | "/help для списка команд" | `answerCallbackQuery("Неактуально")` |
| S4 | "Введите имя ученика" | — (параметр разрешён) | `answerCallbackQuery("Неактуально")` |
| S5 | — | "Выберите ученика кнопками ниже" | — |
| S6 | — | "Используйте кнопки ниже" | — |
| S7 | — | "Используйте кнопки ниже" | — |
| S8 | "Идёт урок. /addword или /finishlesson" | "/addword или /finishlesson" | `answerCallbackQuery("Неактуально")` |
| S9 | "Введите английское слово" | — | `answerCallbackQuery("Неактуально")` |
| S10 | "Выберите часть речи: NOUN, VERB, ADJECTIVE" | "Выберите: NOUN, VERB или ADJECTIVE" | `answerCallbackQuery("Неактуально")` |
| S11 | "Введите перевод" | — | `answerCallbackQuery("Неактуально")` |
| S12 | — | "Выберите тип упражнения кнопками ниже" | — |
| S13 | "Введите тему упражнения" | — | `answerCallbackQuery("Неактуально")` |
| S14 | "Введите ответ" | — | `answerCallbackQuery("Неактуально")` |

## Принцип работы

### Обработка Update

```
process(update):
    sm = repo.findByChatId(chatId)
    config = STATE_CONFIG[sm.state]
    
    тип = определить_тип(update)   // команда | параметр | callback
    
    if config.разрешён(тип):
        выполнить_действие(config, тип, update)
        sm.state = config.новое_состояние(тип)
        обновить_контекст(sm, config, update)
        repo.save(sm)
        return render(sm.state)
    else:
        return renderError(sm.state, тип)
```

### Формирование ответа

Ответ всегда рендерится из **текущего** состояния машины после обработки:

- **Валидный ввод**: действие выполнено → состояние изменилось → `render(новое_состояние)`
- **Невалидный ввод**: состояние не меняется → `renderError(текущее_состояние)`

Каждое состояние предоставляет `render()` — текст + клавиатуру (если нужна):

| Состояние | render() |
|-----------|----------|
| S1 `INITIAL` | "Добро пожаловать! Для начала зарегистрируйтесь: /register" |
| S2 `AWAITING_REGISTRATION_NAME` | "Введите ваше имя" |
| S3 `ACTIVE` | "Главное меню. /newstudent — добавить ученика, /students — список учеников" |
| S4 `AWAITING_STUDENT_NAME` | "Введите имя нового ученика" |
| S5 `STUDENTS_LIST` | "Ваши ученики:" + inline-кнопки с именами |
| S6 `STUDENT_OPTIONS` | "Ученик: {name}" + inline-кнопки: [Start Lesson] [Details] [Exercise] [◀ Back] |
| S7 `STUDENT_DETAILS` | Карточка: имя, статистика словаря, статус урока + кнопка [◀ Back] |
| S8 `IN_LESSON` | "Урок с {name}. /addword — добавить слово, /finishlesson — завершить" |
| S9 `AWAITING_WORD` | "Введите английское слово" |
| S10 `AWAITING_POS` | "Выберите часть речи:" + inline-кнопки [NOUN] [VERB] [ADJECTIVE] |
| S11 `AWAITING_TRANSLATION` | "Введите перевод слова '{word}'" |
| S12 `AWAITING_EXERCISE_TYPE` | "Выберите тип упражнения:" + кнопки [Fill in the blank] [Multiple choice] |
| S13 `AWAITING_EXERCISE_TOPIC` | "Введите тему упражнения (например, 'Animals')" |
| S14 `AWAITING_EXERCISE_ANSWER` | Текст упражнения + "Введите ваш ответ:" |

## Граф переходов

```
S1: INITIAL
 │
 │ /register
 ▼
S2: AWAITING_REGISTRATION_NAME
 │
 │ текст (имя)
 ▼
S3: ACTIVE ──────────────────────────────────────────────────────────────┐
 │                                                                       │
 ├── /newstudent ──► S4: AWAITING_STUDENT_NAME ──(текст)──► S3           │
 │                                                                       │
 ├── /students ──► S5: STUDENTS_LIST                                     │
 │                    │                                                  │
 │                    │ callback: student:<id>                            │
 │                    ▼                                                  │
 │                 S6: STUDENT_OPTIONS                                   │
 │                    │                                                  │
 │                    ├── action:startlesson ──► S8: IN_LESSON            │
 │                    │      │                                           │
 │                    │      ├── /addword ──► S9 ──► S10 ──► S11         │
 │                    │      │                  ▲          │             │
 │                    │      │                  └──────────┘             │
 │                    │      │                                           │
 │                    │      └── /finishlesson ──► S3                    │
 │                    │                                                  │
 │                    ├── action:details ──► S7: STUDENT_DETAILS         │
 │                    │      │                                           │
 │                    │      └── details:back ──► S6                     │
 │                    │                                                  │
 │                    ├── action:exercise ──► S12 ──► S13 ──► S14        │
 │                    │                                      │           │
 │                    │                              (ответ)──┘           │
 │                    │                                                  │
 │                    └── action:back ──► S5                             │
 │                                                                       │
```

## Контекст состояния (Map<String, Object>)

| Состояние | Ключи |
|-----------|-------|
| S3 `ACTIVE` | `chatId`, `teacherId`, `teacherName` |
| S5 `STUDENTS_LIST` | + `messageId` (для редактирования клавиатуры) |
| S6 `STUDENT_OPTIONS` | + `selectedStudentId`, `selectedDictionaryId`, `selectedStudentName` |
| S7 `STUDENT_DETAILS` | те же |
| S8 `IN_LESSON` | + `lessonId` |
| S9 `AWAITING_WORD` | `lessonId`, `selectedDictionaryId` |
| S10 `AWAITING_POS` | + `word` |
| S11 `AWAITING_TRANSLATION` | + `word`, `pos` |
| S12 `AWAITING_EXERCISE_TYPE` | `selectedStudentId`, `selectedDictionaryId` |
| S13 `AWAITING_EXERCISE_TOPIC` | + `type` |
| S14 `AWAITING_EXERCISE_ANSWER` | + `exerciseId` |

## Inline-кнопки и callback data

| Экран | Кнопки | Callback data |
|-------|--------|---------------|
| S5: список учеников | `[Alice]` `[Bob]` ... | `student:<uuid>` |
| S6: меню ученика | `[Start Lesson]` `[Details]` `[Exercise]` `[◀ Back]` | `action:startlesson`, `action:details`, `action:exercise`, `action:back` |
| S7: детали ученика | `[◀ Back]` | `details:back` |
| S10: часть речи | `[NOUN]` `[VERB]` `[ADJECTIVE]` | (обычные кнопки, не inline) |
| S12: тип упражнения | `[Fill in the blank]` `[Multiple choice]` | `exercise:FILL_IN_THE_BLANK`, `exercise:MULTIPLE_CHOICE` |

## Структура модуля

```
chatbot/
├── domain/
│   └── statemachine/
│       ├── State.java                 # Enum: все 14 состояний
│       ├── StateConfig.java           # Конфиг состояния: разрешённые команды/параметр/КБ + действия
│       ├── StateMachine.java          # Агрегат: id, chatId, state, context
│       └── StateMachineRepository.java
├── application/
│   ├── config/
│   │   └── ChatbotModuleConfig.java
│   └── service/
│       └── StateMachineService.java   # process(update) → определить тип → делегировать
├── infrastructure/
│   ├── bot/
│   │   └── EnglishTutorBot.java       # Тонкий: onUpdateReceived → StateMachineService
│   └── persistence/
│       └── InMemoryStateMachineRepository.java
└── package-info.java
```

## Зависимости модуля

```java
@ApplicationModule(allowedDependencies = {
    "teacher :: teacher",
    "student :: student",
    "student :: lesson",
    "dictionary :: dictionary",
    "dictionary :: word",
    "exercise :: exercise"
})
```
