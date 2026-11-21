package com.hydroyura.eta.chatbot.application.statemachine.transition.account;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActingTeacherResolver;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.support.SupportView;
import com.hydroyura.eta.support.api.support.FindSupportMessages;
import com.hydroyura.eta.support.api.support.OpenSupportThread;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SUPPORT_THREAD_ID;

@RequiredArgsConstructor
public class SupportCbAccountTransition implements Transition<Action.Callback> {

    private final ActingTeacherResolver resolver;

    private final OpenSupportThread openSupportThread;

    private final FindSupportMessages findSupportMessages;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var thread = openSupportThread.open(resolver.actingTeacherId(chat, chat.getId().chatId()));
        chat.getContext().put(SUPPORT_THREAD_ID.getValue(), thread.id().value().toString());
        chat.updateState(ChatState.SUPPORT);
        return SupportView.thread(findSupportMessages.findByThreadId(thread.id()));
    }
}
