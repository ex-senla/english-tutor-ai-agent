# Chatbot State Machine

## Описание состояний

| # | Состояние | Назначение | Как попадаем | Как выходим |
|---|-----------|------------|--------------|-------------|
| S1 | `INITIAL` | Пользователь открыл бота, но не зарегистрирован | Автоматически при первом входе | `/register` → S2 |
| S2 | `AWAITING_REGISTRATION_NAME` | Ожидание имени учителя | Из S1 по `/register` | Ввод имени → S3, `/cancel` → S1 |
| S3 | `ACTIVE` | Зарегистрирован, без активного урока. Idle-состояние | Из S2, S5, S6, S14 | Кнопки/команды → S4, S6, S14 |
| S4 | `AWAITING_STUDENT_NAME` | Ожидание имени нового ученика | Из S3 или S6 по `/new`/кнопке, из S5 при ошибке валидации имени | Ввод имени → S5, `/cancel` → S3 |
| S5 | `AWAITING_STUDENT_LEVEL` | Выбор CEFR-уровня нового ученика (inline-кнопки) | Из S4 вводом имени | `level:<код>` → S3 (ошибка → S4), `/cancel` → S3 |
| S6 | `STUDENTS_LIST` | Inline-кнопки со списком учеников | Из S3 по `/list`, из S7 по `action:back` | `student:<id>` → S7, `/new` → S4, `back:main` → S3 |
| S7 | `STUDENT_OPTIONS` | Меню действий для выбранного ученика | Из S6/S14 по `student:<id>`, из S8/S10/S17 (legacy: S18), из S20 | `action:*` → S8/S10/S15/S19/S6 |
| S8 | `STUDENT_DETAILS` | Карточка ученика: уровень, статистика словаря, активный урок | Из S7 по `action:details`, из S9 | `details:back` → S7, `levelchange` → S9 |
| S9 | `AWAITING_STUDENT_LEVEL_CHANGE` | Выбор нового CEFR-уровня ученика | Из S8 по `levelchange` | `level:<код>` → S8, `back:main` → S8, `/cancel` → S8 |
| S10 | `IN_LESSON` | Активный урок, режим ввода слов | Из S7 по `action:startlesson`, из S11/S12/S13 после слова | Слово → S11, кнопка/`/finishlesson` → S7 (сводка урока) |
| S11 | `AWAITING_POS` | Выбор части речи (inline-кнопки) | Из S10 вводом валидного слова | `pos:*` → S12/S13, `/cancel` → S10 |
| S12 | `AWAITING_TRANSLATION_CHOICE` | Выбор варианта перевода (inline-кнопки от AI) | Из S11 при непустом ответе AI | `translation:<i>` → S10, `translation:own` → S13, `/cancel` → S10 |
| S13 | `AWAITING_MANUAL_TRANSLATION` | Ручной ввод переводов через запятую | Из S11 (пустой AI), из S12 (`translation:own`) | Ввод переводов → S10, `/cancel` → S10 |
| S14 | `STUDENT_SEARCH` | Инкрементальный поиск ученика по фрагменту имени | Из S3 по `/find`/кнопке | Фрагмент → S14, `student:<id>` → S7, `back:main` → S3, `/cancel` → S3 |
| S15 | `AWAITING_EXERCISE_TYPE` | Выбор типа упражнения (inline-кнопки) | Из S7 по `action:exercise` | `exercise:*` → S16, `/cancel` → S7 |
| S16 | `AWAITING_EXERCISE_GRAMMAR` | Ввод грамматического правила | Из S15 | Ввод правила → S17, `/cancel` → S7 |
| S17 | `AWAITING_EXERCISE_TOPIC` | Ввод темы упражнения | Из S16 | Ввод темы → сохранить тему + предвыбор слов → S20, `/cancel` → S7 |
| S18 | `AWAITING_EXERCISE_ANSWER` | Ожидание ответа на упражнение (legacy) | — в текущем PDF-потоке **не достигается** | Ввод ответа → S7, `/cancel` → S7 |
| S19 | `STUDENT_DICTIONARY` | Меню «Словарь»: все слова ученика с пагинацией и сбросом | Из S7 по `action:dictionary` | `wdpg:prev/next` → S19, `wdreset:<uuid>` → S19, `wdback` → S8, `/cancel` → S8 |
| S20 | `AWAITING_EXERCISE_WORD_SELECTION` | Шаг выбора слов для упражнения | Из S17 вводом темы | `exword:<uuid>`/`exfilter:<status>`/`expg:prev/next`/`exback` → S20, `exdone`/`excont` → генерация + PDF → S7, `exselback`/`/cancel` → S7 |

