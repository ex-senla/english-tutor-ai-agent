package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class FindTeacherService implements FindTeacher {

    private final TeacherRepository teacherRepository;

    @Override
    public Optional<TeacherId> findByTelegramChatId(Long chatId) {
        return teacherRepository.findByIdentifier(IdentifierType.TELEGRAM, chatId)
            .map(Teacher::getId);
    }

    @Override
    public Set<StudentId> getStudentIds(Long chatId) {
        return teacherRepository.findByIdentifier(IdentifierType.TELEGRAM, chatId)
            .map(Teacher::getStudentIds)
            .orElse(Set.of());
    }
}
