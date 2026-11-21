package com.hydroyura.eta.chatbot.view;

public final class Messages {

    public static final String CHOOSE_EXERCISE_TYPE = "Выберите тип упражнения кнопками ниже";

    public static final String TOPIC_SELECTION = "Выберите грамматическую тему:";

    public static final String TOPIC_SEARCH = "🔍 Введите текст для поиска";

    public static final String TOPIC_SEARCH_RESULTS = "Найдено:";

    public static final String TOPIC_SELF_INPUT = "✍️ Введите свою грамматическую тему (до 100 символов):";

    public static final String TOPIC_CONFIRM = "Тема: %s\n\nПодтвердите использование:";

    public static final String TOPIC_INVALID =
            "❌ %s\n\nТема не распознана как грамматическая. Введите заново или вернитесь назад.";

    public static final String TOPIC_EMPTY = "Тема не может быть пустой. Введите тему заново:";

    public static final String TOPIC_TOO_LONG = "Тема слишком длинная (максимум 100 символов). Введите короче:";

    public static final String CHECKING_TOPIC = "⏳ Проверяю тему…";

    public static final String THEMATIC_TOPIC_PROMPT =
            "Введите тематику упражнения (например, Travel, Food, Work) или нажмите ⏭ Пропустить:";

    public static final String THEMATIC_TOPIC_EMPTY =
            "Тематика не может быть пустой. Введите тематику или нажмите ⏭ Пропустить:";

    public static final String THEMATIC_TOPIC_TOO_LONG =
            "Тематика слишком длинная (максимум 100 символов). Введите короче или нажмите ⏭ Пропустить:";

    public static final String CHOOSE_STUDENT = "Выберите ученика кнопками ниже";

    public static final String CANCELLED = "✅ Действие отменено";

    public static final String NOTHING_TO_CANCEL = "Нет активного действия для отмены";

    public static final String SEARCH_PROMPT = "🔍 Введите фрагмент имени ученика";

    public static final String SEARCH_NOTHING_FOUND = "Ничего не найдено";

    public static final String TRANSLATION_SUGGESTIONS_FAILED =
            "⚠️ Не удалось предложить переводы. Введите перевод вручную.";

    public static final String TRANSLATION_PROMPT = "Введите переводы через запятую (например: дом, здание, строение)";

    public static final String WORD_NOT_FOUND = "⚠️ Слово не найдено. Начните заново.";

    public static final String CHOOSE_POS = "Выберите часть речи кнопками ниже";

    public static final String EXERCISE_NOT_FOUND = "⚠️ Упражнение не найдено. Начните заново.";

    public static final String EXERCISE_ENTER_ANSWER = """
            %s | Тема: %s

            %s

            ✍️ Введите ваш ответ:""";

    public static final String EXERCISE_GENERATED = "✅ Упражнение сгенерировано.";

    public static final String GENERATING_EXERCISE = "⏳ Генерирую упражнение…";

    public static final String EXERCISE_PDF_ERROR = "⚠️ Не удалось сформировать PDF упражнения. Попробуйте ещё раз.";

    public static final String FILL_IN_THE_BLANK = "✏️ Fill in the blank";

    public static final String CHOOSE_LEVEL = "Выберите уровень ученика (CEFR):";

    public static final String CHOOSE_NEW_LEVEL = "Выберите новый уровень (CEFR):";

    public static final String ENTER_ANOTHER_NAME = "❌ %s. Введите другое имя:";

    public static final String ENTER_STUDENT_NAME = "Введите имя ученика";

    public static final String ENTER_NEW_STUDENT_NAME = "Введите имя нового ученика";

    public static final String ENTER_YOUR_NAME = "Введите ваше имя";

    public static final String ENTER_TRANSLATIONS = "Введите переводы через запятую";

    public static final String ENTER_WORD_IN_ENGLISH = "Введите слово на английском";

    public static final String ENTER_YOUR_ANSWER = "Введите ваш ответ на упражнение";

    public static final String ENTER_EXERCISE_TOPIC = "Введите тему упражнения";

    public static final String ENTER_EXERCISE_GRAMMAR = "Введите грамматическое правило";