> **Примечание к S18.** Переход `InputAwaitingExerciseAnswerTransition` и дефолтный `Handler`
> зарегистрированы, но ни один переход больше не переводит чат в `AWAITING_EXERCISE_ANSWER`:
> интерактивный поток заменён отправкой упражнения PDF-документами (S17 → S20 → S7).

## Конфиг состояний

Каждое состояние определяет, какие триггеры обработаны переходами; всё остальное уходит
в дефолтный `Handler` состояния. Команда `/cancel` зарегистрирована во всех 20 состояниях.

| # | Состояние | Команды | Кнопки (reply) | Ввод | Callback |
|---|-----------|---------|----------------|------|----------|
| S1 | `INITIAL` | `/register` → S2, `/start`,`/help` → приветствие, `/cancel` → «Нет активного действия» | — | дефолт | — |
| S2 | `AWAITING_REGISTRATION_NAME` | `/cancel` → S1 | — | ✅ `RegisterTeacher` → S3 | — |
| S3 | `ACTIVE` | `/new` → S4, `/list` → S6, `/find` → S14, `/help`,`/start` → меню, `/cancel` → «Нет активного действия» | «➕ Новый студент» → S4, «👥 Мои студенты» → S6, «🔍 Найти ученика» → S14 | дефолт | — |
| S4 | `AWAITING_STUDENT_NAME` | `/cancel` → S3 | — | ✅ сохранить `pendingStudentName` → S5 | — |
| S5 | `AWAITING_STUDENT_LEVEL` | `/cancel` → S3 | — | дефолт | `level:<A1…C2>` → `CreateStudentWithDictionary` → S3 (ошибка → S4) |
| S6 | `STUDENTS_LIST` | `/new` → S4, `/list`,`/help` → обновить список, `/cancel` → «Нет активного действия» | — | дефолт | `student:<id>` → S7, `back:main` → S3 |
| S7 | `STUDENT_OPTIONS` | `/cancel` → «Нет активного действия» | — | дефолт | `action:startlesson` → S10, `action:details` → S8, `action:dictionary` → S19, `action:exercise` → S15, `action:back` → S6 |
| S8 | `STUDENT_DETAILS` | `/cancel` → «Нет активного действия» | — | дефолт | `details:back` → S7, `levelchange` → S9 |
| S9 | `AWAITING_STUDENT_LEVEL_CHANGE` | `/cancel` → S8 | — | дефолт | `level:<A1…C2>` → `ChangeStudentLevel` → S8, `back:main` → S8 |
| S10 | `IN_LESSON` | `/finishlesson` → S7 (сводка), `/help` → подсказка, `/cancel` → «Нет активного действия» | «🏁 Завершить урок» → S7 | ✅ `CheckWord` → S11 (невалидное слово — остаёмся) | — |
| S11 | `AWAITING_POS` | `/cancel` → S10 | — | дефолт | `pos:noun`/`pos:verb`/`pos:adjective` → S12/S13 |
| S12 | `AWAITING_TRANSLATION_CHOICE` | `/cancel` → S10 | — | дефолт | `translation:<i>` → S10, `translation:own` → S13 |
| S13 | `AWAITING_MANUAL_TRANSLATION` | `/cancel` → S10 | — | ✅ `AddWordToDictionary` + `AddWordToLesson` → S10 | — |
| S14 | `STUDENT_SEARCH` | `/cancel` → S3 | — | ✅ `searchByNameFragment` → S14 | `student:<id>` → S7, `back:main` → S3 |
| S15 | `AWAITING_EXERCISE_TYPE` | `/cancel` → S7 | — | дефолт | `exercise:fillintheblank`, `exercise:multiplechoice` → S16 |
| S16 | `AWAITING_EXERCISE_GRAMMAR` | `/cancel` → S7 | — | ✅ сохранить `grammarRule` → S17 | — |
| S17 | `AWAITING_EXERCISE_TOPIC` | `/cancel` → S7 | — | ✅ сохранить `exerciseTopic` + предвыбор `IN_PROGRESS` → S20 | — |
| S18 | `AWAITING_EXERCISE_ANSWER` | `/cancel` → S7 | — | ✅ `CheckExercise` → S7 (legacy) | — |
| S19 | `STUDENT_DICTIONARY` | `/cancel` → S8 | — | дефолт | `wdpg:prev/next` → S19, `wdreset:<uuid>` → S19, `wdback` → S8 |
| S20 | `AWAITING_EXERCISE_WORD_SELECTION` | `/cancel` → S7 | — | дефолт | `exword:<uuid>`/`exfilter:<status>`/`expg:prev/next`/`exback` → S20, `exdone`/`excont` → `GenerateExercise` + `GenerateExercisePdf` → S7, `exselback` → S7 |

