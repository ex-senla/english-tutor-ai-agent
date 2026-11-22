package com.hydroyura.eta.chatbot.application.statemachine.transition.account;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.Messages;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AccountCbTransitionTest {

    private final AccountCbTransition transition = new AccountCbTransition(
            mock(SupportCbAccountTransition.class),
            mock(SubscriptionsCbAccountTransition.class),
            new OnboardingCbAccountTransition(),
            mock(BackCbAccountTransition.class));

    @Test
    void onboardingShouldShowReviewAndKeepAccountState() {
        var chat = Chat.ofDefaults(new ChatId(777L));
        chat.updateState(ChatState.ACCOUNT);

        var result = transition.transit(chat, new Action.Callback(Callbacks.ACCOUNT, Callbacks.ONBOARDING, 1));

        assertThat(result).isEqualTo(new ActionResult.TextResponse(Messages.ONBOARDING_REVIEW));
        assertThat(chat.getState()).isEqualTo(ChatState.ACCOUNT);
    }

    @Test
    void unknownPayloadShouldShowHintAndKeepAccountState() {
        var chat = Chat.ofDefaults(new ChatId(777L));
        chat.updateState(ChatState.ACCOUNT);

        var result = transition.transit(chat, new Action.Callback(Callbacks.ACCOUNT, "unknown", 1));

        assertThat(result).isEqualTo(new ActionResult.TextResponse(Messages.USE_BUTTONS_BELOW));
        assertThat(chat.getState()).isEqualTo(ChatState.ACCOUNT);
    }
}
