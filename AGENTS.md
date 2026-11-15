# ETA — English Tutor AI Agent

AI assistant for an English tutor. UI is a Telegram bot. Version 1.0.0 goals: teacher starts a
lesson and sends unknown words to the bot, bot links words to the student's dictionary, teacher can
send the dictionary to the student and generate exercises on a topic using dictionary words.
Core v1 flow is implemented: registration, students (with CEFR level), lessons, add-word wizard,
student details, AI exercise generation (fill-in-the-blank / multiple choice) with PDF export for
student and teacher (see `docs/TODO.md` for remaining work).

## Tech Stack

- Java 26, Maven (wrapper included: `mvnw` / `mvnw.cmd`)
- Spring Boot 4.1.0 (webmvc, actuator, thymeleaf)
- Spring Modulith 2.1.0 (module boundaries are enforced)
- jMolecules DDD 2025.0.2
- Spring AI 2.0.0 — OpenAI starter against any OpenAI-compatible endpoint, configured via env:
  `AI_API_KEY`, `AI_BASE_URL`, `AI_CHAT_MODEL` (prompts in `src/main/resources/exercise/prompts/`)
- OpenHTMLtoPDF (`openhtmltopdf-pdfbox` 1.0.10) + Thymeleaf — renders exercise PDFs
- TelegramBots 6.9.7.1 (spring-boot-starter)
- Lombok (annotation processors configured in maven-compiler-plugin)
- Persistence: in-memory only (`InMemory*Repository` in each module's `infrastructure/persistence`).
  PostgreSQL + Flyway/JPA planned; `debug` module is to be removed at that point.

## Build & Test

```bash
./mvnw test                 # all tests (use ./mvnw.cmd on Windows shells)
./mvnw verify               # tests + Modulith verification
./mvnw spring-boot:run      # run app (needs TELEGRAM_BOT_TOKEN, AI_API_KEY, AI_BASE_URL, AI_CHAT_MODEL env vars)
./mvnw test -Dtest=CreateDictionaryUseCaseTest   # single test class
```

Tests do not require Telegram/AI credentials.
`eta.demo-students.enabled=true` (see `application.yaml`) seeds demo students with words on startup
(`DemoStudentsSeeder` in `teacher.application.demo`) — handy for local runs.

## Architecture

Modular monolith + DDD. Root package: `com.hydroyura.eta`. One Maven module; each Spring Modulith
module declares `@ApplicationModule(allowedDependencies = ...)` in its `package-info.java`.
Modules talk to each other only through `api` packages, which expose
`@NamedInterface`s (`shared`, `dictionary`, `word`, `student`, `lesson`, `teacher`, `exercise`,
`document`).

### Module dependency rules (enforced by Modulith)

| Module | May depend on (named interfaces) | Purpose |
|---|---|---|
| `shared` | — | Cross-module contracts in `shared.api`: `Specification<T>`, `DomainException`, `SnapshotProvider` |
| `dictionary` | `shared` | Student dictionary, `WordInfo` aggregate (word + translations + POS + status), validation (`WordSpecifications`) |
| `student` | `dictionary` (`dictionary`, `word`), `shared` | Student aggregate (incl. CEFR level); `Lesson` is part of it |
| `teacher` | `dictionary` (`dictionary`, `word`), `student` (`student`, `lesson`), `shared` | Teacher, orchestrates Student+Dictionary creation, demo data seeder |
| `exercise` | `dictionary` (`dictionary`, `word`), `shared` | AI exercise generation and checking (Spring AI) |
| `generator` | `exercise` (`exercise`), `dictionary` (`word`), `shared` | Renders exercises into PDF documents (student sheet + teacher answers) |
| `chatbot` | `teacher`, `student`, `dictionary`, `exercise`, `generator` (`document`), `shared` | State machine, bot commands |
| `debug` | `shared` | Debug REST endpoints (temporary) |

Never introduce a dependency that is not in this table; update the table and the module's
`package-info.java` together if boundaries intentionally change.

### Layering inside each module

`api` (cross-module interfaces) → `application` (config, usecase, port) → `domain` (entities, pure
logic, no framework deps) → `infrastructure` (persistence, `ai`, `bot`, `pdf`, `rest` adapters).
`chatbot` additionally has a `view` package — Telegram presentation vocabulary: `Commands`,
`Buttons` (reply buttons), `Callbacks` (inline `prefix:payload`), `Messages`, and per-feature
subpackages (`menu`, `students`, `lesson`, `word`, `exercise`) plus `util` (`ViewUtils`).
`generator` keeps Thymeleaf templates and fonts in `src/main/resources/generator/`.

Domain layer stays framework-free. External integrations (Telegram, LLM, PDF, future DB) live only
in `infrastructure` packages — the chatbot state machine is platform-agnostic.

### Key domain concepts

- Chatbot (v2 design, see `docs/modules/chatbot.md` and `docs/chat-bot-state-machine.MD`):
  - `Chat` aggregate (`chatbot.domain.chat`): `ChatId`, `ChatState`, key-value `context` for
    multi-step flows with typed `ContextKey` enum keys (`SELECTED_STUDENT_ID`, `WORD_VALUE`,
    `EXERCISE_TYPE`, `GRAMMAR_RULE`, ...).
  - `ChatState` enum — 17 states: `INITIAL`, `AWAITING_REGISTRATION_NAME`, `ACTIVE`,
    `AWAITING_STUDENT_NAME`, `AWAITING_STUDENT_LEVEL`, `STUDENTS_LIST`, `STUDENT_OPTIONS`,
    `STUDENT_DETAILS`, `AWAITING_STUDENT_LEVEL_CHANGE`, `IN_LESSON`, `AWAITING_WORD`, `AWAITING_POS`,
    `AWAITING_TRANSLATION`, `AWAITING_EXERCISE_TYPE`, `AWAITING_EXERCISE_GRAMMAR`,
    `AWAITING_EXERCISE_TOPIC`, `AWAITING_EXERCISE_ANSWER`.
  - `Action` / `ActionResult` sealed types (`chatbot.domain.action`): inputs are `Command`,
    `Input`, `Callback`, `Button`; outputs are `TextResponse`, `TextWithInlineKeyboard`,
    `TextWithReplyKeyboard`, `EditMessageText`, `DeleteMessage`, `SendDocument`, `SendDocuments`
    (PDF delivery to Telegram).
  - `StateMachine` (application): pure routing `(Chat, Action) → ActionResult` via four transition
    tables (command / button / callback / input). Unmatched triggers fall to the state's default
    `Handler`. Persistence of `Chat` is done by the caller, not the machine.
  - `Transition<T>` (application, one package per state): orchestrates use cases of other modules
    and mutates `Chat`. Naming: `<What><Trigger><State>Transition`, triggers `Cmd`/`Inp`/`Cb`/`Btn`.
  - Telegram adapters live in `chatbot.infrastructure.bot`: `UpdateParser` (Update → `Action`),
    `SendMessageConverter` (`ActionResult` → Telegram API), `EnglishTutorBot`, `BotInitializer`.
- Exercise flow (`docs/exercise-generation-flow.md`): `GenerateExerciseUseCase` → `ExerciseGenerator`
  port → `SpringAiExerciseGenerator` (ChatClient + prompt files) → `CheckExerciseUseCase`;
  `generator.GenerateExercisePdfUseCase` turns an exercise into an `ExercisePdfBundle` (student +
  teacher PDFs) via `ThymeleafPdfRenderer`.
- All module exceptions extend `shared` `DomainException`.
- Use Specification pattern (from `shared`) for validation/filtering, see `WordSpecifications`.

## Docs

- `docs/modules/<module>.md` — per-module design docs (Russian; exists for all modules except
  `generator` and `debug`). Update the module doc when you change that module's design; keep them
  in Russian to match existing style.
- `docs/roadmap.MD`, `docs/TODO.md`, `docs/chat-bot-state-machine.MD`,
  `docs/exercise-generation-flow.md` — planning and flow documentation.

## Conventions

- Tests mirror module structure under `src/test/java`; unit tests for use cases, domain and chatbot
  transitions. Module boundaries are verified by root `ModuleVerificationTest`;
  `ModuleDocumentationTest` generates the Modulith component docs into
  `target/spring-modulith-docs/`. `debug` currently has no dedicated unit tests.
- Prefer records and sealed types for value objects; Lombok is available (`@Getter`,
  `@RequiredArgsConstructor`, ...).
- Commits: short lowercase imperative messages (`wordInfo add update`, `finish lesson update logic`).