### Команда `/cancel`

Регистрируется во всех 20 состояниях через `CancelCmdTransition` (14 диалоговых состояний) и
`NoCancelCmdTransition` (6 не-диалоговых). Правила отмены:

| Состояние | Возврат | Удаляемые ключи |
|-----------|---------|-----------------|
| S2 `AWAITING_REGISTRATION_NAME` | S1 | — |
| S4 `AWAITING_STUDENT_NAME` | S3 | `pendingStudentName` |
| S5 `AWAITING_STUDENT_LEVEL` | S3 | `pendingStudentName` |
| S9 `AWAITING_STUDENT_LEVEL_CHANGE` | S8 | — |
| S11 `AWAITING_POS` | S10 | `wordValue` |
| S12 `AWAITING_TRANSLATION_CHOICE` | S10 | `wordValue`, `wordPos`, `wordTranslations` |
| S13 `AWAITING_MANUAL_TRANSLATION` | S10 | `wordValue`, `wordPos`, `wordTranslations` |
| S14 `STUDENT_SEARCH` | S3 | — |
| S15 `AWAITING_EXERCISE_TYPE` | S7 | — |
| S16 `AWAITING_EXERCISE_GRAMMAR` | S7 | `exerciseType` |
| S17 `AWAITING_EXERCISE_TOPIC` | S7 | `exerciseType`, `grammarRule` |
| S18 `AWAITING_EXERCISE_ANSWER` | S7 | `exerciseId`, `exerciseTopic`, `exerciseType` |
| S19 `STUDENT_DICTIONARY` | S8 | `dictionaryPage` |
| S20 `AWAITING_EXERCISE_WORD_SELECTION` | S7 | `exerciseType`, `grammarRule`, `exerciseTopic`, `wordSelection`, `wordFilter`, `selectionPage` |

Общие ключи (`selectedStudentId`, `selectedStudentName`, `activeLessonId`, `teacherName`)
никогда не удаляются.

### Дефолтные Handler'ы

| Состояние | Ответ |
|-----------|-------|
| S1 `INITIAL` | «Добро пожаловать! Для начала зарегистрируйтесь: /register» |
| S2 `AWAITING_REGISTRATION_NAME` | «Введите ваше имя» |
| S3 `ACTIVE` | «Неизвестная команда. /help — список команд» |
| S4 `AWAITING_STUDENT_NAME` | «Введите имя ученика» |
| S5 `AWAITING_STUDENT_LEVEL` | «Выберите уровень ученика (CEFR):» |
| S6 `STUDENTS_LIST` | «Выберите ученика кнопками ниже» |
| S7 `STUDENT_OPTIONS` | «Используйте кнопки ниже» |
| S8 `STUDENT_DETAILS` | «Используйте кнопки ниже» |
| S9 `AWAITING_STUDENT_LEVEL_CHANGE` | «Выберите новый уровень (CEFR):» |
| S10 `IN_LESSON` | «Просто отправьте слово — добавим его в словарь. …» |
| S11 `AWAITING_POS` | «Выберите часть речи кнопками ниже» |
| S12 `AWAITING_TRANSLATION_CHOICE` | «Используйте кнопки ниже» |
| S13 `AWAITING_MANUAL_TRANSLATION` | «Введите переводы через запятую…» |
| S14 `STUDENT_SEARCH` | «🔍 Введите фрагмент имени ученика» |
| S15 `AWAITING_EXERCISE_TYPE` | «Выберите тип упражнения кнопками ниже» |
| S16 `AWAITING_EXERCISE_GRAMMAR` | «Введите грамматическое правило» |
| S17 `AWAITING_EXERCISE_TOPIC` | «Введите тему упражнения» |
| S18 `AWAITING_EXERCISE_ANSWER` | «Введите ваш ответ на упражнение» |
| S19 `STUDENT_DICTIONARY` | «Используйте кнопки ниже» |
| S20 `AWAITING_EXERCISE_WORD_SELECTION` | «Используйте кнопки ниже» |

## Принцип работы

### Обработка Update (`EnglishTutorBot.onUpdateReceived`)

