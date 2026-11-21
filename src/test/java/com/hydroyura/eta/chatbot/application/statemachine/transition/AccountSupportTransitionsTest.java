package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.application.statemachine.transition.account.AccountBtnActiveTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.support.InputSupportTransition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.menu.MenuView;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.support.api.support.AppendSupportMessage;
import com.hydroyura.eta.support.api.support.AppendSupportMessageCommand;
import com.hydroyura.eta.support.api.support.FindSupportMessages;
import com.hydroyura.eta.support.api.support.SupportAuthor;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.api.teacher.TeacherInfo;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTING_TEACHER_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SUPPORT_THREAD_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountSupportTransitionsTest {

    @Test
    void accountShouldShowProfileWithActiveStudentCount() {
        var resolver = mock(ActingTeacherResolver.class);
        var teacherDirectory = mock(TeacherDirectory.class);
        var studentQuery = mock(StudentQuery.class);

        var teacherId = TeacherId.generate();
        var active = StudentId.generate();
        when(resolver.actingTeacherId(any(), any())).thenReturn(teacherId);
        when(resolver.actingStudentIds(any(), any())).thenReturn(Set.of(active));
        when(teacherDirectory.findById(teacherId)).thenReturn(Optional.of(new TeacherInfo(teacherId, "Anna", false)));
        when(studentQuery.findStudentsByIds(Set.of(active))).thenReturn(List.of(new StudentInfo(active, "Ivan")));

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ACTIVE);

        var result = new AccountBtnActiveTransition(resolver, teacherDirectory, studentQuery)
                .transit(chat, new Action.Button("👤 Личный кабинет"));

        assertThat(chat.getState()).isEqualTo(ChatState.ACCOUNT);
        var text = ((ActionResult.TextWithInlineKeyboard) result).text();
        assertThat(text).contains("Anna").contains("1");
    }

    @Test
    void inputSupportShouldAppendTeacherMessage() {
        var append = mock(AppendSupportMessage.class);
        var findMessages = mock(FindSupportMessages.class);
        var threadId = SupportThreadId.generate();
        when(findMessages.findByThreadId(threadId)).thenReturn(List.of());

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.SUPPORT);
        chat.getContext().put(SUPPORT_THREAD_ID.getValue(), threadId.value().toString());

        var result = new InputSupportTransition(append, findMessages)
                .transit(chat, new Action.Input("Не работает генерация"));

        var captor = ArgumentCaptor.forClass(AppendSupportMessageCommand.class);
        verify(append).append(captor.capture());
        assertThat(captor.getValue().author()).isEqualTo(SupportAuthor.TEACHER);
        assertThat(captor.getValue().text()).isEqualTo("Не работает генерация");
        assertThat(chat.getState()).isEqualTo(ChatState.SUPPORT);
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void startShouldClearImpersonationAndRenderMenu() {
        var activeMenuSupport = mock(ActiveMenuSupport.class);
        when(activeMenuSupport.render(any())).thenReturn(MenuView.activeMenu());

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ACTIVE);
        chat.getContext().put(ACTING_TEACHER_ID.getValue(), TeacherId.generate().value().toString());

        var result = new StartCmdTransition(activeMenuSupport).transit(chat, new Action.Command("/start"));

        assertThat(chat.getContext()).doesNotContainKey(ACTING_TEACHER_ID.getValue());
        assertThat(chat.getState()).isEqualTo(ChatState.ACTIVE);
        assertThat(result).isInstanceOf(ActionResult.TextWithReplyKeyboard.class);
    }
}
