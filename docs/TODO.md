# TODO

## Срочно

- [x] Запуск приложения — исправлено: Spring Modulith 1.1.10 → 2.1.0 (несовместимость с SB 4.1.0)
- [x] `ModuleVerificationTest.verifyModularity` — исправлено
- [x] Миграция teacher → `api/teacher/`, `domain/teacher/` (готово: `api/teacher/`, `domain/teacher/`)
- [x] Миграция exercise → `api/exercise/`, `domain/exercise/` (готово)

## Модули

- [x] **Student** — factory + validation
- [x] **Teacher** — use case создания Student (CreateStudentWithDictionary: оркестрирует Dictionary → Student)
- [x] **Lesson** — реализован как Entity внутри агрегата Student
- [ ] **Exercise** — наполнить: API, use cases, типы упражнений

## Chatbot

- [x] StateMachine на базе `Class<? extends Command> pendingCommand`
- [x] Context — key-value map вместо sealed classes
- [x] CommandDispatcherImpl + CommandDispatcherConfig
- [x] Двухфазный ввод для register/newstudent/startlesson
- [x] Трёхшаговый ввод для AddWordCmd (word → POS → translation)
- [x] Result record с stay() / transition()
- [x] StateMachineAppService вместо StateMachineService

## Инфраструктура

- [ ] JPA entities + репозитории (замена in-memory)
- [ ] PostgreSQL + Flyway миграции
- [ ] Spring AI интеграция

## Технический долг

- [ ] `EtaApplicationTests` — удалён из-за Spring Boot ClassNotFoundException, восстановить
- [ ] InMemoryDictionaryRepository — заменить на JPA
- [ ] InMemoryStudentRepository — заменить на JPA
- [ ] InMemoryTeacherRepository — заменить на JPA
- [ ] InMemoryLessonRepository — заменить на JPA
- [ ] InMemoryExerciseRepository — заменить на JPA
- [ ] InMemoryStateMachineRepository — заменить на persistent
- [ ] Wire AddWordToDictionaryUseCase в full context после появления JPA репозитория
- [ ] Chatbot module - провести рефакторинг команд, сделать логику более расширяемой 
