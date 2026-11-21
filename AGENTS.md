# ETA — English Tutor AI Agent

AI assistant for an English tutor. UI is a Telegram bot. Version 1.0.0 goals: teacher starts a
lesson and sends unknown words to the bot, bot links words to the student's dictionary, teacher can
send the dictionary to the student and generate exercises on a topic using dictionary words.
Core v1 flow is implemented: registration, students (with CEFR level), lessons, add-word wizard,
student details, AI exercise generation (fill-in-the-blank / multiple choice) with PDF export for
student and teacher (see `docs/TODO.md` for remaining work).

## Task tracking

- **Open issues (single source of truth):** https://github.com/ex-senla/english-tutor-ai-agent/issues
- Workflow position (AgentCraft): `.pi/agentcraft-state.md` — check and keep it in sync after any
  work on an issue; when an issue is implemented/closed on GitHub, reflect that here and in
  `docs/TODO.md`.

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
- Persistence: PostgreSQL + Spring Data JPA + Flyway. Schema lives in a single migration
  `src/main/resources/db/migration/V1__init.sql`, Hibernate runs in `ddl-auto: validate`.
  JPA-entities (`*Jpa`), Spring Data interfaces (`*DataRepository`) and domain↔JPA mappers live
  only in each module's `infrastructure/persistence`; domain stays framework-free.
  Integration/N+1 tests run against real PostgreSQL via Testcontainers (Docker is required).

## Build & Test

```bash
./mvnw test                 # all tests (use ./mvnw.cmd on Windows shells)
./mvnw verify               # tests + Modulith verification + Spotless (needs Docker/Testcontainers)
./mvnw spring-boot:run      # run app (needs TELEGRAM_BOT_TOKEN, AI_API_KEY, AI_BASE_URL, AI_CHAT_MODEL env vars)
./mvnw test -Dtest=CreateDictionaryUseCaseTest   # single test class
```

Tests do not require Telegram/AI credentials (the bot/LLM are disabled with dummy values and
`telegram.bot.enabled=false`).

Local dev database: `docker compose up -d postgres` (credentials match the local defaults in
`application.yaml`; see `.env.example`). `./mvnw spring-boot:run` connects to it by default.
Demo students seeding is **dev-only and off by default** (`eta.demo-students.enabled=false`);
enable it locally via env `ETA_DEMO_STUDENTS_ENABLED=true`.

## CI/CD & Releases

- CI: GitHub Actions `.github/workflows/test-deploy.yml`. Triggers: `pull_request` → build
  (`./mvnw -B verify`, merge gate, no deploy); `push` to `master` → build + deploy to test;
  `workflow_dispatch` → manual re-deploy.
- Test environment: Docker image `ghcr.io/ex-senla/english-tutor-ai-agent:<sha>` (built from
  `Dockerfile`), pushed to GHCR, deployed over SSH to the test VPS via
  `docker compose -f docker-compose.test.yml pull && up -d` (single long-polling instance).
