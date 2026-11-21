package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.api.teacher.TeacherInfo;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTING_TEACHER_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ADMIN_SELECTED_TEACHER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActiveMenuSupportTest {

    private final FindTeacher findTeacher = mock(FindTeacher.class);

    private final TeacherDirectory teacherDirectory = mock(TeacherDirectory.class);

    private final ActiveMenuSupport support = new ActiveMenuSupport(findTeacher, teacherDirectory);

    @Test
    void shouldResolveImpersonationIndicatorByActingTeacherId() {
        var realTeacherId = TeacherId.generate();
        var actingTeacherId = TeacherId.generate();
        when(findTeacher.findByTelegramChatId(1L)).thenReturn(Optional.of(realTeacherId));
        when(teacherDirectory.findById(realTeacherId)).thenReturn(Optional.of(
                new TeacherInfo(realTeacherId, "Admin", true)));
        when(teacherDirectory.findById(actingTeacherId)).thenReturn(Optional.of(
                new TeacherInfo(actingTeacherId, "TeacherB", false)));

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.getContext().put(ACTING_TEACHER_ID.getValue(), actingTeacherId.value().toString());
        // stale inline-button name must be ignored
        chat.getContext().put(ADMIN_SELECTED_TEACHER_NAME.getValue(), "StaleName");

        var result = support.render(chat);

        var text = switch (result) {
            case com.hydroyura.eta.chatbot.domain.action.ActionResult.TextWithReplyKeyboard twk -> twk.text();
            default -> "";
        };
        assertThat(text).contains("TeacherB").doesNotContain("StaleName");
    }
}
