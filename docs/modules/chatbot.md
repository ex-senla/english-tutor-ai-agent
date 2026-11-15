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
Sealed-иерархия ответов: `TextResponse`, `TextWithInlineKeyboard`, `TextWithReplyKeyboard`
(с `cleanupMessageId` — id сообщения, которое надо удалить при показе reply-экрана),
`EditMessageText`, `DeleteMessage`, `SendDocument` (fileName + содержимое + caption),
`SendDocuments` (список документов + текст с inline-клавиатурой).

### Chat (Domain Entity, `domain/chat`)
- `ChatId id` — record с `Long chatId`
- `ChatState state` — текущее состояние
- `Map<String, Object> context` — key-value хранилище. Ключи типизированы enum'ом `ContextKey`
  (`TEACHER_NAME`, `SELECTED_STUDENT_ID`, `SELECTED_STUDENT_NAME`, `ACTIVE_LESSON_ID`,
  `PENDING_STUDENT_NAME`, `WORD_VALUE`, `WORD_POS`, `WORD_TRANSLATIONS`, `EXERCISE_TYPE`,
  `GRAMMAR_RULE`, `EXERCISE_ID`, `EXERCISE_TOPIC`, `DICTIONARY_PAGE`, `WORD_SELECTION`,
  `WORD_FILTER`, `SELECTION_PAGE`)
- `ofDefaults(ChatId)` — фабрика, начальное состояние `INITIAL`

### ChatState (Enum, `domain/chat`)
20 состояний: `INITIAL`, `AWAITING_REGISTRATION_NAME`, `ACTIVE`, `AWAITING_STUDENT_NAME`,
`AWAITING_STUDENT_LEVEL`, `STUDENTS_LIST`, `STUDENT_OPTIONS`, `STUDENT_DETAILS`,
`STUDENT_DICTIONARY`, `AWAITING_STUDENT_LEVEL_CHANGE`, `IN_LESSON`, `AWAITING_POS`,
`AWAITING_TRANSLATION_CHOICE`, `AWAITING_MANUAL_TRANSLATION`, `STUDENT_SEARCH`,
`AWAITING_EXERCISE_TYPE`, `AWAITING_EXERCISE_GRAMMAR`, `AWAITING_EXERCISE_TOPIC`,
`AWAITING_EXERCISE_WORD_SELECTION`, `AWAITING_EXERCISE_ANSWER`.

`AWAITING_EXERCISE_ANSWER` (интерактивные ответы) в текущем PDF-потоке не достигается —
ни один переход в него не ведёт; переход и `CheckExercise` сохранены как задел.
Полная карта состояний и переходов — `docs/chat-bot-state-machine.MD`.

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
(`FinishLessonInLessonTransition`, `StudentSearchTransition`).

Переход — application-оркестратор: вызывает use case-порты других модулей (`RegisterTeacher`,
`StartLesson`, `AddWordToDictionary`, `GenerateExercise`, `GenerateExercisePdf`, ...),
мутирует собственный агрегат `Chat` и возвращает `ActionResult`. Чужие репозитории не инжектятся.

Именование: `<Что><Триггер><Состояние>Transition`, триггеры: `Cmd` / `Inp` / `Cb` / `Btn`.
Например `NewCmdActiveTransition`, `StudentListTransition`, `PosCbAwaitingPosTransition`.
Пакеты по состояниям (`active/`, `initial/`, `studentslist/`, `studentsearch/`, `cancel/`,
`awaitingtranslationchoice/`, `awaitingmanualtranslation/`, `studentdictionary/`,
`awaitingexercisewordselection/`, ...). В корне пакета — общие артефакты: `TransitionKey`,
`StudentSupport` (работа с выбранным учеником в контексте), `NewStudentTransition` и
`StudentListTransition` (регистрируются под несколькими ключами).

