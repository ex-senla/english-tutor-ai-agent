package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.shared.api.DomainException;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;

import java.util.Set;
import java.util.UUID;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTING_TEACHER_ID;

/**
 * Единая точка резолвинга «действующего учителя» и его учеников.
 *
 * <p>Если в контексте чата активна имперсонация ({@code ACTING_TEACHER_ID}), все чтения и
 * записи флоу идут от имени выбранного учителя; иначе — от учителя текущего Telegram-чата.</p>
 */
public final class ActingTeacherResolver {

    private final FindTeacher findTeacher;

    private final TeacherDirectory teacherDirectory;

    public ActingTeacherResolver(FindTeacher findTeacher, TeacherDirectory teacherDirectory) {
        this.findTeacher = findTeacher;
        this.teacherDirectory = teacherDirectory;
    }

    public TeacherId actingTeacherId(Chat chat, Long chatId) {
        if (isImpersonating(chat)) {
            return parseActingTeacherId(chat);
        }
        return findTeacher.findByTelegramChatId(chatId)
                .orElseThrow(() -> new DomainException("Teacher not found for chatId=" + chatId) {
                });
    }

    public Set<StudentId> actingStudentIds(Chat chat, Long chatId) {
        if (isImpersonating(chat)) {
            var actingTeacherId = parseActingTeacherId(chat);
            return teacherDirectory.getStudentIds(actingTeacherId);
        }
        return findTeacher.getStudentIds(chatId);
    }

    public boolean isImpersonating(Chat chat) {
        var raw = chat.getContext().get(ACTING_TEACHER_ID.getValue());
        return raw instanceof String value && !value.isBlank();
    }

    private TeacherId parseActingTeacherId(Chat chat) {
        var raw = (String) chat.getContext().get(ACTING_TEACHER_ID.getValue());
        try {
            return new TeacherId(UUID.fromString(raw));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid actingTeacherId in chat context: " + raw) {
            };
        }
    }
}
