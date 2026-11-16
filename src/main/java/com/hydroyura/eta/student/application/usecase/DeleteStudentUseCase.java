package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.student.api.student.DeleteStudent;
import com.hydroyura.eta.student.api.student.DeleteStudentCommand;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import com.hydroyura.eta.student.domain.student.exception.StudentDeletionNotAllowedException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DeleteStudentUseCase implements DeleteStudent {

    private final StudentRepository studentRepository;

    private final LessonRepository lessonRepository;

    @Override
    public void execute(DeleteStudentCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.studentId(), "studentId must not be null");

        var student = studentRepository.findById(command.studentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + command.studentId()));

        // Идемпотентность: повторное удаление — no-op, активный урок не проверяется.
        if (student.isDeleted()) {
            return;
        }

        if (lessonRepository.findActiveByStudentId(command.studentId()).isPresent()) {
            throw new StudentDeletionNotAllowedException(
                    "Student has an active lesson: " + command.studentId());
        }

        student.markDeleted();
        studentRepository.save(student);

        log.info("Student '{}' marked as deleted", student.getName());
    }
}