    public static final String INVALID_INPUT = "❌ Некорректный ввод";

    public static final String LESSON = "Урок %s";

    public static final String LESSON_INSTRUCTION = """
            Просто отправьте слово — добавим его в словарь.
            %s — завершить урок""".formatted(Buttons.FINISH_LESSON);

    public static final String LESSON_STARTED = """
            Урок начат для %s!

            """ + LESSON_INSTRUCTION;

    public static final String STUDENT_ADDED = "✅ Ученик '%s' добавлен!";

    public static final String STUDENT_DELETE_CONFIRM = "Удалить ученика \"%s\"? Подтвердите удаление";

    public static final String STUDENT_DELETE_LESSON_ACTIVE = "⚠️ Завершите урок перед удалением";

    public static final String STUDENT_NOT_FOUND_OR_DELETED = "Ученик не найден или удалён";

    public static final String STUDENT_DELETED = "✅ Ученик удалён";

    public static final String STUDENT_FEEDBACK = """
            🎯 Ученик: %s

            %s""";

    public static final String MULTIPLE_CHOICE = "🔤 Multiple choice";

    public static final String NO_STUDENTS = """
            У вас пока нет учеников.
            %s — добавить ученика""".formatted(Buttons.NEW_STUDENT);

    public static final String UNKNOWN_COMMAND = "Неизвестная команда. /help — список команд";

    public static final String USE_BUTTONS_BELOW = "Используйте кнопки ниже";

    public static final String WELCOME = "Добро пожаловать! Для начала зарегистрируйтесь: /register";

    public static final String WORD_ALREADY_EXISTS = """
            🔄 Слово уже есть в словаре '%s' (%s)
            Переводы: %s""";

    public static final String DICTIONARY_EMPTY = "📚 Словарь пуст.";

    public static final String WORD_SELECTION_PROMPT = "Выберите слова для упражнения:";

    public static final String WORD_SELECTION_EMPTY_WARNING =
            "⚠️ Вы не выбрали ни одного слова. Упражнение будет сгенерировано только по теме, без слов из словаря.";

    public static final String WORD_POS_TRANSLATIONS = """
            ✅ Слово '%s' (%s)
            Переводы: %s

            Отправьте следующее слово.
            """ + Buttons.FINISH_LESSON + " — завершить урок";

    // --- личный кабинет ---
    public static final String ACCOUNT_PROFILE = """
            👤 Личный кабинет

            Имя: %s
            Учеников: %d""";

    public static final String ACCOUNT_HINT = "Используйте кнопки: Поддержка, Подписки или Назад";

    public static final String SUBSCRIPTIONS_STUB = """
            Подписки скоро появятся.

            Следите за обновлениями!""";

    // --- поддержка ---
    public static final String SUPPORT_ENTER = """
            💬 Поддержка

            Напишите сообщение — оно будет передано в поддержку.
            Выход — /cancel""";

    public static final String SUPPORT_HINT = "Текст сообщений уходит в поддержку. Выход — /cancel";

    public static final String SUPPORT_SENT = "✅ Сообщение отправлено в поддержку";

    // --- админка ---
    public static final String ADMIN_MENU = "🛡 Админ-панель";

    public static final String ADMIN_MENU_HINT = "Используйте кнопки: Учителя, Обращения или возврат в меню";

    public static final String ADMIN_TEACHERS_HINT = "Выберите учителя из списка или вернитесь назад";

    public static final String ADMIN_TEACHER_STUDENTS_HINT = "Выберите действие: войти от имени учителя или вернуться";

    public static final String ADMIN_THREADS_HINT = "Выберите обращение из списка или вернитесь назад";

    public static final String ADMIN_THREAD_VIEW_HINT = "Введите текст ответа или вернитесь к списку обращений";

    public static final String ADMIN_ACCESS_DENIED = "❌ Недостаточно прав для доступа к админ-панели";

    public static final String ADMIN_THREAD_NOT_FOUND = "Диалог не найден. Вернитесь к списку обращений";

    public static final String SUPPORT_ANSWER_PREFIX = "📬 Ответ поддержки:\n";

    public static final String IMPERSONATION_INDICATOR = "👤 Работа от имени: %s";

}
