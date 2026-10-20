package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.student.api.student.ChangeStudentLevel;
import com.hydroyura.eta.student.api.student.ChangeStudentLevelCommand;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ChangeStudentLevelUseCase implements ChangeStudentLevel {

    private final StudentRepository studentRepository;

    @Override
    public void execute(ChangeStudentLevelCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.studentId(), "studentId must not be null");
        Objects.requireNonNull(command.level(), "level must not be null");

        var student = studentRepository.findById(command.studentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + command.studentId()));

        student.changeLevel(command.level());
        studentRepository.save(student);

        log.info("Student '{}' level changed to {}", student.getName(), command.level());
    }
}
