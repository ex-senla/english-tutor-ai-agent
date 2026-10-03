package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevelchange;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BackCbAwaitingStudentLevelChangeTransition implements Transition<Action.Callback> {

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var studentId = new StudentId(UUID.fromString((String) chat.getContext().get("selectedStudentId")));

        var details = studentQuery.findStudentDetails(studentId)
                .orElseThrow(() -> new IllegalStateException("No details for student " + studentId));

        chat.updateState(ChatState.STUDENT_DETAILS);
        return StudentView.studentDetails(callback.messageId(), details);
    }
}
