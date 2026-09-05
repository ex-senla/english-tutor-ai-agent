package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.student.api.lesson.EndLesson;
import com.hydroyura.eta.student.api.lesson.EndLessonCommand;
import com.hydroyura.eta.student.api.lesson.EndLessonResult;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class EndLessonUseCase implements EndLesson {

    private final LessonRepository lessonRepository;

    @Override
    public EndLessonResult execute(EndLessonCommand cmd) {
        var lesson = lessonRepository.findById(cmd.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + cmd.lessonId()));

        var startedAt = lesson.getStartedAt();
        var wordIds = lesson.getWordIds();

        lesson.end();
        lessonRepository.save(lesson);

        log.info("Lesson {} ended", cmd.lessonId().value());

        return new EndLessonResult(startedAt, lesson.getEndedAt(), wordIds);
    }
}
