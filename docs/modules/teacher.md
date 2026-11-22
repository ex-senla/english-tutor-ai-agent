# Teacher

Учитель. Управляет учениками, ведёт уроки.

## Составляющие

### Teacher (@Entity)
- `id` (@Identity, TeacherId), `name`, `admin` (DB-managed), `onboardingCompleted` (domain-managed),
  `identifiers` (Identifiers), `studentIds` (@Association Set<StudentId>).
- Статические фабрики: `Teacher.create(id, name)` (admin=false, onboardingCompleted=false) и
  `Teacher.reconstitute(id, name, admin, onboardingCompleted)`.
- Методы: `addStudent(StudentId)`, `getStudentIds()` (unmodifiable set), `isAdmin()`,
  `completeOnboarding()` (выставляет `true`, идемпотентен), `isOnboardingCompleted()`.
  Мутатора флага `admin` нет — назначение роли только SQL; `onboardingCompleted` меняется только
  доменным методом `completeOnboarding()`.

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
| `CompleteOnboarding` | `void execute(TeacherId)` |
| `CreateStudentWithDictionary` | `StudentId execute(CreateStudentWithDictionaryCommand)` |
| `FindTeacher` | `findByTelegramChatId(Long) → Optional<TeacherId>`<br>`getStudentIds(Long chatId) → Set<StudentId>` |
| `TeacherDirectory` | `findById(TeacherId)`, `findAll()`, `findTelegramChatId(TeacherId)`, `getStudentIds(TeacherId)` — лёгкие read-проекции без загрузки коллекций (без N+1) |

### CreateStudentWithDictionary
- Оркестрирует создание Student + Dictionary в одной транзакции:
  1. `CreateDictionary` → DictionaryId
  2. `CreateStudent` с этим DictionaryId
  3. `Teacher.addStudent(StudentId)`

## Use Cases (Application)

| Use Case | Статус |
|----------|--------|
| `RegisterTeacher` | ✅ |
| `CompleteOnboarding` | ✅ |
| `CreateStudentWithDictionary` | ✅ |
| `FindTeacherService` | ✅ |

## Зависимости
```java
@ApplicationModule(allowedDependencies = {
    "dictionary :: dictionary", "dictionary :: wordInfo", "student :: student", "student :: lesson"
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
`WordInfoSpecifications`. Имена учеников — детерминированное базовое имя + порядковый суффикс.
Идемпотентность достигается пред-чеком `StudentQuery.existsByName` ДО `CreateStudentWithDictionary`:
существующее имя → WARN и пропуск ученика (без исключений внутри транзакции регистрации).

Тип `PartOfSpeech` для наполнения словаря потребовал добавить зависимость `dictionary :: wordInfo`.

## Инфраструктура
- Persistence (`infrastructure.persistence`, JPA/PostgreSQL): `TeacherJpa` + `TeacherIdentifierJpa`
  (идентификаторы — отдельная таблица `teacher_identifier`, значение хранится строкой и конвертируется
  по `IdentifierType.getValueClass()`), `studentIds` — `@ElementCollection` → `teacher_student`;
  `TeacherDataRepository` (`findWithChildren`/`findByIdentifier` — `LEFT JOIN FETCH` коллекций),
  `TeacherJpaMapper` (diff-sync идентификаторов) и `JpaTeacherRepository`.
