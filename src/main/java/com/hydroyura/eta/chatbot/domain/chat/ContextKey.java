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
    WORD_POS("wordPos"),
    WORD_VALUE("wordValue");

    private final String value;

}
