package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindActiveLessonService implements FindActiveLesson {

    private final LessonRepository lessonRepository;

    @Override
    public Optional<LessonId> findByStudentId(StudentId studentId) {
        return lessonRepository.findActiveByStudentId(studentId)
                .map(l -> l.getId());
    }
}
