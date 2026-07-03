# Dictionary

Словарь принадлежит ученику. Содержит слова, собранные во время уроков.

## Составляющие

### Dictionary (@AggregateRoot)
- Корень агрегата, владеет списком `Word`.
- Поля: `id` (@Identity), `words` (Set<Word>), `name`.
- Инварианты: не допускает дубликатов слов по value (case-insensitive).
- Связь со Student: Student ссылается на DictionaryId. Dictionary не знает про Student.

### Word (@Entity)
- Слово с множеством переводов, частью речи и прогрессом изучения.
- Immutable поля: `id` (@Identity), `value`, `translations` (Set<String>).
- Mutable поля: `partOfSpeech` (PartOfSpeech), `targetRepetitions`, `currentRepetitions`.
- Статус вычисляется: NEW / IN_PROGRESS / LEARNED.
- Методы: `addTranslation()`, `removeTranslation()`, `setPartOfSpeech()`.
- Нельзя удалить последний перевод (LastTranslationException).

### WordFactory
- Фабрика для создания Word с валидацией через `WordSpecifications`.
- Конфигурация спецификаций через `WordSpecificationConfig` → `WordSpecificationSpringProperties`.

### WordStatus (Enum)
NEW, IN_PROGRESS, LEARNED

### Исключения
- `DictionaryDomainException` (базовый)
- `DictionaryNotFoundException`
- `WordAlreadyExistsException`
- `WordDomainException` (базовый)
- `WordValidationException`
- `LastTranslationException`

## API (публичные интерфейсы)

| Интерфейс | Метод |
|-----------|-------|
| `CreateDictionary` | `DictionaryId execute(CreateDictionaryCommand)` |
| `AddWordToDictionary` | `WordId execute(AddWordCommand)` |

## Use Cases (Application)

| Use Case | Статус |
|----------|--------|
| `CreateDictionary` | ✅ |
| `AddWordToDictionary` | ✅ |

### CreateDictionaryUseCase
- Реализует `CreateDictionary` + `@ApplicationModuleListener` для `EntityCreated<Teacher>`.
- При создании Teacher автоматически создаёт Dictionary для каждого Student.

### AddWordToDictionaryUseCase
- Реализует `AddWordToDictionary`. Использует `WordFactory` + `DictionaryRepository`.

## Зависимости
```java
@ApplicationModule(allowedDependencies = "shared :: shared")
```

## Инфраструктура
- `InMemoryDictionaryRepository` — `ConcurrentHashMap<DictionaryId, Dictionary>`
