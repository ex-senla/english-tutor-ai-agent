# Chatbot

Модуль стейт-машин для бота. Платформонезависимый — Telegram, WhatsApp и т.д. подключаются через инфраструктуру.

## Составляющие

### StateMachine (Domain Entity)
- `StateMachineId id` — record с `Long chatId`
- `State state` — текущее состояние
- `Class<? extends Command> pendingCommand` — ожидаемая команда (для двухфазного ввода)
- `Context context` — key-value хранилище (`Map<String, Object>`)
- `ofDefaults(StateMachineId)` — фабрика, начальное состояние `NOT_REGISTER`
- `execute(Command, String userMessage)` — выполняет команду, возвращает текст ответа
- `getPendingCommandSafely()` → `Optional<Class<? extends Command>>`
- `clearPendingCommand()` — сбрасывает `pendingCommand = null`

### State (Enum)
- `NOT_REGISTER`: START, REGISTER, HELP
- `ACTIVE`: START, NEW_STUDENT, START_LESSON, HELP
- `IN_LESSON`: ADD_WORD, END_LESSON, HELP
- `keyboardButtons()` — возвращает кнопки клавиатуры для состояния

### CommandType (Enum)
START, REGISTER, NEW_STUDENT, START_LESSON, ADD_WORD, END_LESSON, HELP, UNKNOWN

### Context
- Key-value хранилище (`Map<String, Object>`)
- Методы: `put(key, value)`, `get(key)`, `get(key, Class<T>)`
- В `IN_LESSON` хранит `"lessonId"` (UUID)
- `AddWordCmd` использует для многошагового ввода: `"addWord.step"`, `"addWord.word"`, `"addWord.pos"`

### Command (Domain Interface, `domain/command`)
```java
public interface Command {
    CommandType type();
    Result execute(StateMachine sm, String userMessage);
    boolean matches(String text); // default: false
}
```

### Result (Domain Record, `domain/command`)
```java
public record Result(String message, CommandType commandType, State state, Optional<Context> context) {
    static Result stay(message, type);          // без смены состояния
    static Result transition(message, type, newState, newContext); // со сменой состояния
}
```

### CommandDispatcher (Domain Interface, `domain/command`)
- `dispatch(String command)` → Command — находит команду по тексту
- `get(Class<? extends Command>)` → Command — получает команду по классу (для pending)

## Application Layer

### StateMachineAppService
- `handle(String message, Long chatId)` — основной метод обработки сообщения
  1. Загружает StateMachine из репозитория (или создаёт новую)
  2. Если есть `pendingCommand` — использует её, иначе диспатчит по тексту
  3. Выполняет `sm.execute(command, message)`
  4. Сохраняет StateMachine
  5. Возвращает текст ответа
- `getState(Long chatId)` — возвращает текущее состояние

### CommandDispatcherImpl
- `@Component`, создаёт экземпляры команд через `CommandDispatcherConfig`
- Команды создаются per-request (не бины), получают зависимости через config record
- `CommandDispatcherConfig` — record со всеми API-зависимостями:
  `RegisterTeacher`, `CreateStudentWithDictionary`, `StartLesson`, `AddWordToDictionary`,
  `AddWordToLesson`, `EndLesson`, `FindTeacher`, `StudentQuery`

### ChatbotModuleConfig
- `@Configuration`, создаёт бин `StateMachineAppService`

## Команды

### StartCmd
- `/start` — проверяет, зарегистрирован ли учитель (через `FindTeacher.findByTelegramChatId`)
- Если да → transition в ACTIVE ("Welcome back!")
- Если нет → stay в NOT_REGISTER ("Welcome! /register <name>")

### RegisterCmd
- Двухфазный ввод:
  1. `/register` → `pendingCommand = RegisterCmd.class`, просит ввести имя
  2. Следующее сообщение (имя) → выполняет `RegisterTeacher`, transition в ACTIVE
- `/register Name` в одном сообщении → сразу регистрирует

### NewStudentCmd
- Двухфазный ввод: `/newstudent` → запрос имени → `CreateStudentWithDictionary`
- `/newstudent Name` в одном сообщении

### StartLessonCmd
- Двухфазный ввод: `/startlesson` → запрос имени студента → `FindStudentByNameQuery`
- Находит студента среди учеников учителя, вызывает `StartLesson`
- Сохраняет `lessonId` в контекст, transition в IN_LESSON

### AddWordCmd
- Трёхшаговый ввод через контекст:
  1. Запрос слова → сохраняет в `addWord.word`
  2. Запрос части речи (NOUN/VERB/ADJECTIVE) → сохраняет в `addWord.pos`
  3. Запрос перевода (через `;` для нескольких) → `AddWordToDictionary` + `AddWordToLesson`
- `/add word VERB translation` в одном сообщении — все три шага сразу

### EndLessonCmd
- `/endlesson` — достаёт `lessonId` из контекста, вызывает `EndLesson`, transition в ACTIVE

### HelpCmd
- `/help` — возвращает доступные команды в зависимости от состояния

## Инфраструктура
- `EnglishTutorBot` — `TelegramLongPollingBot`, Spring `@Component`
  - `onUpdateReceived` → `service.handle(text, chatId)` → отправляет ответ с клавиатурой
  - Клавиатура формируется из `state.keyboardButtons()`
- `BotInitializer` — регистрирует бота в `TelegramBotsApi` через `@PostConstruct`
- `InMemoryStateMachineRepository` — `ConcurrentHashMap<StateMachineId, StateMachine>`

## Зависимости
```java
@ApplicationModule(allowedDependencies = {
    "teacher :: teacher", "student :: student",
    "student :: lesson", "dictionary :: dictionary", "dictionary :: word"
})
```