Флоу слова переиспользует `transition/word/LessonWordSupport` — общий код добавления слова в
словарь и активный урок (через порты `AddWordToDictionary` + `AddWordToLesson`) с обработкой
дубликата (`WordAlreadyExistsException`). Перевод предлагает порт `WordTranslator` из
`dictionary.api.word` (адаптер на Spring AI — `dictionary.infrastructure.ai.SpringAiWordTranslator`).

### Handler (Application, `application/statemachine/handler`)
Дефолтная реакция состояния на любой нераспознанный триггер («Введите имя ученика»,
«Используйте кнопки ниже», ...). Один `@Component` на состояние (20 штук), собираются в
`Map<ChatState, Handler>` бином `chatStateHandlerMap` в `StateMachineConfig`.

### StateMachineConfig (`application/config`)
Собирает машину в одном `@Bean`-методе `stateMachine(...)`:
- переходы — обычные объекты (`new ...Transition(deps)`), а не Spring-бины; регистрируемые
  под несколькими ключами — локальные переменные
- таблица регистрации: `onCommand(state, команда, transition)`, `onButton(state, кнопка, transition)`,
  `onCallback(state, префикс, transition)`, `onInput(state, transition)`
- ключи Cmd/Btn — текст (`"/new"`, `Buttons.NEW_STUDENT`), ключ Cb — префикс (`"student"`, `"action"`,
  `"pos"`, `"level"`, `"levelchange"`, `"exercise"`, `"details"`, `"back"`, `"translation"`,
  `"wdpg"`, `"wdreset"`, `"wdback"`, `"exword"`, `"exfilter"`, `"expg"`, `"exdone"`,
  `"excont"`, `"exback"`, `"exselback"`);
  команда `/cancel` регистрируется для всех 20 состояний через `CancelRule`/`CancelScreenRenderer`
  (14 правил отмены + `NoCancelCmdTransition` для 6 не-диалоговых состояний); свойства
  `ChatbotSearchProperties` и `ChatbotPaginationProperties` включаются через
  `@EnableConfigurationProperties`

### View (презентация, `view/`)
Статические билдеры `ActionResult`: `MenuView` (главное меню с «🔍 Найти ученика»),
`StudentView` (список/опции/карточка ученика, поиск `searchPrompt`/`searchResults`,
клавиатуры уровней CEFR, `optionsKeyboard()`), `StudentDictionaryView` (меню «Словарь»:
пагинация, кнопка «↺» только для LEARNED), `WordView` (`posMenu`, `translationChoice`,
`manualTranslationPrompt`, `posLabel`, `statusLabel`, маппинг payload → `PartOfSpeech`),
`LessonView` (клавиатура урока — только «🏁 Завершить урок», сводка завершения),
`ExerciseView` (выбор типа), `ExerciseWordSelectionView` (шаг выбора слов: toggle ✓/·,
фильтр по статусу, пагинация, «Готово», предупреждение при пустом выборе).

Словари констант — единый источник для рендера и парсинга:
- `Buttons` — тексты кнопок + `REPLY_BUTTONS` (множество reply-кнопок для `UpdateParser`)
- `Callbacks` — префиксы и payload колбэков (`"student"`, `"action"`, `"pos"`, `"level"`,
  `"translation"`, `"own"`, ...)
- `Commands` — команды (`"/register"`, `"/new"`, `"/find"`, `"/cancel"`, ...)
- `Messages` — тексты сообщений
- `util/ViewUtils.createCallbackData(...)` — сборка `"prefix:payload"`
- `util/ViewUtils.truncateButtonLabel(...)` — транкирование подписи inline-кнопки до 64 байт
  UTF-8 с «…»; `pageCount/hasPrev/hasNext/pageSlice` — хелперы пагинации

## Application Layer

- `ChatService` — загрузка/сохранение `Chat` через `ChatRepository` (`getOrCreate`, `save`)

