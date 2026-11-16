package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.view.Messages.USE_BUTTONS_BELOW;
import static org.assertj.core.api.Assertions.assertThat;

class AwaitingStudentDeleteHandlerTest {

    @Test
    void shouldReturnUseButtonsBelowAndNotChangeState() {
        var handler = new AwaitingStudentDeleteHandler();
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_STUDENT_DELETE);

        var result = handler.handle(chat);

        assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
        assertThat(((ActionResult.TextResponse) result).text()).isEqualTo(USE_BUTTONS_BELOW);
        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_STUDENT_DELETE);
    }
}
