package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import java.util.List;

/**
 * Единый перечень таблиц, очищаемых между E2E-сценариями (JUnit и Cucumber), чтобы
 * сценарии были изолированы. Порядок задан с учётом внешних ключей.
 */
public final class E2eTestTables {

    public static final List<String> ALL = List.of(
            "exercise_word_info",
            "word_info_translation",
            "word_info",
            "lesson_word_info",
            "lesson",
            "student",
            "teacher_identifier",
            "teacher_student",
            "teacher",
            "dictionary",
            "exercise",
            "support_message",
            "support_thread",
            "chat");

    private E2eTestTables() {
    }
}
