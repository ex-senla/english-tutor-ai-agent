package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevelchange;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.ChangeStudentLevel;
import com.hydroyura.eta.student.api.student.ChangeStudentLevelCommand;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LevelCbAwaitingStudentLevelChangeTransitionTest {

    @Test
    void shouldChangeLevelAndReturnToDetails() {
        var studentQuery = mock(StudentQuery.class);
        var changeStudentLevel = mock(ChangeStudentLevel.class);

        var studentId = StudentId.generate();
        when(studentQuery.findStudentDetails(studentId)).thenReturn(Optional.of(
                new StudentDetails("Иван", new DictionaryStats(3, 1, 1, 1), false, CefrLevel.C1)));

        var transition = new LevelCbAwaitingStudentLevelChangeTransition(studentQuery, changeStudentLevel);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.getContext().put("selectedStudentId", studentId.value().toString());

        var result = transition.transit(chat, new Action.Callback("level", "C1", 42));

        var captor = ArgumentCaptor.forClass(ChangeStudentLevelCommand.class);
        verify(changeStudentLevel).execute(captor.capture());
        assertThat(captor.getValue().studentId()).isEqualTo(studentId);
        assertThat(captor.getValue().level()).isEqualTo(CefrLevel.C1);
        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_DETAILS);
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
    }

    @Test
    void shouldReturnKeyboardOnInvalidPayload() {
        var studentQuery = mock(StudentQuery.class);
        var changeStudentLevel = mock(ChangeStudentLevel.class);

        var transition = new LevelCbAwaitingStudentLevelChangeTransition(studentQuery, changeStudentLevel);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_STUDENT_LEVEL_CHANGE);
        chat.getContext().put("selectedStudentId", StudentId.generate().value().toString());

        var result = transition.transit(chat, new Action.Callback("level", "ZZ", 42));

        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_STUDENT_LEVEL_CHANGE);
        verifyNoInteractions(changeStudentLevel);
    }
}
