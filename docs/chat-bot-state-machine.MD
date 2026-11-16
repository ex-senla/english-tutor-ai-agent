# Chatbot State Machine

## Состояния

| Состояние | Назначение |
|-----------|------------|
| `INITIAL` | Пользователь открыл бота, но не зарегистрирован |
| `AWAITING_REGISTRATION_NAME` | Ожидание имени учителя |
| `ACTIVE` | Зарегистрирован, без активного урока. Idle-состояние |
| `AWAITING_STUDENT_NAME` | Ожидание имени нового ученика |
| `AWAITING_STUDENT_LEVEL` | Выбор CEFR-уровня нового ученика |
| `STUDENTS_LIST` | Inline-кнопки со списком учеников |
| `STUDENT_OPTIONS` | Меню действий для выбранного ученика |
| `STUDENT_DETAILS` | Карточка ученика |
| `AWAITING_STUDENT_LEVEL_CHANGE` | Выбор нового CEFR-уровня ученика |
| `AWAITING_STUDENT_DELETE` | Подтверждение удаления ученика |
| `IN_LESSON` | Активный урок, режим ввода слов |
| `AWAITING_POS` | Ручное меню выбора части речи (fallback) |
| `AWAITING_TRANSLATION_CHOICE` | Экран подтверждения/выбора перевода |
| `AWAITING_MANUAL_TRANSLATION` | Ручной ввод переводов |
| `STUDENT_SEARCH` | Поиск ученика по фрагменту имени |
| `AWAITING_EXERCISE_TYPE` | Выбор типа упражнения |
| `AWAITING_EXERCISE_TOPIC` | Экран выбора грамматической темы (дерево/поиск) |
| `AWAITING_CUSTOM_TOPIC` | Ввод «✍️ Своя тема» |
| `AWAITING_EXERCISE_TOPIC_CONFIRM` | Подтверждение LLM-валидированной «Своей темы» |
| `AWAITING_EXERCISE_THEMATIC_TOPIC` | Ввод тематической темы (свободный текст / «⏭ Пропустить») |
| `AWAITING_EXERCISE_WORD_SELECTION` | Шаг выбора слов для упражнения |
| `AWAITING_EXERCISE_ANSWER` | Ожидание ответа на упражнение (legacy, недостижим) |
| `STUDENT_DICTIONARY` | Меню «Словарь» ученика |

## Флоу упражнения

```
AWAITING_EXERCISE_TYPE
  └─ exercise:<type> → AWAITING_EXERCISE_TOPIC (дерево каталога)
        ├─ topicnav:<slug> → дерево (спуск/назад)
        ├─ topic:<slug> → сохранить CatalogTopic(slug) → AWAITING_EXERCISE_THEMATIC_TOPIC
        ├─ topicsearch → экран поиска; ввод текста → поиск по активным темам
        ├─ topicself → AWAITING_CUSTOM_TOPIC
        ├─ topicpg:prev|next → пагинация дерева/поиска
        └─ /cancel → STUDENT_OPTIONS

AWAITING_CUSTOM_TOPIC
  ├─ ввод текста → пре-проверки → LLM-валидация (ValidateCustomTopic)
  │     ├─ fail-open → CustomTopic(trimmed) → AWAITING_EXERCISE_THEMATIC_TOPIC
  │     ├─ невалидно → показать причину, остаться
  │     └─ валидно → AWAITING_EXERCISE_TOPIC_CONFIRM
  ├─ topicconfirm:back → AWAITING_EXERCISE_TOPIC
  └─ /cancel → STUDENT_OPTIONS

AWAITING_EXERCISE_TOPIC_CONFIRM
  ├─ topicconfirm:use → CustomTopic(canonicalName) → AWAITING_EXERCISE_THEMATIC_TOPIC
  ├─ topiccatalog:<slug> → CatalogTopic(slug) → AWAITING_EXERCISE_THEMATIC_TOPIC
  ├─ topicconfirm:reenter → AWAITING_CUSTOM_TOPIC
  ├─ topicconfirm:back → AWAITING_EXERCISE_TOPIC
  └─ /cancel → STUDENT_OPTIONS

AWAITING_EXERCISE_THEMATIC_TOPIC
  ├─ ввод текста → trim → пусто/длинно (>100): остаться с подсказкой
  │     └─ иначе: сохранить EXERCISE_THEMATIC_TOPIC → AWAITING_EXERCISE_WORD_SELECTION
  ├─ topicskip → (тематика не сохраняется) → AWAITING_EXERCISE_WORD_SELECTION
  └─ /cancel → STUDENT_OPTIONS
```

## Флоу добавления слова

