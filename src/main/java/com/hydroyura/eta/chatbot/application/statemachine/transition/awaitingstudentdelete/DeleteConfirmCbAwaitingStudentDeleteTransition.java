package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentdelete;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActingTeacherResolver;
import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.student.api.student.DeleteStudent;
import com.hydroyura.eta.student.api.student.DeleteStudentCommand;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;

@Slf4j
@RequiredArgsConstructor
public class DeleteConfirmCbAwaitingStudentDeleteTransition implements Transition<Action.Callback> {

    private final DeleteStudent deleteStudent;

    private final ActingTeacherResolver resolver;

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var studentId = StudentSupport.selectedStudentId(chat);
        deleteStudent.execute(new DeleteStudentCommand(studentId));

        chat.getContext().remove(SELECTED_STUDENT_ID.getValue());
        chat.getContext().remove(SELECTED_STUDENT_NAME.getValue());
        chat.updateState(ChatState.STUDENTS_LIST);

        var studentIds = resolver.actingStudentIds(chat, chat.getId().chatId());
        var students = studentQuery.findStudentsByIds(studentIds);
        log.info("Student {} deleted", studentId);
        return StudentView.deletedStudentsList(callback.messageId(), students);
    }
}
