package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.notification.BotNotifier;
import com.hydroyura.eta.chatbot.application.statemachine.transition.ActiveMenuSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.ActingTeacherResolver;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.menu.MenuView;
import com.hydroyura.eta.support.api.support.AppendSupportMessage;
import com.hydroyura.eta.support.api.support.FindSupportMessages;
import com.hydroyura.eta.support.api.support.FindSupportThreads;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.support.api.support.SupportThreadInfo;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.api.teacher.TeacherInfo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTING_TEACHER_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ADMIN_SELECTED_TEACHER_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SUPPORT_THREAD_ID;
import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_ACCESS_DENIED;
import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_THREAD_NOT_FOUND;
import static com.hydroyura.eta.chatbot.view.Messages.SUPPORT_ANSWER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminTransitionsTest {

    @Test
    void adminCmdShouldDenyNonAdmin() {
        var findTeacher = mock(FindTeacher.class);
        var teacherDirectory = mock(TeacherDirectory.class);
        var guard = new AdminGuard(findTeacher, teacherDirectory);
        when(findTeacher.findByTelegramChatId(1L)).thenReturn(Optional.empty());

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ACTIVE);

        var result = new AdminCmdActiveTransition(guard).transit(chat, new Action.Command("/admin"));

        assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
        assertThat(((ActionResult.TextResponse) result).text()).isEqualTo(ADMIN_ACCESS_DENIED);
        assertThat(chat.getState()).isEqualTo(ChatState.ACTIVE);
    }

    @Test
    void adminCmdShouldOpenAdminMenuAndResetImpersonation() {
        var findTeacher = mock(FindTeacher.class);
        var teacherDirectory = mock(TeacherDirectory.class);
        when(findTeacher.findByTelegramChatId(1L)).thenReturn(Optional.of(TeacherId.generate()));
        when(teacherDirectory.findById(any())).thenReturn(Optional.of(
                new TeacherInfo(TeacherId.generate(), "Admin", true)));

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ACTIVE);
        chat.getContext().put(ACTING_TEACHER_ID.getValue(), TeacherId.generate().value().toString());

        var result = new AdminCmdActiveTransition(new AdminGuard(findTeacher, teacherDirectory))
                .transit(chat, new Action.Command("/admin"));

        assertThat(chat.getState()).isEqualTo(ChatState.ADMIN_MENU);
        assertThat(chat.getContext()).doesNotContainKey(ACTING_TEACHER_ID.getValue());
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void impersonationExitShouldReturnAdminMenuWhenActive() {
        var resolver = mock(ActingTeacherResolver.class);
        when(resolver.isImpersonating(any())).thenReturn(true);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ACTIVE);
        chat.getContext().put(ACTING_TEACHER_ID.getValue(), TeacherId.generate().value().toString());

        var result = new ImpersonationExitCmdActiveTransition(resolver)
                .transit(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.ADMIN_MENU);
        assertThat(chat.getContext()).doesNotContainKey(ACTING_TEACHER_ID.getValue());
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void impersonationExitShouldBeNoOpWithoutImpersonation() {
        var resolver = mock(ActingTeacherResolver.class);
        when(resolver.isImpersonating(any())).thenReturn(false);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ACTIVE);

        var result = new ImpersonationExitCmdActiveTransition(resolver)
                .transit(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.ACTIVE);
        assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
    }

    @Test
    void inputAdminThreadViewShouldPushToThreadOwner() {
        var append = mock(AppendSupportMessage.class);
        var findThreads = mock(FindSupportThreads.class);
        var findMessages = mock(FindSupportMessages.class);
        var teacherDirectory = mock(TeacherDirectory.class);
        var notifier = mock(BotNotifier.class);
        var guard = mock(AdminGuard.class);
        when(guard.isAdmin(any())).thenReturn(true);

        var teacherId = TeacherId.generate();
        var threadId = SupportThreadId.generate();
        when(findThreads.findById(threadId)).thenReturn(Optional.of(
                new SupportThreadInfo(threadId, teacherId, Instant.now(), Instant.now())));
        when(teacherDirectory.findTelegramChatId(teacherId)).thenReturn(Optional.of(999L));
        when(teacherDirectory.findById(teacherId)).thenReturn(Optional.of(
                new TeacherInfo(teacherId, "B", false)));
        when(findMessages.findByThreadId(threadId)).thenReturn(List.of());

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ADMIN_THREAD_VIEW);
        chat.getContext().put(SUPPORT_THREAD_ID.getValue(), threadId.value().toString());

        var transition = new InputAdminThreadViewTransition(append, findThreads, findMessages, teacherDirectory,
                notifier, guard);
        var result = transition.transit(chat, new Action.Input("Проверим"));

        verify(notifier).notify(999L, SUPPORT_ANSWER_PREFIX + "Проверим");
        assertThat(chat.getState()).isEqualTo(ChatState.ADMIN_THREAD_VIEW);
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void inputAdminThreadViewShouldReturnNotFoundWithoutPush() {
        var append = mock(AppendSupportMessage.class);
        var findThreads = mock(FindSupportThreads.class);
        var findMessages = mock(FindSupportMessages.class);
        var teacherDirectory = mock(TeacherDirectory.class);
        var notifier = mock(BotNotifier.class);
        var guard = mock(AdminGuard.class);
        when(guard.isAdmin(any())).thenReturn(true);

        var threadId = SupportThreadId.generate();
        when(findThreads.findById(threadId)).thenReturn(Optional.empty());

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ADMIN_THREAD_VIEW);
        chat.getContext().put(SUPPORT_THREAD_ID.getValue(), threadId.value().toString());

        var transition = new InputAdminThreadViewTransition(append, findThreads, findMessages, teacherDirectory,
                notifier, guard);
        var result = transition.transit(chat, new Action.Input("Проверим"));

        assertThat(((ActionResult.TextResponse) result).text()).isEqualTo(ADMIN_THREAD_NOT_FOUND);
        verify(append, never()).append(any());
        verify(notifier, never()).notify(any(), any());
    }

    @Test
    void inputAdminThreadViewShouldDenyNonAdminWithoutAppendOrPush() {
        var append = mock(AppendSupportMessage.class);
        var findThreads = mock(FindSupportThreads.class);
        var findMessages = mock(FindSupportMessages.class);
        var teacherDirectory = mock(TeacherDirectory.class);
        var notifier = mock(BotNotifier.class);
        var guard = mock(AdminGuard.class);
        when(guard.isAdmin(any())).thenReturn(false);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ADMIN_THREAD_VIEW);
        chat.getContext().put(SUPPORT_THREAD_ID.getValue(), SupportThreadId.generate().value().toString());

        var transition = new InputAdminThreadViewTransition(append, findThreads, findMessages, teacherDirectory,
                notifier, guard);
        var result = transition.transit(chat, new Action.Input("Проверим"));

        assertThat(((ActionResult.TextResponse) result).text()).isEqualTo(ADMIN_ACCESS_DENIED);
        assertThat(chat.getState()).isEqualTo(ChatState.ADMIN_THREAD_VIEW);
        verify(append, never()).append(any());
        verify(notifier, never()).notify(any(), any());
    }

    @Test
    void impersonateShouldWriteActingTeacherAndClearStudentKeys() {
        var findTeacher = mock(FindTeacher.class);
        var teacherDirectory = mock(TeacherDirectory.class);
        when(findTeacher.findByTelegramChatId(1L)).thenReturn(Optional.of(TeacherId.generate()));
        when(teacherDirectory.findById(any())).thenReturn(Optional.of(
                new TeacherInfo(TeacherId.generate(), "Admin", true)));
        var activeMenuSupport = mock(ActiveMenuSupport.class);
        when(activeMenuSupport.render(any())).thenReturn(MenuView.activeMenu());

        var teacherId = TeacherId.generate();
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ADMIN_TEACHER_STUDENTS);
        chat.getContext().put(ADMIN_SELECTED_TEACHER_NAME.getValue(), "B");
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), "stale");

        var transition = new ImpersonateCbAdminTransition(new AdminGuard(findTeacher, teacherDirectory),
                activeMenuSupport);
        var result = transition.transit(chat, new Action.Callback("adm", "imp:" + teacherId.value(), 1));

        assertThat(chat.getState()).isEqualTo(ChatState.ACTIVE);
        assertThat(chat.getContext().get(ACTING_TEACHER_ID.getValue())).isEqualTo(teacherId.value().toString());
        assertThat(chat.getContext()).doesNotContainKey(SELECTED_STUDENT_ID.getValue());
        assertThat(result).isInstanceOf(ActionResult.TextWithReplyKeyboard.class);
    }
}
