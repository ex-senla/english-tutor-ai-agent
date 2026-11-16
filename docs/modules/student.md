# Student

Агрегат: Student + уроки (Lesson).

## Составляющие

### Student (@AggregateRoot)
- `id` (@Identity), `name`, `dictionaryId` (@Association), `deleted` (boolean, default `false`).
- Не знает про Teacher.
- Статическая фабрика: `Student.create(id, dictionaryId, name)` (создаёт живого ученика, флаг `deleted` — `false`).
- Доменные операции: `changeLevel(...)`, `markDeleted()` (идемпотентная установка `deleted = true`).

### Lesson (@Entity, внутри агрегата Student)
- `id` (@Identity), `studentId` (@Association), `name`, `wordInfoIds` (@Association Set<WordId>).
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
| `DeleteStudent` | `void execute(DeleteStudentCommand)` |
| `StudentQuery` | `existsByName(query)`, `findByNameIn(query)`, `getDictionaryId(studentId)`, `findStudentsByIds(ids)`, `searchByNameFragment(query)`, `findStudentDetails(studentId)`, `getCefrLevel(studentId)` |

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
| `DeleteStudent` | ✅ |
| `StartLesson` | ✅ |
| `AddWordToLesson` | ✅ |
| `EndLesson` | ✅ |
| `FindActiveLessonService` | ✅ |
| `StudentQueryService` | ✅ |
| `ChangeStudentLevel` | ✅ |

### StudentQueryService
- `searchByNameFragment(SearchStudentsQuery)` — ищет учеников по подстроке имени без учёта регистра среди заданного множества `studentIds`, возвращает `List<StudentInfo>` в стабильном порядке (по имени). Лимит подсказок не входит в query — это UI-параметр чат-бота.
- Все read-методы (`findStudentsByIds`, `searchByNameFragment`, `findByNameIn`, `findStudentDetails`, `getCefrLevel`, `getDictionaryId`) исключают удалённых учеников (`.filter(s -> !s.isDeleted())`). `existsByName` исключает удалённых на уровне `InMemoryStudentRepository.existsByNameInIds`.

### DeleteStudent
- `DeleteStudentUseCase`: `findById` → «Student not found» (`IllegalArgumentException`) → идемпотентный short-circuit `if (isDeleted) return` → проверка активного урока через `LessonRepository.findActiveByStudentId` (присутствует → `StudentDeletionNotAllowedException`) → `markDeleted()` → `save`.
- Повторное удаление — no-op; активный урок для уже удалённого ученика исключение не бросает.

## Зависимости
```java
@ApplicationModule(allowedDependencies = {
    "dictionary :: dictionary", "dictionary :: wordInfo"
})
```

## Инфраструктура
- `InMemoryStudentRepository` — `ConcurrentHashMap<StudentId, Student>`
- `InMemoryLessonRepository` — `ConcurrentHashMap<LessonId, Lesson>`
