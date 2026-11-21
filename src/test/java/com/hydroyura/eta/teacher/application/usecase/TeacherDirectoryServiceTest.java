package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.api.teacher.TeacherInfo;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeacherDirectoryServiceTest {

    private final TeacherRepository repository = mock(TeacherRepository.class);

    private final TeacherDirectoryService service = new TeacherDirectoryService(repository);

    @Test
    void shouldFindAllInfos() {
        var id = TeacherId.generate();
        when(repository.findAllInfos()).thenReturn(List.of(new TeacherInfo(id, "John", true)));

        assertThat(service.findAll()).containsExactly(new TeacherInfo(id, "John", true));
    }

    @Test
    void shouldFindById() {
        var id = TeacherId.generate();
        when(repository.findAllInfos()).thenReturn(List.of(new TeacherInfo(id, "John", true)));

        assertThat(service.findById(id)).contains(new TeacherInfo(id, "John", true));
        assertThat(service.findById(TeacherId.generate())).isEmpty();
    }

    @Test
    void shouldFindTelegramChatId() {
        var id = TeacherId.generate();
        when(repository.findTelegramChatId(id)).thenReturn(Optional.of(777L));

        assertThat(service.findTelegramChatId(id)).contains(777L);
    }

    @Test
    void shouldReturnStudentIds() {
        var id = TeacherId.generate();
        var studentId = StudentId.generate();
        when(repository.findStudentIds(id)).thenReturn(Set.of(studentId));

        assertThat(service.getStudentIds(id)).containsExactly(studentId);
        verify(repository).findStudentIds(id);
    }
}
