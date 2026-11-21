package com.hydroyura.eta.chatbot.application.statemachine.transition.account;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActingTeacherResolver;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.account.AccountView;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccountBtnActiveTransition implements Transition<Action.Button> {

    private final ActingTeacherResolver resolver;

    private final TeacherDirectory teacherDirectory;

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action.Button action) {
        var chatId = chat.getId().chatId();
        var teacherId = resolver.actingTeacherId(chat, chatId);
        var teacher = teacherDirectory.findById(teacherId)
                .orElseThrow(() -> new IllegalStateException("Teacher not found: " + teacherId));
        var activeStudents = studentQuery.findStudentsByIds(resolver.actingStudentIds(chat, chatId));

        chat.updateState(ChatState.ACCOUNT);
        return AccountView.profile(teacher.name(), activeStudents.size());
    }
}
