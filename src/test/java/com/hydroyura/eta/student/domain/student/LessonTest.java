package com.hydroyura.eta.student.domain.student;

import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LessonTest {

    @Test
    void shouldStartLesson() {
        var lesson = Lesson.start(LessonId.generate(), StudentId.generate(), "Lesson 1");

        assertThat(lesson.getStatus()).isEqualTo(LessonStatus.ACTIVE);
        assertThat(lesson.getStartedAt()).isNotNull();
        assertThat(lesson.getEndedAt()).isNull();
        assertThat(lesson.getWordInfoIds()).isEmpty();
    }

    @Test
    void shouldAddWordToActiveLesson() {
        var lesson = Lesson.start(LessonId.generate(), StudentId.generate(), "Lesson 1");
        var wordId = WordInfoId.generate();

        lesson.addWord(wordId);

        assertThat(lesson.getWordInfoIds()).contains(wordId);
    }

    @Test
    void shouldNotAddWordToEndedLesson() {
        var lesson = Lesson.start(LessonId.generate(), StudentId.generate(), "Lesson 1");
        lesson.end();

        assertThatThrownBy(() -> lesson.addWord(WordInfoId.generate()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldEndLesson() {
        var lesson = Lesson.start(LessonId.generate(), StudentId.generate(), "Lesson 1");

        lesson.end();

        assertThat(lesson.getStatus()).isEqualTo(LessonStatus.ENDED);
        assertThat(lesson.getEndedAt()).isNotNull();
    }

    @Test
    void shouldNotEndAlreadyEndedLesson() {
        var lesson = Lesson.start(LessonId.generate(), StudentId.generate(), "Lesson 1");
        lesson.end();

        assertThatThrownBy(lesson::end)
                .isInstanceOf(IllegalStateException.class);
    }
}
