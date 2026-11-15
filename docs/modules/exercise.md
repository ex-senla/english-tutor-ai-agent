# Exercise

Упражнения, генерируемые на основе словаря ученика.

## Составляющие

### Exercise (@Entity)
- `id` (@Identity, ExerciseId), `type` (ExerciseType), `topic`, ассоциация
  `wordInfoIds` (`@Association`, `Set<WordInfoId>`), `content`, `expectedAnswers`,
  `items`, `status` (ExerciseStatus).
- Статическая фабрика: `Exercise.create(id, type, topic, wordInfoIds)`.

### API (публичные интерфейсы)

| Интерфейс | Метод |
|-----------|-------|
| `GenerateExercise` | `ExerciseDto execute(GenerateExerciseCommand)` |
| `CheckExercise` | `CheckExerciseResult execute(CheckExerciseCommand)` |

### GenerateExerciseCommand
- `type`, `grammarRule`, `topic`, `dictionaryId`, `cefrLevel`, `wordIds`
  (`Set<WordInfoId>` — выбранные преподавателем слова; может быть пустым для
  генерации без слов словаря).

## Use Cases (Application)

### GenerateExerciseUseCase
- Слова для генерации берутся строго по `command.wordIds()` через
  `FindWords.findByDictionaryIdAndWordIds` (не весь словарь).
- `Exercise.wordInfoIds = command.wordIds()`.
- После `repository.save(exercise)` определяются реально использованные слова:
  значение встречается в `item.sentence()` ИЛИ `item.options()` (whole-word без учёта
  регистра, `Pattern.quote` + `\b`) — единый критерий с PDF-блоком used words
  (`generator.GenerateExercisePdfUseCase`).
- Использованные выбранные слова получают `currentRepetitions +1` через порт
  `IncrementWordRepetitions` (дедуп через `Set<WordInfoId>` — ровно +1 за упражнение).
  Fallback-упражнение (items пуст) инкрементов не даёт.

### CheckExerciseUseCase
- Проверка ответа по `CheckExerciseCommand`.

## Зависимости
```java
@ApplicationModule(allowedDependencies = "dictionary :: dictionary")
```

## Инфраструктура
- `SpringAiExerciseGenerator` — адаптер порта `ExerciseGenerator` на Spring AI
  (`ChatClient`), читает `ExerciseGenerationProperties`; при ошибке/невалидном ответе AI
  возвращает fallback-упражнение с пустыми `items`.
- `InMemoryExerciseRepository` — `ConcurrentHashMap<ExerciseId, Exercise>`
