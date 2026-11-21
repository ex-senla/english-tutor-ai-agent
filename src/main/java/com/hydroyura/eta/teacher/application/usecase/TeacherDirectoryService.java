package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.api.teacher.TeacherInfo;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherDirectoryService implements TeacherDirectory {

    private final TeacherRepository teacherRepository;

    @Override
    public Optional<TeacherInfo> findById(TeacherId id) {
        return teacherRepository.findAllInfos().stream()
                .filter(info -> info.id().equals(id))
                .findFirst();
    }

    @Override
    public List<TeacherInfo> findAll() {
        return teacherRepository.findAllInfos();
    }

    @Override
    public Optional<Long> findTelegramChatId(TeacherId id) {
        return teacherRepository.findTelegramChatId(id);
    }

    @Override
    public Set<StudentId> getStudentIds(TeacherId id) {
        return teacherRepository.findStudentIds(id);
    }
}
