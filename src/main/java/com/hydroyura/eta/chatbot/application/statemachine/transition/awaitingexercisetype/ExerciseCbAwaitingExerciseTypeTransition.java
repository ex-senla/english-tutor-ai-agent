package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetype;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;

import java.util.Map;

import static com.hydroyura.eta.chatbot.view.Messages.CHOOSE_EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.view.Messages.CHOOSE_EXERCISE_TYPE;

public class ExerciseCbAwaitingExerciseTypeTransition implements Transition<Action.Callback> {

    private static final Map<String, ExerciseType> PAYLOAD_EXERCISE_TYPE_MAP = Map.of(
            Callbacks.FILL_IN_THE_BLANK, ExerciseType.FILL_IN_THE_BLANK,
            Callbacks.MULTIPLE_CHOICE, ExerciseType.MULTIPLE_CHOICE
    );

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        //FIXME если передать null, то будет NPE
        var exerciseType = PAYLOAD_EXERCISE_TYPE_MAP.get(callback.payload());

        if (exerciseType == null) {
            return new ActionResult.TextResponse(CHOOSE_EXERCISE_TYPE);
        }
        chat.getContext().put("exerciseType", exerciseType);
        chat.updateState(ChatState.AWAITING_EXERCISE_TOPIC);
        return new ActionResult.TextResponse(CHOOSE_EXERCISE_TOPIC);
    }

}
