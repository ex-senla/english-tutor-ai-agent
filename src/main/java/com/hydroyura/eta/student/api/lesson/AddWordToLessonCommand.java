package com.hydroyura.eta.student.api.lesson;

import com.hydroyura.eta.dictionary.api.word.WordId;

public record AddWordToLessonCommand(
        LessonId lessonId,
        WordId wordId
) {
}
