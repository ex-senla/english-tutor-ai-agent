package com.hydroyura.eta.teacher.api.teacher;

import com.hydroyura.eta.student.api.student.StudentId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Read-модель учителей для списков и доставки сообщений.
 *
 * <p>В отличие от {@link FindTeacher} (chatId-driven), работает по {@link TeacherId} и не
 * загружает тяжёлые агрегаты с коллекциями — только лёгкие проекции.</p>
 */
public interface TeacherDirectory {

    Optional<TeacherInfo> findById(TeacherId id);

    List<TeacherInfo> findAll();

    Optional<Long> findTelegramChatId(TeacherId id);

    Set<StudentId> getStudentIds(TeacherId id);
}
