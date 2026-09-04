package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingregistrationname;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.view.menu.MenuView;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class InputAwaitingRegistrationNameTransition implements Transition<Action.Input> {

    private final RegisterTeacher registerTeacher;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        String name = input.text();
        registerTeacher.execute(new RegisterTeacherCommand(chat.getId().chatId(), name));
        chat.getContext().put("teacherName", name);
        chat.updateState(ChatState.ACTIVE);
        log.info("Teacher registered: chatId={}, name={}", chat.getId().chatId(), name);
        return MenuView.activeMenu();
    }

}
