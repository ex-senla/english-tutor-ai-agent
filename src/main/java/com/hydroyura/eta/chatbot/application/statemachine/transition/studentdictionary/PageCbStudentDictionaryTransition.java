package com.hydroyura.eta.chatbot.application.statemachine.transition.studentdictionary;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.Callbacks;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.DICTIONARY_PAGE;

@RequiredArgsConstructor
public class PageCbStudentDictionaryTransition implements Transition<Action.Callback> {

    private final StudentDictionarySupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var current = currentPage(chat);
        var delta = Callbacks.NEXT.equals(callback.payload()) ? 1 : -1;
        var next = Math.max(0, current + delta);
        chat.getContext().put(DICTIONARY_PAGE.getValue(), next);
        return support.render(chat, callback.messageId());
    }

    private static int currentPage(Chat chat) {
        var raw = chat.getContext().get(DICTIONARY_PAGE.getValue());
        return raw instanceof Integer page ? page : 0;
    }
}
