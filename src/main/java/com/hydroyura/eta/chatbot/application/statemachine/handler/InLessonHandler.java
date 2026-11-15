package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.lesson.LessonView;
import org.springframework.stereotype.Component;

import static com.hydroyura.eta.chatbot.view.Messages.LESSON_INSTRUCTION;

@Component
public class InLessonHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return LessonView.lessonKeyboard(LESSON_INSTRUCTION);
    }

    @Override
    public ChatState getChatState() {
        return ChatState.IN_LESSON;
    }
}
