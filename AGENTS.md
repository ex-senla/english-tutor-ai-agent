# ETA — English Tutor AI Agent

AI assistant for an English tutor. UI is a Telegram bot. Version 1.0.0 goals: teacher starts a
lesson and sends unknown words to the bot, bot links words to the student's dictionary, teacher can
send the dictionary to the student and generate exercises on a topic using dictionary words.
Core v1 bot flow (registration, students, lessons, add-word wizard) is implemented; exercise
generation and student details are stubs (see `docs/TODO.md`).

## Tech Stack

- Java 26, Maven (wrapper included: `mvnw` / `mvnw.cmd`)
- Spring Boot 4.1.0 (webmvc, actuator)
- Spring Modulith 2.1.0 (module boundaries are enforced)
- jMolecules DDD 2025.0.2
- Spring AI 2.0.0 — OpenAI starter pointed at DeepSeek (`deepseek-chat` via `DEEPSEEK_API_KEY`)
- TelegramBots 6.9.7.1 (spring-boot-starter)
- Lombok (annotation processors configured in maven-compiler-plugin)
- Persistence: in-memory only (`InMemory*Repository` in each module's `infrastructure/persistence`).
  PostgreSQL + Flyway/JPA planned; `debug` module is to be removed at that point.

## Build & Test

```bash
./mvnw test                 # all tests (use ./mvnw.cmd on Windows shells)
./mvnw verify               # tests + Modulith verification
./mvnw spring-boot:run      # run app (needs DEEPSEEK_API_KEY and TELEGRAM_BOT_TOKEN env vars)
./mvnw test -Dtest=CreateDictionaryUseCaseTest   # single test class
```

Tests do not require Telegram/AI credentials.

## Architecture

Modular monolith + DDD. Root package: `com.hydroyura.eta`. One Maven module; each Spring Modulith
module declares `@ApplicationModule(allowedDependencies = ...)` in its `package-info.java`.
Modules talk to each other only through `api` packages, which expose
`@NamedInterface`s (`shared`, `dictionary`, `word`, `student`, `lesson`, `teacher`, `exercise`).

### Module dependency rules (enforced by Modulith)

| Module | May depend on (named interfaces) | Purpose |
|---|---|---|
| `shared` | — | Cross-module contracts in `shared.api`: `Specification<T>`, `DomainException`, `SnapshotProvider` |
| `dictionary` | `shared` | Student dictionary, word validation (`WordSpecifications`) |
| `student` | `dictionary` (`dictionary`, `word`), `shared` | Student aggregate; `Lesson` is part of it |
| `teacher` | `dictionary` (`dictionary`, `word`), `student` (`student`, `lesson`), `shared` | Teacher, orchestrates Student+Dictionary creation |
| `exercise` | `dictionary`, `shared` | AI exercise generation (Spring AI) |
| `chatbot` | `teacher`, `student`, `dictionary`, `exercise`, `shared` | State machine, bot commands |
| `debug` | `shared` | Debug REST endpoints (temporary) |

Never introduce a dependency that is not in this table; update the table and the module's
`package-info.java` together if boundaries intentionally change.

### Layering inside each module

`api` (cross-module interfaces) → `application` (config, usecase, port) → `domain` (entities, pure
logic, no framework deps) → `infrastructure` (persistence, `ai`, `bot`, `rest` adapters).
`chatbot` additionally has a `view` package — Telegram presentation vocabulary: `Commands`,
`Buttons` (reply buttons), `Callbacks` (inline `prefix:payload`), `Messages`, and per-feature
subpackages (`menu`, `students`, `lesson`, `word`, `exercise`).

Domain layer stays framework-free. External integrations (Telegram, DeepSeek, future DB) live only
in `infrastructure` packages — the chatbot state machine is platform-agnostic.

### Key domain concepts

- Chatbot (v2 design, see `docs/modules/chatbot.md` and `docs/chat-bot-state-machine.MD`):
  - `Chat` aggregate (`chatbot.domain.chat`): `ChatId`, `ChatState`, key-value `context` for
    multi-step flows (`selectedStudentId`, `wordValue`, `exerciseType`, ...).
  - `ChatState` enum — 14 states: `INITIAL`, `AWAITING_REGISTRATION_NAME`, `ACTIVE`,
    `AWAITING_STUDENT_NAME`, `STUDENTS_LIST`, `STUDENT_OPTIONS`, `STUDENT_DETAILS`, `IN_LESSON`,
    `AWAITING_WORD`, `AWAITING_POS`, `AWAITING_TRANSLATION`, `AWAITING_EXERCISE_TYPE`,
    `AWAITING_EXERCISE_TOPIC`, `AWAITING_EXERCISE_ANSWER`.
  - `Action` / `ActionResult` sealed types (`chatbot.domain.action`): inputs are `Command`,
    `Input`, `Callback`, `Button`; outputs are `TextResponse`, `TextWithInlineKeyboard`,
    `TextWithReplyKeyboard`, `EditMessageText`, `DeleteMessage`.
  - `StateMachine` (application): pure routing `(Chat, Action) → ActionResult` via four transition
    tables (command / button / callback / input). Unmatched triggers fall to the state's default
    `Handler`. Persistence of `Chat` is done by the caller, not the machine.
  - `Transition<T>` (application, one package per state): orchestrates use cases of other modules
    and mutates `Chat`. Naming: `<What><Trigger><State>Transition`, triggers `Cmd`/`Inp`/`Cb`/`Btn`.
  - Telegram adapters live in `chatbot.infrastructure.bot`: `UpdateParser` (Update → `Action`),
    `SendMessageConverter` (`ActionResult` → Telegram API), `EnglishTutorBot`, `BotInitializer`.
- All module exceptions extend `shared` `DomainException`.
- Use Specification pattern (from `shared`) for validation/filtering, see `WordSpecifications`.

## Docs

- `docs/modules/<module>.md` — per-module design docs (Russian). Update the module doc when you
  change that module's design; keep them in Russian to match existing style.
- `docs/roadmap.MD`, `docs/TODO.md`, `docs/chat-bot-state-machine.MD`,
  `docs/exercise-generation-flow.md` — planning and flow documentation.
- `docs/sessions/` — per-session work logs (Russian), appended during development.

## Conventions

- Tests mirror module structure under `src/test/java`; unit tests for use cases and domain.
  Module boundaries are verified by root `ModuleVerificationTest`; `ModuleDocumentationTest`
  generates the Modulith component docs into `target/spring-modulith-docs/`.
  `chatbot` and `debug` currently have no dedicated unit tests.
- Prefer records and sealed types for value objects; Lombok is available (`@Getter`,
  `@RequiredArgsConstructor`, ...).
- Commits: short lowercase imperative messages (`word add update`, `finish lesson update logic`).
