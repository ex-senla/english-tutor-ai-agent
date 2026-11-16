package com.hydroyura.eta.chatbot.application.statemachine.transition.studentdetails;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.view.Messages.STUDENT_DELETE_LESSON_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteCbStudentDetailsTransitionTest {

    @Test
    void shouldEnterConfirmationWithoutActiveLesson() {
        var findActiveLesson = mock(FindActiveLesson.class);
        var studentId = StudentId.generate();
        when(findActiveLesson.findByStudentId(studentId)).thenReturn(Optional.empty());

        var transition = new DeleteCbStudentDetailsTransition(findActiveLesson);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENT_DETAILS);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Anna");

        var result = transition.transit(chat, new Action.Callback(Callbacks.STUDENT_DELETE, "", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_STUDENT_DELETE);
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        assertThat(((ActionResult.EditMessageText) result).text()).contains("Anna");
    }

    @Test
    void shouldKeepStateWhenActiveLessonExists() {
        var findActiveLesson = mock(FindActiveLesson.class);
        var studentId = StudentId.generate();
        when(findActiveLesson.findByStudentId(studentId)).thenReturn(Optional.of(LessonId.generate()));

        var transition = new DeleteCbStudentDetailsTransition(findActiveLesson);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENT_DETAILS);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Anna");

        var result = transition.transit(chat, new Action.Callback(Callbacks.STUDENT_DELETE, "", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_DETAILS);
        assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
        assertThat(((ActionResult.TextResponse) result).text()).isEqualTo(STUDENT_DELETE_LESSON_ACTIVE);
    }
}
