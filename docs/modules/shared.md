# Shared

Общий модуль с кроссплатформенными контрактами. Доступен всем остальным модулям.

## Составляющие

### NamedInterface: `shared`

### Specification\<T\> (Interface)
- Паттерн Specification для валидации и фильтрации.
- Методы: `isSatisfiedBy(T)`, `and()`, `or()`, `not()`.
- Используется в `dictionary` для `WordSpecifications` (валидация слов).

### DomainException (Abstract Class)
- Базовый класс для всех доменных исключений.
- `DomainException(String message)`, `DomainException(String message, Throwable cause)`.
- Все модульные исключения наследуются от него.

## Зависимости
```java
@ApplicationModule(allowedDependencies = {})
```

Не зависит ни от одного другого модуля.
