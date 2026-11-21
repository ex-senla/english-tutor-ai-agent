package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevel;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActiveMenuSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.ActingTeacherResolver;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.menu.MenuView;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentAlreadyExistsException;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LevelCbAwaitingStudentLevelTransitionTest {

    @Test
    void shouldCreateStudentWithSelectedLevel() {
        var findTeacher = mock(FindTeacher.class);
        var createStudentWithDictionary = mock(CreateStudentWithDictionary.class);
        var activeMenuSupport = mock(ActiveMenuSupport.class);

        var teacherId = TeacherId.generate();
        when(findTeacher.findByTelegramChatId(1L)).thenReturn(Optional.of(teacherId));
        when(createStudentWithDictionary.execute(any())).thenReturn(StudentId.generate());
        when(activeMenuSupport.renderWithMessage(any(), any())).thenReturn(MenuView.activeMenu());

        var transition = new LevelCbAwaitingStudentLevelTransition(resolver(findTeacher), createStudentWithDictionary,
                activeMenuSupport);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.getContext().put("pendingStudentName", "Иван");

        var result = transition.transit(chat, new Action.Callback("level", "B1", 42));

        var captor = ArgumentCaptor.forClass(CreateStudentWithDictionaryCommand.class);
        verify(createStudentWithDictionary).execute(captor.capture());
        assertThat(captor.getValue().level()).isEqualTo(CefrLevel.B1);
        assertThat(captor.getValue().studentName()).isEqualTo("Иван");
        assertThat(chat.getState()).isEqualTo(ChatState.ACTIVE);
        assertThat(chat.getContext()).doesNotContainKey("pendingStudentName");
        assertThat(result).isInstanceOf(ActionResult.TextWithReplyKeyboard.class);
    }

    @Test
    void shouldReturnEnterAnotherNameOnDuplicateStudentName() {
        var findTeacher = mock(FindTeacher.class);
        var createStudentWithDictionary = mock(CreateStudentWithDictionary.class);
        var activeMenuSupport = mock(ActiveMenuSupport.class);

        var teacherId = TeacherId.generate();
        when(findTeacher.findByTelegramChatId(1L)).thenReturn(Optional.of(teacherId));
        when(createStudentWithDictionary.execute(any()))
                .thenThrow(new StudentAlreadyExistsException("Иван"));

        var transition = new LevelCbAwaitingStudentLevelTransition(resolver(findTeacher), createStudentWithDictionary,
                activeMenuSupport);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.getContext().put("pendingStudentName", "Иван");

        var result = transition.transit(chat, new Action.Callback("level", "B1", 42));

        assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_STUDENT_NAME);
        assertThat(chat.getContext()).doesNotContainKey("pendingStudentName");
    }

    @Test
    void shouldReturnKeyboardOnInvalidPayload() {
        var findTeacher = mock(FindTeacher.class);
        var createStudentWithDictionary = mock(CreateStudentWithDictionary.class);
        var activeMenuSupport = mock(ActiveMenuSupport.class);

        var transition = new LevelCbAwaitingStudentLevelTransition(resolver(findTeacher), createStudentWithDictionary,
                activeMenuSupport);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_STUDENT_LEVEL);
        chat.getContext().put("pendingStudentName", "Иван");

        var result = transition.transit(chat, new Action.Callback("level", "ZZ", 42));

        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_STUDENT_LEVEL);
        verifyNoInteractions(createStudentWithDictionary);
    }

    private ActingTeacherResolver resolver(FindTeacher findTeacher) {
        return new ActingTeacherResolver(findTeacher, mock(TeacherDirectory.class));
    }
}
