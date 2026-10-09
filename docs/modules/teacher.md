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
    "dictionary :: dictionary", "dictionary :: word", "student :: student", "student :: lesson"
})
```

## Демо-наполнение (тестовое)

`DemoStudentsSeeder` (в `application/demo`) после регистрации преподавателя создаёт демо-учеников
со словарями. Управляется конфигурацией, по умолчанию выключено.

Конфигурация (`eta.demo-students.*` в `application.yaml`):
- `enabled` (default `false`) — включает/выключает наполнение;
- `student-count` (default `3`) — количество учеников;
- `min-words-per-student` (default `3`) и `max-words-per-student` (default `5`) — диапазон слов в словаре.

Демо-слова берутся из курируемого каталога `DemoWordCatalog`, который проходит валидацию
`WordSpecifications`. Имена учеников — случайное базовое имя + порядковый суффикс, что гарантирует
уникальность (иначе `CreateStudentWithDictionary` отклонит дубликат имени).

Тип `PartOfSpeech` для наполнения словаря потребовал добавить зависимость `dictionary :: word`.

## Инфраструктура
- `InMemoryTeacherRepository` — `ConcurrentHashMap<TeacherId, Teacher>`
