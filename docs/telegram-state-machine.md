# Telegram Bot State Machine

## Состояния

```
NOT_REGISTER → ACTIVE → IN_LESSON → ACTIVE → ...
```

| State | Контекст | Допустимые команды |
|-------|----------|-------------------|
| `NOT_REGISTER` | — | `/start`, `/register`, `/help` |
| `ACTIVE` | — | `/start`, `/newstudent`, `/startlesson`, `/students`, `/student`, `/exercise`, `/help` |
| `IN_LESSON` | `context["lessonId"] = UUID` | `/add`, `/endlesson`, `/students`, `/student`, `/exercise`, `/help` |

## Двухфазный ввод (через pendingCommand)

Команды с аргументами (`/register`, `/newstudent`, `/startlesson`, `/add`) работают в два шага:

```mermaid
sequenceDiagram
    participant SM as StateMachine
    participant SVC as StateMachineAppService
    participant DISP as CommandDispatcher
    participant CMD as Command

    U->>B: нажимает /register (кнопка)
    B->>SVC: handle("/register", chatId)
    SVC->>DISP: dispatch("/register")
    DISP-->>SVC: RegisterCmd
    SVC->>CMD: execute(sm, "/register")
    CMD->>SM: setPendingCommand(RegisterCmd.class)
    CMD-->>SVC: Result.stay("Enter your name:")
    B-->>U: "Enter your name:"

    U->>B: Yury
    B->>SVC: handle("Yury", chatId)
    SVC->>SM: getPendingCommandSafely() → RegisterCmd.class
    SVC->>DISP: get(RegisterCmd.class)
    DISP-->>SVC: RegisterCmd
    SVC->>CMD: execute(sm, "Yury")
    CMD->>CMD: registerTeacher.execute(...)
    CMD->>SM: clearPendingCommand()
    CMD-->>SVC: Result.transition("✅ Registered!", ACTIVE)
    B-->>U: "✅ Registered!" + кнопки ACTIVE
```

1. **Кнопка** — команда сохраняет `pendingCommand` (класс) и просит ввод
2. **Текст** — `StateMachineAppService` видит pending, получает команду по классу, выполняет с текстом

## Граф переходов

```mermaid
stateDiagram-v2
    [*] --> NOT_REGISTER
    NOT_REGISTER --> ACTIVE: /register <name>
    ACTIVE --> IN_LESSON: /startlesson <name>
    IN_LESSON --> ACTIVE: /endlesson
```

## Полный цикл обработки сообщения

```mermaid
sequenceDiagram
    participant TG as Telegram
    participant B as EnglishTutorBot
    participant SVC as StateMachineAppService
    participant R as StateMachineRepository
    participant D as CommandDispatcher
    participant C as Command
    participant SM as StateMachine
    participant API as Business API

    TG->>B: Update (chatId, text)
    B->>SVC: handle(text, chatId)
    SVC->>R: findById(chatId)
    R-->>SVC: StateMachine (state, context, pendingCommand)

    alt pendingCommand != null
        SVC->>D: get(pendingCommand)
    else
        SVC->>D: dispatch(text)
    end
    D-->>SVC: Command

    SVC->>SM: execute(command, text)
    alt state.allows(command.type())
        SM->>C: execute(this, text)
        C->>API: use case call
        API-->>C: result
        C-->>SM: Result(state, context, message)
        SM->>SM: state = result.state()
        SM->>SM: context = result.context()
    else
        SM-->>SVC: "Command not available"
    end

    SVC->>R: save(stateMachine)
    SVC-->>B: response text
    B->>SM: getState()
    SM-->>B: current state
    B->>TG: SendMessage(text + keyboard(state))
```

## Многошаговый ввод AddWordCmd

`/add` использует context как временное хранилище для трёхшагового ввода:

```
/add → "Enter word:"       (step=0, pending=AddWordCmd)
  apple → "Enter POS:"      (step=1, word=apple)
  NOUN → "Enter translation:" (step=2, pos=NOUN)
  яблоко → "✅ added"      (done, clearPending)
```

Альтернативно: `/add apple NOUN яблоко` — все три шага в одном сообщении.

