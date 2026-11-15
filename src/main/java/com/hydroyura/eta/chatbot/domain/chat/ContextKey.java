package com.hydroyura.eta.chatbot.domain.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContextKey {

    ACTIVE_LESSON_ID("activeLessonId"),
    EXERCISE_ID("exerciseId"),
    EXERCISE_TOPIC("exerciseTopic"),
    EXERCISE_TYPE("exerciseType"),
    GRAMMAR_RULE("grammarRule"),
    SELECTED_STUDENT_ID("selectedStudentId"),
    SELECTED_STUDENT_NAME("selectedStudentName"),
    TEACHER_NAME("teacherName"),
    PENDING_STUDENT_NAME("pendingStudentName"),
    WORD_POS("wordPos"),
    WORD_VALUE("wordValue"),
    WORD_TRANSLATIONS("wordTranslations");

    private final String value;

}
