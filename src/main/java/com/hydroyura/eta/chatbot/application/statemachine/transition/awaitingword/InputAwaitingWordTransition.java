package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingword;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.view.word.WordView;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWord;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWordCommand;
import lombok.RequiredArgsConstructor;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;

@RequiredArgsConstructor
public class InputAwaitingWordTransition implements Transition<Action.Input> {

    private final CheckWord checkWord;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        String wordValue = input.text();

        if (!checkWord.check(new CheckWordCommand(wordValue))) {
            return WordView.notValidWord();
        }

        chat.getContext().put(WORD_VALUE.getValue(), wordValue);
        chat.updateState(ChatState.AWAITING_POS);
        return WordView.posMenu(wordValue);
    }

}
