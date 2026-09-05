package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import java.util.Objects;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;
import java.util.Objects;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.util.Objects;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import java.util.Objects;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import java.util.Objects;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import java.util.Objects;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public class RegisterTeacherUseCase implements RegisterTeacher {

    private final TeacherRepository teacherRepository;

    @Override
    public TeacherId execute(RegisterTeacherCommand cmd) {
        var name = Objects.requireNonNull(cmd.name(), "Name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        var teacher = Teacher.create(TeacherId.generate(), cmd.name());
        teacher.getIdentifiers().put(IdentifierType.TELEGRAM, cmd.telegramChatId());
        teacher = teacherRepository.save(teacher);

        log.info("Teacher '{}' registered: {}", cmd.name(), teacher.getId().value());
        return teacher.getId();
    }
}
