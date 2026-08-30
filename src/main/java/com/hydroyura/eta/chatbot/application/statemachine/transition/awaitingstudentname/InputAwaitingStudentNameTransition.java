package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentname;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.item.menu.MenuItem;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class InputAwaitingStudentNameTransition implements Transition<Action.Input> {

    private final FindTeacher findTeacher;

    private final CreateStudentWithDictionary createStudentWithDictionary;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var name = input.text();
        var teacherId = findTeacher.findByTelegramChatId(chat.getId().chatId())
                .orElseThrow(() -> new IllegalStateException("Teacher not found for chatId=" + chat.getId().chatId()));

        try {
            var dictionaryName = "Словарь " + name;
            var studentId = createStudentWithDictionary.execute(
                    new CreateStudentWithDictionaryCommand(teacherId, name, dictionaryName));
            log.info("Student created: name={}, id={}, teacherId={}", name, studentId, teacherId);
            chat.updateState(ChatState.ACTIVE);
            var teacherName = (String) chat.getContext().getOrDefault("teacherName", "");
            return MenuItem.activeMenuWithMessage("✅ Ученик '" + name + "' добавлен!", teacherName);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create student '{}': {}", name, e.getMessage());
            return new ActionResult.TextResponse("❌ " + e.getMessage() + ". Введите другое имя:");
        }
    }

    @Override
    public String getName() {
        return "InputAwaitingStudentNameTransition";
    }
}
