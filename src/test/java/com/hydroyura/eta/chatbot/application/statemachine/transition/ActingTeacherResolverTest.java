package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.shared.api.DomainException;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTING_TEACHER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActingTeacherResolverTest {

    private final FindTeacher findTeacher = mock(FindTeacher.class);

    private final TeacherDirectory teacherDirectory = mock(TeacherDirectory.class);

    private final ActingTeacherResolver resolver = new ActingTeacherResolver(findTeacher, teacherDirectory);

    @Test
    void shouldResolveTeacherByChatIdWithoutImpersonation() {
        var teacherId = TeacherId.generate();
        when(findTeacher.findByTelegramChatId(1L)).thenReturn(Optional.of(teacherId));

        assertThat(resolver.actingTeacherId(chat(), 1L)).isEqualTo(teacherId);
        assertThat(resolver.isImpersonating(chat())).isFalse();
    }

    @Test
    void shouldResolveActingTeacherWhenImpersonating() {
        var acting = TeacherId.generate();
        var chat = chat();
        chat.getContext().put(ACTING_TEACHER_ID.getValue(), acting.value().toString());

        assertThat(resolver.actingTeacherId(chat, 1L)).isEqualTo(acting);
        assertThat(resolver.isImpersonating(chat)).isTrue();
    }

    @Test
    void shouldReturnActingStudentIdsWhenImpersonating() {
        var acting = TeacherId.generate();
        var studentId = StudentId.generate();
        when(teacherDirectory.getStudentIds(acting)).thenReturn(Set.of(studentId));
        var chat = chat();
        chat.getContext().put(ACTING_TEACHER_ID.getValue(), acting.value().toString());

        assertThat(resolver.actingStudentIds(chat, 1L)).containsExactly(studentId);
    }

    @Test
    void shouldDelegateToFindTeacherForStudentIdsWithoutImpersonation() {
        var studentId = StudentId.generate();
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(studentId));

        assertThat(resolver.actingStudentIds(chat(), 1L)).containsExactly(studentId);
    }

    @Test
    void shouldThrowOnInvalidActingTeacherId() {
        var chat = chat();
        chat.getContext().put(ACTING_TEACHER_ID.getValue(), "not-a-uuid");

        assertThatThrownBy(() -> resolver.actingTeacherId(chat, 1L))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldReturnEmptyStudentsForChatWithoutTeacher() {
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of());

        assertThat(resolver.actingStudentIds(chat(), 1L)).isEmpty();
    }

    @Test
    void shouldThrowForMissingTeacherWhenRequired() {
        when(findTeacher.findByTelegramChatId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.actingTeacherId(chat(), 1L))
                .isInstanceOf(DomainException.class);
    }

    private Chat chat() {
        return Chat.ofDefaults(new ChatId(1L));
    }
}
