package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;

/**
 * Проверяет роль администратора по реальному учителю текущего Telegram-чата (не действующему
 * в режиме имперсонации).
 */
public final class AdminGuard {

    private final FindTeacher findTeacher;

    private final TeacherDirectory teacherDirectory;

    public AdminGuard(FindTeacher findTeacher, TeacherDirectory teacherDirectory) {
        this.findTeacher = findTeacher;
        this.teacherDirectory = teacherDirectory;
    }

    public boolean isAdmin(Chat chat) {
        return findTeacher.findByTelegramChatId(chat.getId().chatId())
                .flatMap(teacherDirectory::findById)
                .map(info -> info.admin())
                .orElse(false);
    }
}