```
IN_LESSON
  ├─ ввод слова → валидация (CheckWord) → one-shot POS+переводы (WordTranslator.suggestPosAndTranslations)
  │     ├─ успех → экран подтверждения (AWAITING_TRANSLATION_CHOICE)
  │     └─ сбой → AWAITING_POS (ручное меню из 4 кнопок)
  ├─ кнопка/команда «Завершить урок» → STUDENT_OPTIONS
  └─ /cancel → «Нет активного действия»

AWAITING_TRANSLATION_CHOICE
  ├─ translation:<i> → добавить слово → IN_LESSON
  ├─ translation:own → AWAITING_MANUAL_TRANSLATION
  ├─ poschange → AWAITING_POS (меню 4 кнопок)
  └─ /cancel → IN_LESSON

AWAITING_POS
  ├─ pos:noun|verb|adjective|expression → translate(word, pos) → AWAITING_TRANSLATION_CHOICE / AWAITING_MANUAL_TRANSLATION
  └─ /cancel → IN_LESSON
```

## Команда `/cancel`

Правила отмены (цель → ключи для удаления):

| Состояние | Возврат | Удаляемые ключи |
|-----------|---------|-----------------|
| `AWAITING_REGISTRATION_NAME` | `INITIAL` | — |
| `AWAITING_STUDENT_NAME` | `ACTIVE` | `pendingStudentName` |
| `AWAITING_STUDENT_LEVEL` | `ACTIVE` | `pendingStudentName` |
| `AWAITING_STUDENT_LEVEL_CHANGE` | `STUDENT_DETAILS` | — |
| `AWAITING_STUDENT_DELETE` | `STUDENT_DETAILS` | — |
| `AWAITING_POS` | `IN_LESSON` | `wordValue` |
| `AWAITING_TRANSLATION_CHOICE` | `IN_LESSON` | `wordValue`, `wordPos`, `wordTranslations` |
| `AWAITING_MANUAL_TRANSLATION` | `IN_LESSON` | `wordValue`, `wordPos`, `wordTranslations` |
| `STUDENT_SEARCH` | `ACTIVE` | — |
| `AWAITING_EXERCISE_TYPE` | `STUDENT_OPTIONS` | — |
| `AWAITING_EXERCISE_TOPIC` | `STUDENT_OPTIONS` | `exerciseType`, `topicSearchQuery`, `topicPage` |
| `AWAITING_CUSTOM_TOPIC` | `STUDENT_OPTIONS` | `exerciseType` |
| `AWAITING_EXERCISE_TOPIC_CONFIRM` | `STUDENT_OPTIONS` | `exerciseType`, `topicCanonicalName`, `topicMatchedSlug` |
| `AWAITING_EXERCISE_THEMATIC_TOPIC` | `STUDENT_OPTIONS` | `exerciseType`, `exerciseTopic`, `exerciseThematicTopic` |
| `AWAITING_EXERCISE_WORD_SELECTION` | `STUDENT_OPTIONS` | `exerciseType`, `exerciseTopic`, `exerciseThematicTopic`, `wordSelection`, `wordFilter`, `selectionPage` |
| `AWAITING_EXERCISE_ANSWER` | `STUDENT_OPTIONS` | `exerciseId`, `exerciseTopic`, `exerciseType` |
| `STUDENT_DICTIONARY` | `STUDENT_DETAILS` | `dictionaryPage` |

## Callback-префиксы

| Префикс | Payload | Назначение |
|---------|---------|-----------|
| `topicnav` | `slug` | навигация по дереву тем / «назад» |
| `topic` | `slug` | выбор темы (лист/не-лист/результат поиска) |
| `topicsearch` | — | вход в поиск темы |
| `topicself` | — | вход в «✍️ Своя тема» |
| `topicpg` | `prev`/`next` | пагинация дерева/поиска |
| `topicconfirm` | `use`/`reenter`/`back` | подтверждение/повтор/назад «Своей темы» |
| `topiccatalog` | `slug` | «📚 Использовать тему каталога» |
| `topicskip` | — | «⏭ Пропустить» на шаге тематической темы |
| `poschange` | — | «🔁 Изменить часть речи» |
| `pos` | `noun`/`verb`/`adjective`/`expression` | ручной выбор части речи |
| `translation` | `index`/`own` | выбор/свой перевод |
| `exercise` | `fillintheblank`/`multiplechoice` | выбор типа упражнения |
| `exword`/`exfilter`/`expg`/`exdone`/`excont`/`exback`/`exselback` | — | шаг выбора слов |
| `wdpg`/`wdreset`/`wdback` | — | словарь |

## Индикаторы обработки

| Состояние + триггер | Индикатор |
|---------------------|-----------|
| `AWAITING_EXERCISE_WORD_SELECTION` + callback `exdone`/`excont` | «⏳ Генерирую упражнение…» |
| `AWAITING_CUSTOM_TOPIC` + Input | «⏳ Проверяю тему…» |
