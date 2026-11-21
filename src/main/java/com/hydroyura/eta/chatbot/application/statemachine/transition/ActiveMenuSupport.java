package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.menu.MenuView;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.api.teacher.TeacherInfo;
import java.util.UUID;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTING_TEACHER_ID;

/**
 * Единый шов рендера главного меню: свежая роль из БД + индикатор имперсонации.
 */
public final class ActiveMenuSupport {

    private final FindTeacher findTeacher;

    private final TeacherDirectory teacherDirectory;

    public ActiveMenuSupport(FindTeacher findTeacher, TeacherDirectory teacherDirectory) {
        this.findTeacher = findTeacher;
        this.teacherDirectory = teacherDirectory;
    }

    public ActionResult render(Chat chat) {
        return render(chat, null);
    }

    public ActionResult render(Chat chat, Integer cleanupMessageId) {
        var menu = menu(chat);
        return MenuView.activeMenu(menu.teacherInfo(), menu.actingTeacherName(), cleanupMessageId);
    }

    public ActionResult renderWithMessage(Chat chat, String message) {
        var menu = menu(chat);
        return MenuView.activeMenuWithMessage(message, menu.teacherInfo(), menu.actingTeacherName());
    }

    private Menu menu(Chat chat) {
        var chatId = chat.getId().chatId();
        var teacherId = findTeacher.findByTelegramChatId(chatId);
        TeacherInfo teacherInfo = teacherId.flatMap(teacherDirectory::findById).orElse(null);

        String actingTeacherName = null;
        var actingRaw = chat.getContext().get(ACTING_TEACHER_ID.getValue());
        if (actingRaw instanceof String value && !value.isBlank()) {
            try {
                var actingTeacherId = new TeacherId(UUID.fromString(value));
                actingTeacherName = teacherDirectory.findById(actingTeacherId)
                        .map(TeacherInfo::name)
                        .orElse(null);
            } catch (IllegalArgumentException e) {
                actingTeacherName = null;
            }
        }
        return new Menu(teacherInfo, actingTeacherName);
    }

    private record Menu(TeacherInfo teacherInfo, String actingTeacherName) {
    }
}
