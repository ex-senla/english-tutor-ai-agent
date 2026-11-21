package com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActingTeacherResolver;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.Messages;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudentCbStudentsListTransitionTest {

    @Test
    void shouldSelectExistingStudent() {
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        var studentId = StudentId.generate();
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(studentId));
        when(studentQuery.findStudentsByIds(Set.of(studentId))).thenReturn(List.of(new StudentInfo(studentId, "Anna")));

        var transition = new StudentCbStudentsListTransition(resolver(findTeacher), studentQuery);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENTS_LIST);

        var result = transition.transit(chat, new Action.Callback(Callbacks.STUDENT, studentId.value().toString(), 42));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext().get(SELECTED_STUDENT_ID.getValue())).isEqualTo(studentId.value().toString());
        assertThat(chat.getContext().get(SELECTED_STUDENT_NAME.getValue())).isEqualTo("Anna");
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
    }

    @Test
    void shouldHandleDeletedStudentWithoutPollutingContext() {
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        var deletedId = StudentId.generate();
        var aliveId = StudentId.generate();
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(deletedId, aliveId));
        when(studentQuery.findStudentsByIds(Set.of(deletedId, aliveId)))
                .thenReturn(List.of(new StudentInfo(aliveId, "Bob")));

        var transition = new StudentCbStudentsListTransition(resolver(findTeacher), studentQuery);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENTS_LIST);

        var result = transition.transit(chat, new Action.Callback(Callbacks.STUDENT, deletedId.value().toString(), 42));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENTS_LIST);
        assertThat(chat.getContext()).doesNotContainKeys(SELECTED_STUDENT_ID.getValue(), SELECTED_STUDENT_NAME
                .getValue());
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        assertThat(((ActionResult.EditMessageText) result).text()).isEqualTo(Messages.STUDENT_NOT_FOUND_OR_DELETED);
    }

    @Test
    void shouldHandleNonExistingStudent() {
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        var aliveId = StudentId.generate();
        var unknownId = StudentId.generate();
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(aliveId));
        when(studentQuery.findStudentsByIds(Set.of(aliveId))).thenReturn(List.of(new StudentInfo(aliveId, "Bob")));

        var transition = new StudentCbStudentsListTransition(resolver(findTeacher), studentQuery);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENTS_LIST);

        var result = transition.transit(chat, new Action.Callback(Callbacks.STUDENT, unknownId.value().toString(), 42));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENTS_LIST);
        assertThat(chat.getContext()).doesNotContainKeys(SELECTED_STUDENT_ID.getValue(), SELECTED_STUDENT_NAME
                .getValue());
        assertThat(((ActionResult.EditMessageText) result).text()).isEqualTo(Messages.STUDENT_NOT_FOUND_OR_DELETED);
    }

    private ActingTeacherResolver resolver(FindTeacher findTeacher) {
        return new ActingTeacherResolver(findTeacher, mock(TeacherDirectory.class));
    }
}