- Release policy (trunk-based, semver tags `vX.Y.Z` for prod, SHA tag for test, test→prod
  criteria, rollback, secrets): `docs/release-policy.md`. Prod pipeline is **not** automated
  (issue #46 covers only the test environment).

## Architecture

Modular monolith + DDD. Root package: `com.hydroyura.eta`. One Maven module; each Spring Modulith
module declares `@ApplicationModule(allowedDependencies = ...)` in its `package-info.java`.
Modules talk to each other only through `api` packages, which expose
`@NamedInterface`s (`shared`, `dictionary`, `word`, `student`, `lesson`, `teacher`, `exercise`,
`document`, `support`).

### Module dependency rules (enforced by Modulith)

| Module | May depend on (named interfaces) | Purpose |
|---|---|---|
| `shared` | — | Cross-module contracts in `shared.api`: `Specification<T>`, `DomainException` |
| `dictionary` | `shared` | Student dictionary, `WordInfo` aggregate (word + translations + POS + status), validation (`WordSpecifications`) |
| `student` | `dictionary` (`dictionary`, `word`), `shared` | Student aggregate (incl. CEFR level); `Lesson` is part of it |
| `teacher` | `dictionary` (`dictionary`, `word`), `student` (`student`, `lesson`), `shared` | Teacher, orchestrates Student+Dictionary creation, demo data seeder |
| `exercise` | `dictionary` (`dictionary`, `word`), `shared` | AI exercise generation and checking (Spring AI) |
| `generator` | `exercise` (`exercise`), `dictionary` (`word`), `shared` | Renders exercises into PDF documents (student sheet + teacher answers) |
| `support` | `teacher` (`teacher`), `shared` | Support threads and messages (teacher support channel) |
| `chatbot` | `teacher`, `student`, `dictionary`, `exercise`, `generator` (`document`), `support`, `shared` | State machine, bot commands |

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
    `EXERCISE_TYPE`, `EXERCISE_TOPIC`, `EXERCISE_THEMATIC_TOPIC`, ...).
  - `ChatState` enum — 23 states: `INITIAL`, `AWAITING_REGISTRATION_NAME`, `ACTIVE`,
    `AWAITING_STUDENT_NAME`, `AWAITING_STUDENT_LEVEL`, `STUDENTS_LIST`, `STUDENT_OPTIONS`,
    `STUDENT_DETAILS`, `AWAITING_STUDENT_LEVEL_CHANGE`, `AWAITING_STUDENT_DELETE`, `IN_LESSON`,
    `AWAITING_POS`, `AWAITING_TRANSLATION_CHOICE`, `AWAITING_MANUAL_TRANSLATION`, `STUDENT_SEARCH`,
    `AWAITING_EXERCISE_TYPE`, `AWAITING_EXERCISE_TOPIC`, `AWAITING_CUSTOM_TOPIC`,
    `AWAITING_EXERCISE_TOPIC_CONFIRM`, `AWAITING_EXERCISE_THEMATIC_TOPIC`,
    `AWAITING_EXERCISE_WORD_SELECTION`, `AWAITING_EXERCISE_ANSWER`, `STUDENT_DICTIONARY`.
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
  `generator`). Update the module doc when you change that module's design; keep them
  in Russian to match existing style.
- `docs/roadmap.MD`, `docs/TODO.md`, `docs/chat-bot-state-machine.MD`,
  `docs/exercise-generation-flow.md` — planning and flow documentation.
- `docs/release-policy.md` — release policy: branching, merge, versioning (prod tags / test SHA),
  test→prod criteria, rollback, and the manual setup checklist (branch protection, Environment
  `test`, VPS, test bot).

## Conventions

- Tests mirror module structure under `src/test/java`; unit tests for use cases, domain and chatbot
  transitions; integration round-trip/N+1 tests against Testcontainers PostgreSQL. Module
  boundaries are verified by root `ModuleVerificationTest`; `ModuleDocumentationTest` generates
  the Modulith component docs into `target/spring-modulith-docs/`.
- Prefer records and sealed types for value objects; Lombok is available (`@Getter`,
  `@RequiredArgsConstructor`, ...).
- Commits: short lowercase imperative messages (`wordInfo add update`, `finish lesson update logic`).

## AgentCraft workflow

This repository follows the **AgentCraft** development workflow for AI-assisted software development.

## Workflow

Development follows eight stages coordinated by the Orchestrator (the leading coding agent):

1. Preparation
2. Analyst
3. Architect
4. QA Planning
5. Developer
6. Reviewer
7. QA Testing
8. Finalization and Pull Request

- Stage details and transitions: `docs/agentcraft/workflow.md`
- Orchestrator role and delegation rules: `docs/agentcraft/orchestrator.md`
- Quality-gate pass conditions: `docs/agentcraft/quality-gates.md`
- Current workflow position: `.pi/agentcraft-state.md` (maintained by the Orchestrator)

### Model assignments (project rule)

Stages 3 (Architect), 6 (Reviewer), and 7 (QA Testing) MUST be delegated to the
`ollama/glm-5.3` model (GLM 5.3, Ollama Cloud). Other stages use the session's default model.
The Orchestrator passes `model: "ollama/glm-5.3"` when launching the `architect`, `dev-reviewer`,
and `qa-tester` subagents.

## Quality gates

| Gate | What it checks |
|------|----------------|
| G1 | Repository, task, and environment are ready |
| G2 | Requirements and Acceptance Criteria are approved |
| G3 | OpenSpec artifacts are prepared |
| G4 | Test plan and technical solution are approved |
| G5 | Build and primary development checks pass |
| G6 | Independent code review passes |
| G7 | QA passes (or user-confirmed manual verification) |
| G8 | All stages complete, Pull Request ready |

## Working principles

- User approval at defined checkpoints; no implementation before requirements, design, and Test plan are approved.
- Prefer deterministic, programmatic verification over self-reported "done".
- Keep implementation, code review, and QA testing as separate responsibilities.
- Simplicity: no premature abstractions.

