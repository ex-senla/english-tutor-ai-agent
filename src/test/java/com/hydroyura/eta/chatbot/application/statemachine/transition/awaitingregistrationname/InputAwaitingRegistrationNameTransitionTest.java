package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingregistrationname;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActiveMenuSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Messages;
import com.hydroyura.eta.chatbot.view.menu.MenuView;
import com.hydroyura.eta.teacher.api.teacher.CompleteOnboarding;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TEACHER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InputAwaitingRegistrationNameTransitionTest {

    @Test
    void shouldRegisterCompleteOnboardingAndRenderReview() {
        var registerTeacher = mock(RegisterTeacher.class);
        var completeOnboarding = mock(CompleteOnboarding.class);
        var activeMenuSupport = mock(ActiveMenuSupport.class);

        var teacherId = TeacherId.generate();
        when(registerTeacher.execute(any())).thenReturn(teacherId);
        when(activeMenuSupport.renderWithMessage(any(), any()))
                .thenReturn(MenuView.activeMenuWithMessage(Messages.ONBOARDING_REVIEW));

        var chat = Chat.ofDefaults(new ChatId(777L));
        chat.updateState(ChatState.AWAITING_REGISTRATION_NAME);

        var transition = new InputAwaitingRegistrationNameTransition(
                registerTeacher, completeOnboarding, activeMenuSupport);
        var result = transition.transit(chat, new Action.Input("Иван"));

        verify(completeOnboarding).execute(teacherId);
        assertThat(chat.getState()).isEqualTo(ChatState.ACTIVE);
        assertThat(chat.getContext()).containsEntry(TEACHER_NAME.getValue(), "Иван");

        assertThat(result).isInstanceOf(ActionResult.TextWithReplyKeyboard.class);
        var text = ((ActionResult.TextWithReplyKeyboard) result).text();
        assertThat(text).contains(Messages.ONBOARDING_REVIEW).contains("Главное меню");
    }
}
