## 1. Dictionary — показать статус слова

- [x] 1.1 Перенести enum `WordStatus` в `dictionary.api.word` и обновить импорты в `Word`, `FindWordsService` и затронутых тестах
- [x] 1.2 Добавить поле `status` в `WordProjection` и заполнять его в `FindWordsService.findByDictionaryId`
- [x] 1.3 Добавить/поправить unit-тесты `FindWordsService` и `WordProjection` на маппинг статуса

## 2. Exercise — команда и фильтрация слов

- [x] 2.1 Добавить обязательное поле `grammarRule` в `GenerateExerciseCommand` (валидация непустоты) и обновить вызывающие места и тесты
- [x] 2.2 Фильтровать проекции по статусу `IN_PROGRESS` в `GenerateExerciseUseCase` перед построением `WordData`
- [x] 2.3 Бросать понятное исключение из `GenerateExerciseUseCase`, когда нет слов со статусом `IN_PROGRESS`

## 3. Exercise — модель ответов

- [x] 3.1 Заменить `AiExerciseResponse(content, expectedAnswer)` на структурированную модель `AiExerciseResponse(List<AiExerciseItem>)`, где `AiExerciseItem(sentence, options, correctAnswer)`
- [x] 3.2 Обновить промпты, чтобы AI возвращал JSON со списком пунктов (sentence + options + correctAnswer), а не строки content/expectedAnswer
- [x] 3.3 Обновить `Exercise` и `ExerciseDto`: хранить ожидаемые ответы по-вопросно (`List<String> expectedAnswers`), `content` собирать из пунктов
- [x] 3.4 Обновить `CheckExerciseUseCase` на по-вопросную проверку с фидбеком по каждому пункту
- [x] 3.5 Валидировать структурированный ответ (список пунктов непуст, у каждого заполнены sentence и correctAnswer); при сбое — fallback
- [x] 3.6 Добавить/поправить тесты модели ответов и по-вопросной проверки

## 4. Exercise — AI-генератор (промпты и конфиг)

- [x] 4.1 Добавить `ExerciseGenerationProperties` (`eta.exercise.generation.sentence-count`, по умолчанию 5) и зарегистрировать в `ExerciseModuleConfig`
- [x] 4.2 Добавить `eta.exercise.generation.sentence-count: 5` в `application.yaml`
- [x] 4.3 Переписать системный промпт FILL_IN_THE_BLANK под правила `examples.MD` §2 (один `___` на предложение, пропуск проверяет грамматическое правило, один однозначный ответ, лексика как контекст) и использовать настроенный счётчик
- [x] 4.4 Переписать системный промпт MULTIPLE_CHOICE под правила `examples.MD` §3 (один `___` на предложение, ровно 4 варианта, один правильный, грамматические дистракторы, распределённые ответы) и использовать настроенный счётчик
- [x] 4.5 Обновить сообщение пользователя генератора, чтобы передавать `grammarRule`, содержательную `topic` и список слов `IN_PROGRESS`
- [x] 4.6 Убрать мёртвую ветку `case MATCHING, TRANSLATION` в `SpringAiExerciseGenerator` (в `ExerciseType` осталось только 2 типа) — чинит компиляцию
- [x] 4.7 Добавить/поправить unit-тесты `SpringAiExerciseGenerator` (сборка промпта, валидация ответа, fallback)

## 5. Chatbot — грамматическое правило и временная заглушка

- [x] 5.1 Добавить состояние `AWAITING_EXERCISE_GRAMMAR` в `ChatState`, обработчик и сообщение `ENTER_EXERCISE_GRAMMAR`
- [x] 5.2 Добавить `InputAwaitingExerciseGrammarTransition` и зарегистрировать в `StateMachineConfig`
- [x] 5.3 Обновить `ExerciseCbAwaitingExerciseTypeTransition`, чтобы переводить в `AWAITING_EXERCISE_GRAMMAR` (вместо темы)
- [x] 5.4 Обновить `InputAwaitingExerciseTopicTransition`, чтобы читать сохранённое грамматическое правило из контекста и передавать его в `GenerateExerciseCommand`, и уточнить промпт темы как содержательную тему
- [x] 5.5 Обработать ошибку «нет слов IN_PROGRESS» в потоке бота дружелюбным сообщением
- [x] 5.6 (временно) Выводить JSON упражнения и ответов в лог, в чат — только «упражнение сгенерировано»; пометить в коде `// TODO (временно)`
- [ ] 5.7 (позже) Вернуть показ упражнения в чат и проверку ответа (`AWAITING_EXERCISE_ANSWER`)

## 6. Проверка

- [x] 6.1 Добавить/поправить тесты `GenerateExerciseUseCase` на фильтрацию и случай «нет слов»
- [x] 6.2 Запустить `./mvnw verify` (включая `ModuleVerificationTest`) и исправить проблемы модульности или компиляции
