# Chatbot

Модуль стейт-машины чата. Платформонезависимый — Telegram подключается через инфраструктуру.

## Составляющие

### Action (Domain, `domain/action`)
Sealed-иерархия действий — единственная абстракция «что пришло от пользователя»:
- `Command(String command)` — команда (`/new`, `/list`, ...) — определяется по `BOT_COMMAND` entity
- `Input(String text)` — свободный текст (смысл задаётся состоянием)
- `Callback(String prefix, String payload, int messageId)` — inline-кнопка; `prefix`/`payload`
  разделены парсером (`"student:<uuid>"` → `student` + `<uuid>`)
- `Button(String command)` — reply-кнопка; текст совпал со словарём `item/Buttons`

### ActionResult (Domain, `domain/action`)
Sealed-иерархия ответов: `TextResponse`, `TextWithInlineKeyboard`, `TextWithReplyKeyboard`,
`EditMessageText`, `DeleteMessage`.

### Chat (Domain Entity, `domain/chat`)
- `ChatId id` — record с `Long chatId`
- `ChatState state` — текущее состояние
- `Map<String, Object> context` — key-value хранилище (`selectedStudentId`, `selectedStudentName`,
  `teacherName`, `wordValue`, `wordPos`, `exerciseType`, `exerciseId`, `activeLessonId`, ...)
- `ofDefaults(ChatId)` — фабрика, начальное состояние `INITIAL`

### ChatState (Enum)
14 состояний: `INITIAL`, `AWAITING_REGISTRATION_NAME`, `ACTIVE`, `AWAITING_STUDENT_NAME`,
`STUDENTS_LIST`, `STUDENT_OPTIONS`, `STUDENT_DETAILS`, `IN_LESSON`, `AWAITING_WORD`,
`AWAITING_POS`, `AWAITING_TRANSLATION`, `AWAITING_EXERCISE_TYPE`, `AWAITING_EXERCISE_TOPIC`,
`AWAITING_EXERCISE_ANSWER`.

### StateMachine (Domain Service, `domain/statemachine`)
`applyAction(Chat, Action)` — маршрутизация по типу действия:
- `Command` → `TransitionKey(state, command)` из `transitions`
- `Button` → `TransitionKey(state, button.command())` из `transitions`
- `Callback` → `TransitionKey(state, callback.prefix())` из `transitions`
- `Input` → `inputTransitions.get(state)` (свободный текст резолвится только состоянием)

Если переход не найден — дефолтный `Handler` состояния. После перехода чат сохраняется
через `ChatService`.

### Transition (Domain, `domain/statemachine/transition`)
`Transition<T>` — обработчик одного (состояние, триггер): `transit(Chat, T)` + `getName()`.
Типизация по триггеру: `Transition<Action.Command|Input|Callback|Button>`; если логика не
зависит от триггера — `Transition<Action>`.

Именование: `<Что><Триггер><Состояние>Transition`, триггеры: `Cmd` / `Inp` / `Cb` / `Btn`.
Например `NewCmdActiveTransition`, `MyStudentsBtnActiveTransition`, `PosCbAwaitingPosTransition`.

Пакеты по состояниям (`active/`, `initial/`, `studentslist/`, ...). `ActiveTransition` —
референсный монолитный вариант до рефакторинга.

### Handler (Domain, `domain/statemachine/handler`)
Дефолтная реакция состояния, когда переход не найден («Введите имя ученика»,
«Используйте кнопки ниже», ...). Один `@Component` на состояние, собирается в
`Map<ChatState, Handler>`.

### StateMachineConfig (`domain/statemachine`)
Собирает машину:
- каждый переход объявляется `@Bean` (типизированные приводятся к `Transition<Action>` через `cast`)
- `nameTransitionMap` — реестр `getName() -> Transition`
- таблица регистрации: `addTransition(state, ключ, transition)` для Cmd/Btn/Cb,
  `addInputTransition(state, transition)` для Input
- ключи Cmd/Btn — текст (`"/new"`, `Buttons.ADD_STUDENT.getValue()`), ключ Cb — префикс (`"student"`, `"action"`, `"pos"`)

### Items (презентация, `item/`)
Статические билдеры `ActionResult`: `MenuItem` (главное меню), `StudentItem` (список/опции
учеников, `optionsKeyboard()`), `WordItem` (`posMenu`, `posLabel`), `LessonItem`
(клавиатура урока, сводка завершения), `ExerciseItem` (выбор типа упражнения). `Buttons` —
enum reply-кнопок, единый словарь для рендера и парсинга.

## Application Layer

- `ChatService` — загрузка/сохранение `Chat` через `ChatRepository`
- `FlowType` — enum флоу (заготовка)

## Инфраструктура (`infrastructure/bot`)
- `UpdateParser` — `Update` → `Action`: callback-данные делятся на `prefix`/`payload` по первому `:`;
  текст матчится со словарём `Buttons` → `Action.Button`, иначе `Action.Input`
- `EnglishTutorBot` — `TelegramLongPollingBot`: chatId → `ChatService.getOrCreate` →
  `UpdateParser` → `StateMachine.applyAction` → `SendMessageConverter` → send/edit/delete;
  удаляет reply-клавиатуру при выходе из `ACTIVE`/`IN_LESSON`
- `BotInitializer`, `SendMessageConverter`

Персистентность: `InMemoryChatRepository` (план: PostgreSQL + Flyway).

## Зависимости
```java
@ApplicationModule(allowedDependencies = {
    "teacher :: teacher", "student :: student",
    "student :: lesson", "dictionary :: dictionary", "dictionary :: word", "exercise"
})
```

## Известные долги
- `StateMachineConfig` и `Handler`-имплементации лежат в domain-пакете с Spring-аннотациями —
  кандидат на переезд в application
- `StateMachine` зависит от `ChatService` (application) — инверсия слоёв
- Поиск имени ученика: загрузка всех студентов учителя и фильтрация в Java (нужен `FindStudentById`)
- Контекст чата stringly-typed (`Map<String, Object>` + касты)
- Ошибки `IllegalStateException` без обработки на уровне бота
