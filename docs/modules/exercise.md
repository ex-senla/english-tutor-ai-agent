# Exercise

Упражнения, генерируемые на основе словаря ученика.

## Составляющие

### Exercise (@Entity)
- `id` (@Identity, ExerciseId), `type` (ExerciseType), `topic` (готовая строка отображения темы),
  `thematicTopic` (nullable-строка предметно-контекстной тематики),
  ассоциация `wordInfoIds` (`@Association`, `Set<WordInfoId>`), `content`, `expectedAnswers`,
  `items`, `status` (ExerciseStatus).
- Статическая фабрика: `Exercise.create(id, type, topic, thematicTopic, wordInfoIds)`.

### Каталог грамматических тем
- `domain.topic.GrammarTopic` — value object узла каталога (`slug`, `parentSlug`, `name`,
  `nameRu`, `description`, `sortOrder`, `active`, `paid`), без фреймворков.
- `application.topic.GrammarTopicCatalog` — загружает машиночитаемый ресурс
  `exercise/topics/grammar-topics.json` при старте (fail-fast: дубликат slug, битый
  `parentSlug`, цикл → `IllegalStateException`) и предоставляет запросы по активному
  v1-подмножеству (`isActive = true`): корни, дети, поиск по англ. названию (case-insensitive),
  путь от корня, наличие активных детей, `displayTopic(ExerciseTopic)`.
- `config.properties.ExerciseTopicsProperties` — `eta.exercise.topics.path` (default
  `/exercise/topics/grammar-topics.json`).

### API (публичные интерфейсы)

| Интерфейс | Метод |
|-----------|-------|
| `GenerateExercise` | `ExerciseDto execute(GenerateExerciseCommand)` |
| `CheckExercise` | `CheckExerciseResult execute(CheckExerciseCommand)` |
| `ExerciseTopics` | read-порт каталога для chatbot: `roots()`, `children(slug)`, `search(query)`, `exists(slug)`, `find(slug)`, `parent(slug)`, `path(slug)` |
| `ValidateCustomTopic` | `TopicValidationResult validate(ValidateCustomTopicCommand)` |

### Контракт темы
- `sealed interface ExerciseTopic` с `CatalogTopic(String slug)` (предустановленная тема по
  стабильному slug) и `CustomTopic(String name)` («своя тема»).
- `GenerateExerciseCommand` содержит `topic: ExerciseTopic` (вместо прежних `grammarRule`+`topic`)
  и опциональное nullable-поле `thematicTopic` (String; null/blank = «не задана»).
- `ExerciseDto` содержит `topic` (грамматическая тема) и nullable-поле `thematicTopic`
  (тематическая тема), которое восстанавливается и при проверке ответа (`CheckExercise`).
- `TopicView(slug, name, nameRu, hasActiveChildren)` — read-проекция для chatbot.

## Use Cases (Application)

### GenerateExerciseUseCase
- Слова для генерации берутся строго по `command.wordIds()` через
  `FindWords.findByDictionaryIdAndWordIds` (не весь словарь).
- Отображаемая тема строится через `GrammarTopicCatalog.displayTopic(command.topic())`:
  путь каталога для `CatalogTopic` (не-лист — путь + «(раздел: …)» + ограниченный перечень
  активных подтем), текст для `CustomTopic`.
- `Exercise.wordInfoIds = command.wordIds()`; `command.thematicTopic()` пробрасывается в
  `Exercise.create(...)` и далее в `ExerciseDto` (null/blank сохраняются как «не задана»).
- После `repository.save(exercise)` определяются реально использованные слова:
  значение встречается в `item.sentence()` ИЛИ `item.options()` (whole-word без учёта
  регистра, `Pattern.quote` + `\b`) — единый критерий с PDF-блоком used words
  (`generator.GenerateExercisePdfUseCase`).
- Использованные выбранные слова получают `currentRepetitions +1` через порт
  `IncrementWordRepetitions` (дедуп через `Set<WordInfoId>` — ровно +1 за упражнение).
  При ошибке генерации (`ExerciseGenerationException`) запись упражнения и инкременты не выполняются.

### CheckExerciseUseCase
- Проверка ответа по `CheckExerciseCommand`.

### ValidateCustomTopicUseCase
- Валидация свободного ввода «✍️ Своя тема»: детерминированные пре-проверки (trim, пусто,
  длина > 100 → `validated=true, valid=false`, LLM не вызывается), один вызов порта
  `CustomTopicValidator`, верификация `matchedCatalogTopic` по активному каталогу
  (несуществующий slug → `null` + WARN), fail-open при сбое LLM
  (`validated=false, valid=true, canonicalName=trimmed(raw)`).

## Зависимости
```java
@ApplicationModule(allowedDependencies = {"dictionary :: dictionary",
        "dictionary :: word", "shared :: shared"})
```

## Инфраструктура
- `SpringAiExerciseGenerator` — адаптер порта `ExerciseGenerator` на Spring AI
  (`ChatClient`), читает `ExerciseGenerationProperties`; вставляет промпт-фрагмент темы по
  slug через плейсхолдер `{topicGuidance}` (отсутствующий/пустой фрагмент → WARN + базовый
  промпт), user-сообщение «Grammar topic: …» + словарный блок; при непустой тематике добавляется
  строка «General topic: <тематика>» сразу после «Grammar topic:» (при null/blank — отсутствует);
  при ошибке AI или невалидном/пустом ответе бросает `ExerciseGenerationException`
  (исходная причина AI — в `cause`), ничего не сохраняя.
- `SpringAiTopicValidator` (`infrastructure.ai`, `@Component`) — адаптер порта
  `CustomTopicValidator`; промпт `exercise/prompts/topic-validation-system.txt`,
  user-сообщение со списком активных тем каталога (slug + name), структурированный вердикт
  `AiTopicValidationResponse{valid, canonicalName, matchedCatalogTopic, reason}`.
- Ресурсы: `exercise/topics/grammar-topics.json` (каталог), `exercise/topics/prompts/<slug>.txt`
  (фрагменты промптов), `exercise/prompts/*-system.txt` (базовые промпты типов упражнения).
- Persistence (`infrastructure.persistence`, JPA/PostgreSQL): `ExerciseJpa` (`expectedAnswers`/`items` —
  jsonb через `@JdbcTypeCode(SqlTypes.JSON)` + Jackson в `ExerciseJpaMapper`, `wordInfoIds` —
  `@ElementCollection` → `exercise_word_info`), `ExerciseDataRepository` (`JOIN FETCH wordInfoIds`) и
  `JpaExerciseRepository`. Запись сгенерированного упражнения + инкременты повторений — атомарно в
  `TransactionTemplate` (LLM-вызов вне транзакции).
