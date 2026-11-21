package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NewStateHandlerHintTest {

    private final StateMachine machine = new StateMachine(Map.of(
            ChatState.ACCOUNT, new AccountHandler(),
            ChatState.SUPPORT, new SupportHandler(),
            ChatState.ADMIN_MENU, new AdminMenuHandler(),
            ChatState.ADMIN_TEACHERS, new AdminTeachersHandler(),
            ChatState.ADMIN_TEACHER_STUDENTS, new AdminTeacherStudentsHandler(),
            ChatState.ADMIN_THREADS, new AdminThreadsHandler(),
            ChatState.ADMIN_THREAD_VIEW, new AdminThreadViewHandler()));

    @Test
    void unexpectedCommandShouldReturnHandlerHintWithoutChangingState() {
        for (var state : newStates()) {
            var chat = Chat.ofDefaults(new ChatId(1L));
            chat.updateState(state);

            var result = machine.applyAction(chat, new Action.Command("/unexpected"));

            assertThat(chat.getState()).as("state after /unexpected from %s", state).isEqualTo(state);
            assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
            assertThat(((ActionResult.TextResponse) result).text()).isNotBlank();
        }
    }

    @Test
    void unexpectedInputShouldReturnHandlerHintWithoutChangingState() {
        for (var state : newStates()) {
            var chat = Chat.ofDefaults(new ChatId(1L));
            chat.updateState(state);

            var result = machine.applyAction(chat, new Action.Input("unexpected"));

            assertThat(chat.getState()).as("state after input from %s", state).isEqualTo(state);
            assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
            assertThat(((ActionResult.TextResponse) result).text()).isNotBlank();
        }
    }

    private ChatState[] newStates() {
        return new ChatState[]{
                ChatState.ACCOUNT,
                ChatState.SUPPORT,
                ChatState.ADMIN_MENU,
                ChatState.ADMIN_TEACHERS,
                ChatState.ADMIN_TEACHER_STUDENTS,
                ChatState.ADMIN_THREADS,
                ChatState.ADMIN_THREAD_VIEW
        };
    }
}
