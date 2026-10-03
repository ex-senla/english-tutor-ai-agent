package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevel;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LevelCbAwaitingStudentLevelTransitionTest {

    @Test
    void shouldCreateStudentWithSelectedLevel() {
        var findTeacher = mock(FindTeacher.class);
        var createStudentWithDictionary = mock(CreateStudentWithDictionary.class);

        var teacherId = TeacherId.generate();
        when(findTeacher.findByTelegramChatId(1L)).thenReturn(Optional.of(teacherId));
        when(createStudentWithDictionary.execute(any())).thenReturn(StudentId.generate());

        var transition = new LevelCbAwaitingStudentLevelTransition(findTeacher, createStudentWithDictionary);

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
}
