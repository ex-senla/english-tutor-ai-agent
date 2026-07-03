# Exercise

Упражнения, генерируемые на основе словаря ученика.

## Составляющие

### Exercise (@Entity)
- `id` (@Identity, ExerciseId) — пока скелет.
- Статическая фабрика: `Exercise.create(id)`.

## API (публичные интерфейсы)

| Интерфейс | Метод |
|-----------|-------|
| (пока нет) | |

## Use Cases (Application)

Пока нет. Планируется: `GenerateExercise`.

## Зависимости
```java
@ApplicationModule(allowedDependencies = "dictionary :: dictionary")
```

## Инфраструктура
- `InMemoryExerciseRepository` — `ConcurrentHashMap<ExerciseId, Exercise>`

## Статус
⚠️ Скелет. Требуется наполнение: API, use cases, типы упражнений.
