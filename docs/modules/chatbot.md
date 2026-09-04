# Chatbot

Модуль стейт-машины чата. Платформонезависимый — Telegram подключается через инфраструктуру.

## Составляющие

### Action (Domain, `domain/action`)
Sealed-иерархия действий — единственная абстракция «что пришло от пользователя»:
- `Command(String command)` — команда (`/new`, `/list`, ...) — определяется по `BOT_COMMAND` entity
- `Input(String text)` — свободный текст (смысл задаётся состоянием)
- `Callback(String prefix, String payload, int messageId)` — inline-кнопка; `prefix`/`payload`
  разделены парсером (`"student:<uuid>"` → `student` + `<uuid>`)
- `Button(String command)` — reply-кнопка; текст совпал со словарём `view/Buttons.REPLY_BUTTONS`

### ActionResult (Domain, `domain/action`)
Sealed-иерархия ответов: `TextResponse`, `TextWithInlineKeyboard`, `TextWithReplyKeyboard`,
`EditMessageText`, `DeleteMessage`.

### Chat (Domain Entity, `domain/chat`)
- `ChatId id` — record с `Long chatId`
- `ChatState state` — текущее состояние
- `Map<String, Object> context` — key-value хранилище (`selectedStudentId`, `selectedStudentName`,
  `teacherName`, `wordValue`, `wordPos`, `exerciseType`, `exerciseId`, `activeLessonId`, ...)
- `ofDefaults(ChatId)` — фабрика, начальное состояние `INITIAL`

### ChatState (Enum, `domain/chat`)
14 состояний: `INITIAL`, `AWAITING_REGISTRATION_NAME`, `ACTIVE`, `AWAITING_STUDENT_NAME`,
`STUDENTS_LIST`, `STUDENT_OPTIONS`, `STUDENT_DETAILS`, `IN_LESSON`, `AWAITING_WORD`,
`AWAITING_POS`, `AWAITING_TRANSLATION`, `AWAITING_EXERCISE_TYPE`, `AWAITING_EXERCISE_TOPIC`,
`AWAITING_EXERCISE_ANSWER`.

### StateMachine (Application, `application/statemachine`)
Чистая маршрутизация без персистенции — `(Chat, Action) → ActionResult`. Четыре таблицы переходов:
- `commandTransitions`: `TransitionKey(state, команда)` → `Transition<? super Action.Command>`
- `buttonTransitions`: `TransitionKey(state, текст кнопки)` → `Transition<? super Action.Button>`
- `callbackTransitions`: `TransitionKey(state, префикс колбэка)` → `Transition<? super Action.Callback>`
- `inputTransitions`: `ChatState` → `Transition<? super Action.Input>` (свободный текст
  резолвится только состоянием)

`applyAction(Chat, Action)` выбирает таблицу по типу действия. Если переход не найден —
дефолтный `Handler` состояния. Сохранение чата выполняет вызывающий код (`EnglishTutorBot`
после `applyAction`), машина репозиториев не знает.

Регистрация: `onCommand(state, команда, t)` / `onButton(state, кнопка, t)` /
`onCallback(state, префикс, t)` / `onInput(state, t)`.

### Transition (Application, `application/statemachine/transition`)
`Transition<T>` — обработчик одного (состояние, триггер): `transit(Chat, T)`.
Типизация по триггеру: `Transition<Action.Command|Input|Callback|Button>`; если логика не
зависит от триггера (общая для кнопки и команды) — `Transition<Action>`
(`AddWordInLessonTransition`, `FinishLessonInLessonTransition`).

Переход — application-оркестратор: вызывает use case-порты других модулей (`RegisterTeacher`,
`StartLesson`, `AddWordToDictionary`, ...), мутирует собственный агрегат `Chat` и возвращает
`ActionResult`. Чужие репозитории не инжектятся.

