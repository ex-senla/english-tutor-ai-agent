# Dictionary

Словарь принадлежит ученику. Содержит слова, собранные во время уроков.

## Составляющие

### Dictionary (@AggregateRoot)
- Корень агрегата, владеет списком `WordInfo`.
- Поля: `id` (@Identity), `words` (Set<Word>), `name`.
- Инварианты: не допускает дубликатов слов по value (case-insensitive).
- Связь со Student: Student ссылается на DictionaryId. Dictionary не знает про Student.

### Word (@Entity)
- Слово с множеством переводов, частью речи и прогрессом изучения.
- Immutable поля: `id` (@Identity), `value`, `translations` (Set<String>).
- Mutable поля: `partOfSpeech` (PartOfSpeech), `targetRepetitions`, `currentRepetitions`,
  `resetCount` (счётчик сбросов обучения, 0 при создании).
- Статус вычисляется: NEW / IN_PROGRESS / LEARNED.
- Методы: `addTranslation()`, `removeTranslation()`, `setPartOfSpeech()`,
  `incrementRepetition()` (currentRepetitions +1, без верхней границы),
  `resetLearning()` (только LEARNED: currentRepetitions = 0, resetCount +1; для NEW/IN_PROGRESS —
  `WordResetNotAllowedException`).
- Нельзя удалить последний перевод (LastTranslationException).

### WordFactory
- Фабрика для создания Word с валидацией через `WordInfoSpecifications`.
- Нормализует значение до валидации: trim + схлопывание внутренних пробелов в один
  (`«  look   forward   to  »` → `«look forward to»`); проверка дублей идёт по нормализованному значению.
- Конфигурация спецификаций через `WordInfoSpecificationConfig` → `WordInfoSpecificationProperties`.
- `PartOfSpeech` включает `NOUN`, `VERB`, `ADJECTIVE`, `EXPRESSION` (русская подпись «Выражение»).

### WordStatus (Enum)
NEW, IN_PROGRESS, LEARNED

### Исключения
- `DictionaryDomainException` (базовый)
- `DictionaryNotFoundException`
- `WordAlreadyExistsException`
- `WordDomainException` (базовый)
- `WordValidationException`
- `LastTranslationException`
- `WordResetNotAllowedException`
- `WordNotFoundException`

## API (публичные интерфейсы)

| Интерфейс | Метод |
|-----------|-------|
| `CreateDictionary` | `DictionaryId execute(CreateDictionaryCommand)` |
| `AddWordToDictionary` | `WordId execute(AddWordCommand)` |
| `WordTranslator` (в `api.word`) | `List<String> translate(WordTranslationCommand)`, `WordPosTranslations suggestPosAndTranslations(String)` |
| `ResetWordLearning` | `void execute(DictionaryId, WordInfoId)` |
| `IncrementWordRepetitions` | `void execute(DictionaryId, Set<WordInfoId>)` |

## Use Cases (Application)

| Use Case | Статус |
|----------|--------|
| `CreateDictionary` | ✅ |
| `AddWordToDictionary` | ✅ |
| `ResetWordLearning` | ✅ |
| `IncrementWordRepetitions` | ✅ |

### CreateDictionaryUseCase
- Реализует `CreateDictionary` + `@ApplicationModuleListener` для `EntityCreated<Teacher>`.
- При создании Teacher автоматически создаёт Dictionary для каждого Student.

### AddWordToDictionaryUseCase
- Реализует `AddWordToDictionary`. Использует `WordInfoFactory` + `DictionaryRepository`.

### ResetWordLearningUseCase
- Реализует `ResetWordLearning`: загружает `Dictionary`, находит `WordInfo` по `WordInfoId`
  (отсутствие → `WordNotFoundException`), вызывает доменную операцию `resetLearning()`
  (для NEW/IN_PROGRESS — `WordResetNotAllowedException`), сохраняет агрегат.

### IncrementWordRepetitionsUseCase
- Реализует `IncrementWordRepetitions`: инкрементирует `currentRepetitions` каждого найденного
  `WordInfo` из набора; отсутствующие id пропускаются с `log.warn`.

## Зависимости
```java
@ApplicationModule(allowedDependencies = "shared :: shared")
```

## Инфраструктура
- Persistence (`infrastructure.persistence`, JPA/PostgreSQL): `DictionaryJpa` + `WordInfoJpa`
  (переводы — `@ElementCollection` → `word_info_translation`), `DictionaryDataRepository`
  (Spring Data, `findWithWordInfos` с `LEFT JOIN FETCH` слов и переводов), `DictionaryJpaMapper`
  (домен↔JPA, diff-sync) и `JpaDictionaryRepository` — адаптер порта `DictionaryRepository`.
- `SpringAiWordTranslator` (`infrastructure.ai`, `@Component`) — адаптер порта `WordTranslator` на Spring AI `ChatClient`; читает `WordTranslationProperties` (`eta.dictionary.translation.variant-count`, default 3) и `WordInfoSpecificationProperties`, валидирует и дедуплицирует варианты, отбрасывает невалидные, при ошибке/пустом ответе AI возвращает пустой список. Метод `suggestPosAndTranslations` (промпт `word-pos-translations-system.txt`) определяет POS (`noun`/`verb`/`adjective`/`expression`) и переводы одним вызовом; неизвестный POS/пустой ответ/пустые переводы/сбой → `partOfSpeech == null`. Использует существующий bean `ChatClient` из `exercise.infrastructure.ai.AiConfig` (скрытая bean-связка `dictionary → exercise`, невидимая для Modulith-verify; перенос `AiConfig` — отдельная задача при третьем потребителе).
