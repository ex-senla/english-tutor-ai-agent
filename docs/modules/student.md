# Student

Агрегат: Student + уроки (Lesson).

## Составляющие

### Student (@AggregateRoot)
- `id` (@Identity), `name`, `dictionaryId` (@Association).
- Не знает про Teacher.
- Статическая фабрика: `Student.create(id, dictionaryId, name)`.

### Lesson (@Entity, внутри агрегата Student)
- `id` (@Identity), `studentId` (@Association), `name`, `wordIds` (@Association Set<WordId>).
- `status`: ACTIVE / ENDED.
- `startedAt`, `endedAt` (Instant).
- Статическая фабрика: `Lesson.start(id, studentId, name)`.
- Методы: `addWord(WordId)`, `end()`.
- Инварианты: нельзя добавить слово в ENDED, нельзя закончить ENDED.

### LessonStatus (Enum)
ACTIVE, ENDED

## API (публичные интерфейсы)

### Student
| Интерфейс | Метод |
|-----------|-------|
| `CreateStudent` | `StudentId execute(CreateStudentCommand)` |
| `StudentQuery` | `existsByName(query)`, `findByNameIn(query)`, `getDictionaryId(studentId)` |

### Lesson
| Интерфейс | Метод |
|-----------|-------|
| `StartLesson` | `LessonId execute(StartLessonCommand)` |
| `AddWordToLesson` | `void execute(AddWordToLessonCommand)` |
| `EndLesson` | `void execute(EndLessonCommand)` |
| `FindActiveLesson` | `Optional<LessonId> findByStudentId(StudentId)` |

## Use Cases (Application)

| Use Case | Статус |
|----------|--------|
| `CreateStudent` | ✅ |
| `StartLesson` | ✅ |
| `AddWordToLesson` | ✅ |
| `EndLesson` | ✅ |
| `FindActiveLessonService` | ✅ |
| `StudentQueryService` | ✅ |

## Зависимости
```java
@ApplicationModule(allowedDependencies = {
    "dictionary :: dictionary", "dictionary :: word"
})
```

## Инфраструктура
- `InMemoryStudentRepository` — `ConcurrentHashMap<StudentId, Student>`
- `InMemoryLessonRepository` — `ConcurrentHashMap<LessonId, Lesson>`