Именование: `<Что><Триггер><Состояние>Transition`, триггеры: `Cmd` / `Inp` / `Cb` / `Btn`.
Например `NewCmdActiveTransition`, `StudentListTransition`, `PosCbAwaitingPosTransition`.
Пакеты по состояниям (`active/`, `initial/`, `studentslist/`, ...).

### Handler (Application, `application/statemachine/handler`)
Дефолтная реакция состояния на любой нераспознанный триггер («Введите имя ученика»,
«Используйте кнопки ниже», ...). Один `@Component` на состояние, собирается в
`Map<ChatState, Handler>` бином `chatStateHandlerMap` в `StateMachineConfig`.

### StateMachineConfig (`application/config`)
Собирает машину в одном `@Bean`-методе `stateMachine(...)`:
- переходы — обычные объекты (`new ...Transition(deps)`), а не Spring-бины; регистрируемые
  под несколькими ключами — локальные переменные
- таблица регистрации: `onCommand(state, команда, transition)`, `onButton(state, кнопка, transition)`,
  `onCallback(state, префикс, transition)`, `onInput(state, transition)`
- ключи Cmd/Btn — текст (`"/new"`, `Buttons.NEW_STUDENT`), ключ Cb — префикс (`"student"`, `"action"`, `"pos"`)

### View (презентация, `view/`)
Статические билдеры `ActionResult`: `MenuView` (главное меню), `StudentView` (список/опции
учеников, `optionsKeyboard()`), `WordView` (`posMenu`, `posLabel`), `LessonView`
(клавиатура урока, сводка завершения), `ExerciseView` (выбор типа упражнения).

Словари констант — единый источник для рендера и парсинга:
- `Buttons` — тексты кнопок + `REPLY_BUTTONS` (множество reply-кнопок для `UpdateParser`)
- `Callbacks` — префиксы и payload колбэков (`"student"`, `"action"`, `"pos"`, ...)
- `Commands` — команды (`"/register"`, `"/new"`, ...)
- `Messages` — тексты сообщений
- `util/ItemUtils.createCallbackData(...)` — сборка `"prefix:payload"`

## Application Layer

- `ChatService` — загрузка/сохранение `Chat` через `ChatRepository` (`getOrCreate`, `save`)

## Инфраструктура (`infrastructure/bot`)
- `UpdateParser` — `Update` → `Action`: callback-данные делятся на `prefix`/`payload` по первому `:`;
  `BOT_COMMAND` entity → `Action.Command`; текст из `Buttons.REPLY_BUTTONS` → `Action.Button`;
  остальной текст → `Action.Input`
- `EnglishTutorBot` — `TelegramLongPollingBot`: chatId → `ChatService.getOrCreate` →
  `UpdateParser` → `StateMachine.applyAction` → `ChatService.save` → `SendMessageConverter` →
  send/edit/delete; удаляет reply-клавиатуру при выходе из `ACTIVE`/`IN_LESSON`
- `BotInitializer`, `SendMessageConverter`

Персистентность: `InMemoryChatRepository` (план: PostgreSQL + Flyway).

## Зависимости
```java
@ApplicationModule(allowedDependencies = {
    "teacher :: teacher", "student :: student",
    "student :: lesson", "dictionary :: dictionary", "dictionary :: word",
    "shared :: shared", "exercise :: exercise"
})
```

## Известные долги
- Нет тестов на модуль (таблица переходов и переходы покрываются тривиально)
- `StateMachine.isReady()` не проверяет полноту таблиц (TODO в коде): состояние без `Handler`
  даст NPE в рантайме
- Опечатка в именах `AwatingExerciseAnswerHandler` / `AwatingExerciseTopicHandler` /
  `AwatingExerciseTypeHandler` (→ `Awaiting...`)
- Поиск имени ученика: загрузка всех студентов учителя и фильтрация в Java (нужен `FindStudentById`)
- Контекст чата stringly-typed (`Map<String, Object>` + касты)
- Ошибки `IllegalStateException` без обработки на уровне бота
- `ChatService.getOrCreate` и `InMemoryChatRepository.findById` дублируют логику создания чата
