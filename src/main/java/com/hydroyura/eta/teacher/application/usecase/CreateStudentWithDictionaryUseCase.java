package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionaryCommand;
import com.hydroyura.eta.student.api.student.CreateStudent;
import com.hydroyura.eta.student.api.student.CreateStudentCommand;
import com.hydroyura.eta.student.api.student.StudentExistsByNameQuery;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CreateStudentWithDictionaryUseCase implements CreateStudentWithDictionary {

    private final TeacherRepository teacherRepository;

    private final StudentQuery studentQuery;

    private final CreateDictionary createDictionary;

    private final CreateStudent createStudent;

    @Override
    public StudentId execute(CreateStudentWithDictionaryCommand cmd) {
        var teacher = teacherRepository.findById(cmd.teacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + cmd.teacherId()));

        if (studentQuery.existsByName(new StudentExistsByNameQuery(teacher.getStudentIds(), cmd.studentName()))) {
            throw new IllegalArgumentException("Student '" + cmd.studentName() + "' already exists");
        }

        var dictId = createDictionary.execute(new CreateDictionaryCommand(cmd.dictionaryName()));
        var studentId = createStudent.execute(new CreateStudentCommand(cmd.studentName(), dictId));

        teacher.addStudent(studentId);
        teacherRepository.save(teacher);

        log.info("Student '{}' created with dictionary '{}' for teacher {}",
                cmd.studentName(), cmd.dictionaryName(), cmd.teacherId().value());
        return studentId;
    }
}
