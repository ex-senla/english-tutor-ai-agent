package com.hydroyura.eta.teacher.domain.teacher;

import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.TeacherInfo;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TeacherRepository {

    Teacher save(Teacher teacher);

    Optional<Teacher> findById(TeacherId id);

    Optional<Teacher> findByIdentifier(IdentifierType type, Object value);

    List<TeacherInfo> findAllInfos();

    Optional<Long> findTelegramChatId(TeacherId id);

    Set<StudentId> findStudentIds(TeacherId id);
}
