package com.hydroyura.eta.chatbot.application.statemachine.transition.support;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.support.SupportView;
import com.hydroyura.eta.support.api.support.AppendSupportMessage;
import com.hydroyura.eta.support.api.support.AppendSupportMessageCommand;
import com.hydroyura.eta.support.api.support.FindSupportMessages;
import com.hydroyura.eta.support.api.support.SupportAuthor;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SUPPORT_THREAD_ID;

@RequiredArgsConstructor
public class InputSupportTransition implements Transition<Action.Input> {

    private final AppendSupportMessage appendSupportMessage;

    private final FindSupportMessages findSupportMessages;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var threadId = threadId(chat);
        appendSupportMessage.append(new AppendSupportMessageCommand(threadId, SupportAuthor.TEACHER, input.text()));
        return SupportView.sent(findSupportMessages.findByThreadId(threadId));
    }

    private SupportThreadId threadId(Chat chat) {
        var raw = Objects.requireNonNull((String) chat.getContext().get(SUPPORT_THREAD_ID.getValue()),
                "supportThreadId must be present in chat context");
        return new SupportThreadId(UUID.fromString(raw));
    }
}
