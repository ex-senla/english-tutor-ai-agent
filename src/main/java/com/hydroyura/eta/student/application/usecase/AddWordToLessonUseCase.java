package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.AddWordToLessonCommand;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AddWordToLessonUseCase implements AddWordToLesson {

    private final LessonRepository lessonRepository;

    @Override
    public void execute(AddWordToLessonCommand cmd) {
        var lesson = lessonRepository.findById(cmd.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + cmd.lessonId()));

        lesson.addWord(cmd.wordId());
        lessonRepository.save(lesson);

        log.info("Word added to lesson {}", cmd.lessonId().value());
    }
}
