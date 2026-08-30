package com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.item.lesson.LessonItem;

public class HelpCmdInLessonTransition implements Transition<Action.Command> {

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        return LessonItem.lessonKeyboard("➕ Добавить слово — добавить слово\n🏁 Завершить урок — завершить урок");
    }

    @Override
    public String getName() {
        return "HelpCmdInLessonTransition";
    }
}
