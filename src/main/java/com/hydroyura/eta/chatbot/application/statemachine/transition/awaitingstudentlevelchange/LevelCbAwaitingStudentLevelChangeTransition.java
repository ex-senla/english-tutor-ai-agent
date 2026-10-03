package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevelchange;

import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.ChangeStudentLevel;
import com.hydroyura.eta.student.api.student.ChangeStudentLevelCommand;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.hydroyura.eta.chatbot.view.Messages.USE_BUTTONS_BELOW;

@Slf4j
@RequiredArgsConstructor
public class LevelCbAwaitingStudentLevelChangeTransition implements Transition<Action.Callback> {

    private final StudentQuery studentQuery;

    private final ChangeStudentLevel changeStudentLevel;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var level = CefrLevel.fromCode(callback.payload());
        if (level.isEmpty()) {
            log.warn("Invalid level payload '{}'", callback.payload());
            return new ActionResult.EditMessageText(callback.messageId(), USE_BUTTONS_BELOW,
                    StudentView.levelKeyboardWithBack());
        }

        var studentId = StudentSupport.selectedStudentId(chat);
        changeStudentLevel.execute(new ChangeStudentLevelCommand(studentId, level.get()));
        log.info("Student {} level changed to {}", studentId, level.get());

        chat.updateState(ChatState.STUDENT_DETAILS);
        return StudentSupport.studentDetails(studentQuery, chat, callback.messageId());
    }
}
