package com.hydroyura.eta.chatbot.view;

import java.util.Set;

public final class Buttons {

    public static final String NEW_STUDENT = "➕ Новый студент";

    public static final String LIST_STUDENT = "👥 Мои студенты";

    public static final String FIND_STUDENT = "🔍 Найти ученика";

    public static final String FINISH_LESSON = "🏁 Завершить урок";

    public static final String START_LESSON = "▶ Start Lesson";

    public static final String DETAILS = "📋 Details";

    public static final String CHANGE_LEVEL = "🔄 Изменить уровень";

    public static final String DELETE_STUDENT = "🗑 Удалить ученика";

    public static final String DELETE_CONFIRM = "✅ Да, удалить";

    public static final String DELETE_CANCEL = "🔙 Отмена";

    public static final String EXERCISE = "🎯 Exercise";

    public static final String BACK = "◀ Back";

    public static final String NOUN = "📛 Noun";

    public static final String VERB = "🏃 Verb";

    public static final String ADJECTIVE = "🎨 Adjective";

    public static final String EXPRESSION = "💬 Выражение";

    public static final String CHANGE_POS = "🔁 Изменить часть речи";

    public static final String SEARCH = "🔍 Поиск";

    public static final String SELF_TOPIC = "✍️ Своя тема";

    public static final String TOPIC_CHOOSE = "✔️ Выбрать эту тему";

    public static final String TOPIC_CONFIRM_USE = "✓ Использовать";

    public static final String TOPIC_CONFIRM_REENTER = "✍️ Ввести заново";

    public static final String TOPIC_CONFIRM_CATALOG = "📚 Использовать тему каталога";

    public static final String TOPIC_BACK = "🔙 Назад";

    public static final String TOPIC_SKIP = "⏭ Пропустить";

    public static final String TOPIC_CANCEL = "🔙 Отмена";

    public static final String OWN_TRANSLATION = "✍️ Свой перевод";

    public static final String DICTIONARY = "📚 Словарь";

    public static final String PREV = "◀";

    public static final String NEXT = "▶";

    public static final String RESET = "↺";

    public static final String DONE = "✔️ Готово";

    public static final String CONTINUE = "▶️ Продолжить";

    public static final String BACK_TO_SELECTION = "🔙 Вернуться к выбору";

    public static final String BACK_TO_STUDENT = "🔙 Назад";

    public static final Set<String> REPLY_BUTTONS = Set.of(NEW_STUDENT, LIST_STUDENT, FIND_STUDENT, FINISH_LESSON);

    public static Set<String> getReplyButtons() {
        return REPLY_BUTTONS;
    }

}
