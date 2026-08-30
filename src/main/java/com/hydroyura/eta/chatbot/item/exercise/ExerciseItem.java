package com.hydroyura.eta.chatbot.item.exercise;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;

import java.util.List;

public class ExerciseItem {

    public static ActionResult exerciseTypeEdit(int messageId) {
        var keyboard = List.of(
                List.of(new ActionResult.InlineButton("✏️ Fill in the blank", "exercise:FILL_IN_THE_BLANK")),
                List.of(new ActionResult.InlineButton("🔤 Multiple choice", "exercise:MULTIPLE_CHOICE"))
        );
        return new ActionResult.EditMessageText(messageId, "Выберите тип упражнения:", keyboard);
    }

}
