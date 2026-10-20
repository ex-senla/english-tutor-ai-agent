package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevelchange;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BackCbAwaitingStudentLevelChangeTransitionTest {

    @Test
    void shouldReturnToDetailsWithoutChangingLevel() {
        var studentQuery = mock(StudentQuery.class);
        var studentId = StudentId.generate();
        when(studentQuery.findStudentDetails(studentId)).thenReturn(Optional.of(
                new StudentDetails("Иван", new DictionaryStats(3, 1, 1, 1), false, CefrLevel.B1)));

        var transition = new BackCbAwaitingStudentLevelChangeTransition(studentQuery);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.getContext().put("selectedStudentId", studentId.value().toString());

        var result = transition.transit(chat, new Action.Callback("back", "main", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_DETAILS);
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        var edit = (ActionResult.EditMessageText) result;
        assertThat(edit.text()).contains("Уровень: B1");
    }
}
