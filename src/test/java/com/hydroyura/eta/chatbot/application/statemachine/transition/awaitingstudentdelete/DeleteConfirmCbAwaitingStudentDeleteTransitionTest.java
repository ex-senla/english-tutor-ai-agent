package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentdelete;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.Messages;
import com.hydroyura.eta.student.api.student.DeleteStudent;
import com.hydroyura.eta.student.api.student.DeleteStudentCommand;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteConfirmCbAwaitingStudentDeleteTransitionTest {

    @Test
    void shouldDeleteAndClearContextAndShowList() {
        var deleteStudent = mock(DeleteStudent.class);
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);

        var deletedId = StudentId.generate();
        var remaining = new StudentInfo(StudentId.generate(), "Bob");
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(deletedId, remaining.id()));
        when(studentQuery.findStudentsByIds(Set.of(deletedId, remaining.id()))).thenReturn(List.of(remaining));

        var transition = new DeleteConfirmCbAwaitingStudentDeleteTransition(deleteStudent, findTeacher, studentQuery);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_STUDENT_DELETE);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), deletedId.value().toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Anna");

        var result = transition.transit(chat, new Action.Callback(Callbacks.STUDENT_DELETE_CONFIRM, "", 42));

        var captor = ArgumentCaptor.forClass(DeleteStudentCommand.class);
        verify(deleteStudent).execute(captor.capture());
        assertThat(captor.getValue().studentId()).isEqualTo(deletedId);

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENTS_LIST);
        assertThat(chat.getContext()).doesNotContainKeys(SELECTED_STUDENT_ID.getValue(), SELECTED_STUDENT_NAME
                .getValue());

        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        var edit = (ActionResult.EditMessageText) result;
        assertThat(edit.text()).isEqualTo(Messages.STUDENT_DELETED);
        var buttons = edit.keyboard().stream().flatMap(List::stream)
                .map(ActionResult.InlineButton::text)
                .toList();
        assertThat(buttons).contains("Bob").doesNotContain("Anna");
    }

    @Test
    void shouldShowEmptyListWhenLastStudentDeleted() {
        var deleteStudent = mock(DeleteStudent.class);
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);

        var deletedId = StudentId.generate();
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(deletedId));
        when(studentQuery.findStudentsByIds(Set.of(deletedId))).thenReturn(List.of());

        var transition = new DeleteConfirmCbAwaitingStudentDeleteTransition(deleteStudent, findTeacher, studentQuery);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_STUDENT_DELETE);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), deletedId.value().toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Anna");

        var result = transition.transit(chat, new Action.Callback(Callbacks.STUDENT_DELETE_CONFIRM, "", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENTS_LIST);
        var edit = (ActionResult.EditMessageText) result;
        assertThat(edit.text()).isEqualTo(Messages.NO_STUDENTS);
        assertThat(edit.keyboard()).isEmpty();
    }
}
