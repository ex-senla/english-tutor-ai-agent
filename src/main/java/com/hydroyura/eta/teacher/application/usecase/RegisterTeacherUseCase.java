package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.application.demo.DemoStudentsSeeder;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RegisterTeacherUseCase implements RegisterTeacher {

    private final TeacherRepository teacherRepository;

    private final DemoStudentsSeeder demoStudentsSeeder;

    @Override
    public TeacherId execute(RegisterTeacherCommand cmd) {
        var name = Objects.requireNonNull(cmd.name(), "Name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        var teacher = Teacher.create(TeacherId.generate(), cmd.name());
        teacher.getIdentifiers().put(IdentifierType.TELEGRAM, cmd.telegramChatId());
        teacher = teacherRepository.save(teacher);

        demoStudentsSeeder.seed(teacher.getId());

        log.info("Teacher '{}' registered: {}", cmd.name(), teacher.getId().value());
        return teacher.getId();
    }
}
