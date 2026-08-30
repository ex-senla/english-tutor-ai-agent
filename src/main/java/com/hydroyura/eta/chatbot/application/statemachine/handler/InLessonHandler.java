package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.item.lesson.LessonItem;
import org.springframework.stereotype.Component;

@Component
public class InLessonHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return LessonItem.lessonKeyboard("Используйте кнопки ниже");
    }

    @Override
    public ChatState getChatState() {
        return ChatState.IN_LESSON;
    }
}
