package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.student.api.student.CreateStudent;
import com.hydroyura.eta.student.api.student.CreateStudentCommand;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CreateStudentUseCase implements CreateStudent {

    private final StudentRepository studentRepository;

    @Override
    public StudentId execute(CreateStudentCommand cmd) {
        var name = Objects.requireNonNull(cmd.name(), "Name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        var student = Student.create(StudentId.generate(), cmd.dictionaryId(), cmd.name(), cmd.level());
        student = studentRepository.save(student);

        log.info("Student '{}' created: {}", cmd.name(), student.getId().value());
        return student.getId();
    }
}
