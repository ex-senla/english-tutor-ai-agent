package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_THEMATIC_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;

public class SelectionBackCbAwaitingExerciseWordSelectionTransition implements Transition<Action.Callback> {

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        chat.getContext().remove(EXERCISE_TYPE.getValue());
        chat.getContext().remove(EXERCISE_TOPIC.getValue());
        chat.getContext().remove(EXERCISE_THEMATIC_TOPIC.getValue());
        chat.getContext().remove(WORD_SELECTION.getValue());
        chat.getContext().remove(WORD_FILTER.getValue());
        chat.getContext().remove(SELECTION_PAGE.getValue());
        chat.updateState(ChatState.STUDENT_OPTIONS);

        var name = (String) chat.getContext().getOrDefault(SELECTED_STUDENT_NAME.getValue(), "?");
        return StudentView.options(callback.messageId(), name);
    }
}
