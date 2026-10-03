package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevel;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.menu.MenuView;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.hydroyura.eta.chatbot.view.Messages.ENTER_ANOTHER_NAME;
import static com.hydroyura.eta.chatbot.view.Messages.STUDENT_ADDED;

@Slf4j
@RequiredArgsConstructor
public class LevelCbAwaitingStudentLevelTransition implements Transition<Action.Callback> {

    private final FindTeacher findTeacher;

    private final CreateStudentWithDictionary createStudentWithDictionary;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var level = CefrLevel.valueOf(callback.payload());
        var name = (String) chat.getContext().get("pendingStudentName");
        var teacherId = findTeacher.findByTelegramChatId(chat.getId().chatId())
                .orElseThrow(() -> new IllegalStateException("Teacher not found for chatId=" + chat.getId().chatId()));

        try {
            var dictionaryName = "Словарь " + name;
            var studentId = createStudentWithDictionary.execute(
                    new CreateStudentWithDictionaryCommand(teacherId, name, dictionaryName, level));
            log.info("Student created: name={}, id={}, teacherId={}, level={}", name, studentId, teacherId, level);
            chat.getContext().remove("pendingStudentName");
            chat.updateState(ChatState.ACTIVE);
            return MenuView.activeMenuWithMessage(STUDENT_ADDED.formatted(name));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create student '{}': {}", name, e.getMessage());
            chat.getContext().remove("pendingStudentName");
            chat.updateState(ChatState.AWAITING_STUDENT_NAME);
            return new ActionResult.TextResponse(ENTER_ANOTHER_NAME.formatted(e.getMessage()));
        }
    }
}
