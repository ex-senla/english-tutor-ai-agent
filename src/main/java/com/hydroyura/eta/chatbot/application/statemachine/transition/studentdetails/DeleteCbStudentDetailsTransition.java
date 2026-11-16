package com.hydroyura.eta.chatbot.application.statemachine.transition.studentdetails;

import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.view.Messages.STUDENT_DELETE_LESSON_ACTIVE;

@RequiredArgsConstructor
public class DeleteCbStudentDetailsTransition implements Transition<Action.Callback> {

    private final FindActiveLesson findActiveLesson;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var studentId = StudentSupport.selectedStudentId(chat);

        if (findActiveLesson.findByStudentId(studentId).isPresent()) {
            return new ActionResult.TextResponse(STUDENT_DELETE_LESSON_ACTIVE);
        }

        chat.updateState(ChatState.AWAITING_STUDENT_DELETE);
        var name = (String) chat.getContext().get(SELECTED_STUDENT_NAME.getValue());
        return StudentView.deleteConfirmation(callback.messageId(), name);
    }
}