## Инфраструктура (`infrastructure/bot`)
- `UpdateParser` — `Update` → `Action`: callback-данные делятся на `prefix`/`payload` по первому `:`
  (payload может быть пустым — `"levelchange"`); `BOT_COMMAND` entity → `Action.Command`;
  текст из `Buttons.REPLY_BUTTONS` → `Action.Button`; остальной текст → `Action.Input`
- `EnglishTutorBot` — `TelegramLongPollingBot`: chatId → `ChatService.getOrCreate` →
  `UpdateParser` → (индикатор «⏳ Генерирую упражнение…» для `AWAITING_EXERCISE_TOPIC` + ввод) →
  `StateMachine.applyAction` → удаление индикатора → `ChatService.save` → удаление сообщения
  из `cleanupMessageId` → `SendMessageConverter` → send/edit/delete. `SendDocuments` отправляется
  батчем: документы по одному, затем текст с inline-клавиатурой. Удаляет reply-клавиатуру при
  выходе из `ACTIVE`/`IN_LESSON` (`shouldRemoveReplyKeyboard` по смене oldState→newState).
  Служебные update (`my_chat_member` — кик, смена статуса) пропускаются
- `BotInitializer`, `SendMessageConverter` (в т.ч. `convertDocument` /
  `convertTextWithInlineKeyboard`; `SendDocuments` конвертером не разбирается — обрабатывается ботом)
- `BotCommands` — публичные команды Telegram-меню; `BotInitializer` регистрирует их через
  `SetMyCommands` (ошибка — log warn, не ломает стартап)

Персистентность: `InMemoryChatRepository` (+ `SnapshotProvider` для debug-эндпоинтов;
план: PostgreSQL + Flyway).

## Тесты

Юнит-тесты зеркалируют структуру модуля (`src/test/java/.../chatbot`):
переходы флоу слова (`InputInLessonTransition`, `PosCbAwaitingPosTransition`,
`TranslationCbAwaitingTranslationChoiceTransition`, `InputAwaitingManualTranslationTransition`,
`LessonWordSupport`), поиска (`studentsearch`), отмены (`cancel`, `StateMachineCancelTest`),
а также существующие `InputAwaitingExerciseTopicTransition`, `LevelCbAwaitingStudentLevelTransition`,
`BackCb`/`LevelCb` из `awaitingstudentlevelchange`, `SendMessageConverterTest`, `BotCommandsTest`.

## Зависимости
```java
@ApplicationModule(allowedDependencies = {
    "teacher :: teacher", "student :: student",
    "student :: lesson", "dictionary :: dictionary", "dictionary :: word",
    "shared :: shared", "exercise :: exercise",
    "generator :: document"
})
```

`dictionary :: word` используется портом `WordTranslator` (перевод слова) в флоу урока;
`generator :: document` — в переходах генерации упражнения (`GenerateCbAwaitingExerciseWordSelectionTransition` —
`GenerateExercisePdf` — пара PDF: упражнение + ответы).

## Известные долги
- `StateMachine.isReady()` не проверяет полноту таблиц (TODO в коде): состояние без `Handler`
  даст NPE в рантайме
- Опечатка в именах `AwatingExerciseAnswerHandler` / `AwatingExerciseTopicHandler` /
  `AwatingExerciseTypeHandler` (→ `Awaiting...`)
- Поиск имени ученика: загрузка всех студентов учителя и фильтрация в Java (нужен `FindStudentById`)
- Контекст чата остаётся stringly-typed: `Map<String, Object>` + касты, несмотря на `ContextKey`
  (`StudentSupport` использует строку `"selectedStudentId"`)
- `AWAITING_EXERCISE_ANSWER` недостижим (legacy-ветка интерактивных ответов) — решить судьбу
- Ошибки `IllegalStateException` без обработки на уровне бота
- `ChatService.getOrCreate` и `InMemoryChatRepository.findById` дублируют логику создания чата
- FIXME в `ExerciseCbAwaitingExerciseTypeTransition` (NPE-комментарий к lookup payload)
