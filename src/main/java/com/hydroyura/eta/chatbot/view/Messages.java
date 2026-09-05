package com.hydroyura.eta.chatbot.view;

public final class Messages {

    public static final String CHOOSE_EXERCISE_TYPE = "Выберите тип упражнения кнопками ниже";

    public static final String CHOOSE_EXERCISE_TOPIC = "Введите тему упражнения (например, 'Animals')";

    public static final String CHOOSE_STUDENT = "Выберите ученика кнопками ниже";

    public static final String CHOOSE_POS = "Выберите часть речи кнопками ниже";

    public static final String EXERCISE_NOT_FOUND = "⚠️ Упражнение не найдено. Начните заново.";

    public static final String EXERCISE_ENTER_ANSWER = """
            %s | Тема: %s

            %s

            ✍️ Введите ваш ответ:""";

    public static final String FILL_IN_THE_BLANK = "✏️ Fill in the blank";

    public static final String ENTER_ANOTHER_NAME = "❌ %s. Введите другое имя:";

    public static final String ENTER_STUDENT_NAME = "Введите имя ученика";

    public static final String ENTER_NEW_STUDENT_NAME = "Введите имя нового ученика";

    public static final String ENTER_YOUR_NAME = "Введите ваше имя";

    public static final String ENTER_TRANSLATIONS = "Введите переводы через запятую";

    public static final String ENTER_WORD_IN_ENGLISH = "Введите слово на английском";

    public static final String ENTER_YOUR_ANSWER = "Введите ваш ответ на упражнение";

    public static final String ENTER_EXERCISE_TOPIC = "Введите тему упражнения";

    public static final String LESSON = "Урок %s";

    public static final String LESSON_INSTRUCTION = """
            %s — добавить слово
            %s — завершить урок""".formatted(Buttons.ADD_WORD, Buttons.FINISH_LESSON);

    public static final String LESSON_STARTED = "Урок начат для %s!";

    public static final String STUDENT_ADDED = "✅ Ученик '%s' добавлен!";

    public static final String STUDENT_FEEDBACK = """
            🎯 Ученик: %s

            %s""";

    public static final String WORD_POS_TRANSLATIONS = """
            ✅ Слово '%s' (%s)
            Переводы: %s""";

    public static final String MULTIPLE_CHOICE = "🔤 Multiple choice";

    public static final String NO_STUDENTS = """
            У вас пока нет учеников.
            %s — добавить ученика""".formatted(Buttons.NEW_STUDENT);

    public static final String UNKNOWN_COMMAND = "Неизвестная команда. /help — список команд";

    public static final String USE_BUTTONS_BELOW = "Используйте кнопки ниже";

    public static final String WELCOME = "Добро пожаловать! Для начала зарегистрируйтесь: /register";

}
