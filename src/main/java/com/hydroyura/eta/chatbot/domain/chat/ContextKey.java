package com.hydroyura.eta.chatbot.domain.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContextKey {

    ACTIVE_LESSON_ID("activeLessonId"),
    EXERCISE_ID("exerciseId"),
    EXERCISE_TOPIC("exerciseTopic"),
    EXERCISE_THEMATIC_TOPIC("exerciseThematicTopic"),
    EXERCISE_TYPE("exerciseType"),
    TOPIC_SEARCH_QUERY("topicSearchQuery"),
    TOPIC_PAGE("topicPage"),
    TOPIC_NODE("topicNode"),
    TOPIC_CANONICAL_NAME("topicCanonicalName"),
    TOPIC_MATCHED_SLUG("topicMatchedSlug"),
    SELECTED_STUDENT_ID("selectedStudentId"),
    SELECTED_STUDENT_NAME("selectedStudentName"),
    TEACHER_NAME("teacherName"),
    PENDING_STUDENT_NAME("pendingStudentName"),
    WORD_POS("wordPos"),
    WORD_VALUE("wordValue"),
    WORD_TRANSLATIONS("wordTranslations"),
    DICTIONARY_PAGE("dictionaryPage"),
    WORD_SELECTION("wordSelection"),
    WORD_FILTER("wordFilter"),
    SELECTION_PAGE("selectionPage"),
    ACTING_TEACHER_ID("actingTeacherId"),
    SUPPORT_THREAD_ID("supportThreadId"),
    ADMIN_SELECTED_TEACHER_ID("adminSelectedTeacherId"),
    ADMIN_SELECTED_TEACHER_NAME("adminSelectedTeacherName");

    private final String value;

}