```
skip my_chat_member
chatId  = extractChatId(update)
chat    = chatService.getOrCreate(chatId)
action  = updateParser.parseUpdate(update)
indicator = showProcessingIndicator(chat, action)   // S17 + ввод: «⏳ Генерирую упражнение…»
oldState = chat.getState()
result  = stateMachine.applyAction(chat, action)
newState = chat.getState()
deleteProcessingIndicator(indicator)
chatService.save(chat)
cleanupMessageId -> DeleteMessage
send documents / convert -> SendMessage | EditMessageText | DeleteMessage | SendDocument
```

- **Удаление reply-клавиатуры** при выходе из `ACTIVE`/`IN_LESSON` (смена oldState→newState).
- **`SendDocuments`** — PDF упражнения отправляются по одному, затем текст меню ученика.

`StateMachine.applyAction` — чистая маршрутизация по четырём таблицам; переход не найден →
дефолтный `Handler`.

## Контекст состояния (Map<String, Object>)

Типизированные ключи — enum `ContextKey` (включая `PENDING_STUDENT_NAME`, `WORD_TRANSLATIONS`,
`DICTIONARY_PAGE`, `WORD_SELECTION`, `WORD_FILTER`, `SELECTION_PAGE`).

| Состояние | Ключи |
|-----------|-------|
| S3 `ACTIVE` | `TEACHER_NAME` |
| S4/S5 | `PENDING_STUDENT_NAME` (удаляется после создания/отмены) |
| S7 `STUDENT_OPTIONS` | `SELECTED_STUDENT_ID`, `SELECTED_STUDENT_NAME` |
| S8/S9 | те же |
| S10 `IN_LESSON` | + `ACTIVE_LESSON_ID` |
| S11 `AWAITING_POS` | + `WORD_VALUE` |
| S12 `AWAITING_TRANSLATION_CHOICE` | `WORD_VALUE`, `WORD_POS`, `WORD_TRANSLATIONS` |
| S13 `AWAITING_MANUAL_TRANSLATION` | `WORD_VALUE`, `WORD_POS` (может оставаться `WORD_TRANSLATIONS`) |
| S15 | `SELECTED_*`; после выбора — `EXERCISE_TYPE` |
| S16 | + `GRAMMAR_RULE` |
| S17 | `EXERCISE_TYPE`, `GRAMMAR_RULE`; после ввода темы — `EXERCISE_TOPIC` |
| S18 | `EXERCISE_ID`, `EXERCISE_TOPIC`, `EXERCISE_TYPE` (legacy) |
| S19 `STUDENT_DICTIONARY` | + `DICTIONARY_PAGE` |
| S20 `AWAITING_EXERCISE_WORD_SELECTION` | `EXERCISE_TYPE`, `GRAMMAR_RULE`, `EXERCISE_TOPIC`, `WORD_SELECTION`, `WORD_FILTER`, `SELECTION_PAGE` |

## Inline-кнопки и callback data

| Экран | Кнопки | Callback data |
|-------|--------|---------------|
| S5/S9: уровень | `[A1] … [C2]` | `level:<код>` |
| S6: список учеников | `[имя]` + `[◀ Back]` | `student:<uuid>`, `back:main` |
| S7: меню ученика | `[▶ Start Lesson]` `[📋 Details]` `[📚 Словарь]` `[🎯 Exercise]` `[◀ Back]` | `action:*` |
| S8: карточка ученика | `[🔄 Изменить уровень]` `[◀ Back]` | `levelchange`, `details:back` |
| S19: словарь | `[↺ значение]` (LEARNED), `[◀]` `[▶]`, `[🔙 Назад]` | `wdreset:<uuid>`, `wdpg:prev/next`, `wdback` |
| S20: выбор слов | `[✓/· значение (POS, статус)]`, `[🔄 In progress]` `[🆕 New]` `[✅ Learned]` `[📋 All]`, `[◀]` `[▶]`, `[✔️ Готово]`, `[🔙 Назад]` | `exword:<uuid>`, `exfilter:<status>`, `expg:prev/next`, `exdone`, `exselback` |
| S11: часть речи | `[📛 Noun]` `[🏃 Verb]` `[🎨 Adjective]` | `pos:noun`, `pos:verb`, `pos:adjective` |
| S12: переводы | `[вариант]` + `[✍️ Свой перевод]` | `translation:<index>`, `translation:own` |
| S14: поиск | `[имя]` + `[◀ Back]` | `student:<uuid>`, `back:main` |
| S15: тип упражнения | `[✏️ Fill in the blank]` `[🔤 Multiple choice]` | `exercise:fillintheblank`, `exercise:multiplechoice` |
