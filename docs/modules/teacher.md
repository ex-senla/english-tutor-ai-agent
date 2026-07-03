# Teacher

Учитель. Управляет учениками, ведёт уроки.

## Составляющие

### Teacher (@Entity)
- `id` (@Identity, TeacherId), `name`, `identifiers` (Identifiers), `studentIds` (@Association Set<StudentId>).
- Статическая фабрика: `Teacher.create(id, name)`.
- Методы: `addStudent(StudentId)`, `getStudentIds()` (unmodifiable set).

### Identifiers (Value Object)
- Key-value хранилище идентификаторов преподавателя: `Map<IdentifierType, Object>`.
- Используется для связи Telegram chatId ↔ Teacher.
- Методы: `put(type, value)`, `get(type)`, `asMap()`.

### IdentifierType (Enum)
Типы идентификаторов (TELEGRAM_CHAT_ID и др.).

## API (публичные интерфейсы)

| Интерфейс | Метод |
|-----------|-------|
| `RegisterTeacher` | `TeacherId execute(RegisterTeacherCommand)` |
| `CreateStudentWithDictionary` | `StudentId execute(CreateStudentWithDictionaryCommand)` |
| `FindTeacher` | `findByTelegramChatId(Long) → Optional<TeacherId>`<br>`getStudentIds(Long chatId) → Set<StudentId>` |

### CreateStudentWithDictionary
- Оркестрирует создание Student + Dictionary в одной транзакции:
  1. `CreateDictionary` → DictionaryId
  2. `CreateStudent` с этим DictionaryId
  3. `Teacher.addStudent(StudentId)`

## Use Cases (Application)

| Use Case | Статус |
|----------|--------|
| `RegisterTeacher` | ✅ |
| `CreateStudentWithDictionary` | ✅ |
| `FindTeacherService` | ✅ |

## Зависимости
```java
@ApplicationModule(allowedDependencies = {
    "dictionary :: dictionary", "student :: student", "student :: lesson"
})
```

## Инфраструктура
- `InMemoryTeacherRepository` — `ConcurrentHashMap<TeacherId, Teacher>`
