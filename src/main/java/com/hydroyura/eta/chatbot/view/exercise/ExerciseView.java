package com.hydroyura.eta.chatbot.view.exercise;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Callbacks;

import java.util.List;

import static com.hydroyura.eta.chatbot.view.Messages.FILL_IN_THE_BLANK;
import static com.hydroyura.eta.chatbot.view.Messages.MULTIPLE_CHOICE;
import static com.hydroyura.eta.chatbot.view.util.ItemUtils.createCallbackData;

public class ExerciseView {

    public static ActionResult exerciseTypeEdit(int messageId) {
        var keyboard = List.of(
                List.of(new ActionResult.InlineButton(FILL_IN_THE_BLANK, createCallbackData(Callbacks.EXERCISE, Callbacks.FILL_IN_THE_BLANK))),
                List.of(new ActionResult.InlineButton(MULTIPLE_CHOICE, createCallbackData(Callbacks.EXERCISE, Callbacks.MULTIPLE_CHOICE)))
        );
        return new ActionResult.EditMessageText(messageId, "Выберите тип упражнения:", keyboard);
    }

}
