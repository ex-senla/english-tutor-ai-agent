package com.hydroyura.eta.chatbot.application.statemachine.transition.account;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.Messages;

/**
 * Повторный просмотр онбординг-обзора из личного кабинета: показывает текст справки новым
 * сообщением, не меняет состояние чата ({@code ACCOUNT}) и не пишет в БД.
 */
public class OnboardingCbAccountTransition implements Transition<Action.Callback> {

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        return new ActionResult.TextResponse(Messages.ONBOARDING_REVIEW);
    }
}
