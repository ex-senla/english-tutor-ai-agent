package com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.view.lesson.LessonView;

import static com.hydroyura.eta.chatbot.view.Messages.LESSON_INSTRUCTION;

public class HelpCmdInLessonTransition implements Transition<Action.Command> {

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        return LessonView.lessonKeyboard(LESSON_INSTRUCTION);
    }

}