## Архитектура взаимодействия

```mermaid
flowchart TB
    subgraph Telegram
        U[Пользователь]
    end

    subgraph "Chatbot Module"
        subgraph Infrastructure
            B[EnglishTutorBot]
        end

        subgraph Application
            SVC[StateMachineAppService]
            CFG[CommandDispatcherConfig]
            DISP[CommandDispatcherImpl]
            MOD[ChatbotModuleConfig]
        end

        subgraph "Domain - StateMachine"
            SM[StateMachine]
            ST[State]
            CTX[Context]
            REPO[StateMachineRepository]
        end

        subgraph "Domain - Command"
            CMD[Command]
            RES[Result]
            CDISP[CommandDispatcher]
        end

        subgraph Commands
            C1[StartCmd]
            C2[RegisterCmd]
            C3[NewStudentCmd]
            C4[StartLessonCmd]
            C5[AddWordCmd]
            C6[EndLessonCmd]
            C7[HelpCmd]
        end
    end

    subgraph "Business Modules"
        TEACHER[Teacher API]
        STUDENT[Student API]
        DICT[Dictionary API]
    end

    U -->|"/register"| B
    B -->|handle| SVC
    SVC -->|load/save| REPO
    SVC -->|dispatch/get| DISP
    DISP -->|create via| CFG
    CFG -->|injects| TEACHER
    CFG -->|injects| STUDENT
    CFG -->|injects| DICT
    C2 -->|execute| SM
    C2 -->|calls| TEACHER
    SM -->|state| ST
    SM -->|context| CTX
    MOD -->|creates bean| SVC
```

---

## Callback-запросы (inline-кнопки)

С версии, добавившей фичи 3 и 4, бот поддерживает inline-кнопки и callback-запросы:

```mermaid
sequenceDiagram
    participant U as Пользователь
    participant B as EnglishTutorBot
    participant SVC as StateMachineAppService
    participant CMD as StudentCmd

    U->>B: /students
    B->>SVC: handle("/students", chatId)
    SVC-->>B: BotResponse(text + inlineKeyboard)
    B-->>U: список учеников + кнопки

    U->>B: клик "Иван"
    B->>B: callback "student:Иван" → "/student Иван"
    B->>SVC: handle("/student Иван", chatId)
    SVC->>CMD: execute(sm, "/student Иван")
    CMD-->>SVC: Result.stay("👤 Иван\n📚 Dictionary: ...")
    B-->>U: детали ученика
```

Callback конвертируется в текстовую команду и проходит через общий `service.handle()` — бот остаётся тонким адаптером.

## Inline-клавиатура в Result

```java
// Result record с опциональной inline-клавиатурой
public record Result(String message, CommandType commandType, State state,
                     Optional<Context> context, List<List<String>> inlineKeyboard) {

    public static Result stay(String message, CommandType type) { ... }

    // Новый метод: stay с inline-кнопками
    public static Result stay(String message, CommandType type,
                              List<List<String>> inlineKeyboard) { ... }
}
```

`BotExecuteResult` передаёт inlineKeyboard из StateMachine в StateMachineAppService,
который возвращает `BotResponse` с текстом и кнопками.

## Context — новые методы

```java
public class Context {
    public void put(String key, Object value) { ... }
    public Object get(String key) { ... }
    public void remove(String key) { ... }          // NEW
    public <T> T get(String key, Class<T> type) { ... }
}
```

## ExerciseCmd — двухфазная работа

```
/exercise FILL_IN_THE_BLANK Animals  →  генерация + "Reply with your answer:"
  user types answer                   →  CheckExercise → ✅/❌ + результат
```

В context сохраняется `exerciseId` для проверки ответа. После проверки — очистка.

## Новые команды (v1.0.0)

| Команда | Класс | Назначение |
|---------|-------|-----------|
| `/students` | `StudentsCmd` | Список учеников с inline-кнопками |
| `/student <name>` | `StudentCmd` | Детали ученика: словарь, статус урока |
| `/exercise` | `ExerciseCmd` | Генерация и проверка упражнений |
