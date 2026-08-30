package com.hydroyura.eta.chatbot.application.statemachine.transition.awatingexercisetype;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;

public class ExerciseCbAwatingExerciseTypeTransition implements Transition<Action.Callback> {

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var exerciseType = switch (callback.payload()) {
            case "FILL_IN_THE_BLANK" -> ExerciseType.FILL_IN_THE_BLANK;
            case "MULTIPLE_CHOICE" -> ExerciseType.MULTIPLE_CHOICE;
            default -> null;
        };
        if (exerciseType == null) {
            return new ActionResult.TextResponse("Выберите тип упражнения кнопками ниже");
        }
        chat.getContext().put("exerciseType", exerciseType);
        chat.updateState(ChatState.AWAITING_EXERCISE_TOPIC);
        return new ActionResult.TextResponse("Введите тему упражнения (например, 'Animals')");
    }

    @Override
    public String getName() {
        return "ExerciseCbAwatingExerciseTypeTransition";
    }
}
