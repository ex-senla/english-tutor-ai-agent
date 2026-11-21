package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.lesson.StartLessonCommand;
import com.hydroyura.eta.student.domain.student.Lesson;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Transactional
public class StartLessonUseCase implements StartLesson {

    private final LessonRepository lessonRepository;

    @Override
    public LessonId execute(StartLessonCommand cmd) {
        var lesson = Lesson.start(LessonId.generate(), cmd.studentId(), cmd.name());
        lesson = lessonRepository.save(lesson);

        log.info("Lesson '{}' started for student {}", cmd.name(), cmd.studentId().value());
        return lesson.getId();
    }
}
