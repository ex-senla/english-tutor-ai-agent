package com.hydroyura.eta.student.api.lesson;

import com.hydroyura.eta.dictionary.api.word.WordInfoId;

public record AddWordToLessonCommand(
        LessonId lessonId,
        WordInfoId wordInfoId
) {
}
