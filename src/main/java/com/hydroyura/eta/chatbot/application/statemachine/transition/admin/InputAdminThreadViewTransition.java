package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.notification.BotNotifier;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.admin.AdminView;
import com.hydroyura.eta.support.api.support.AppendSupportMessage;
import com.hydroyura.eta.support.api.support.AppendSupportMessageCommand;
import com.hydroyura.eta.support.api.support.FindSupportMessages;
import com.hydroyura.eta.support.api.support.FindSupportThreads;
import com.hydroyura.eta.support.api.support.SupportAuthor;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SUPPORT_THREAD_ID;
import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_ACCESS_DENIED;
import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_THREAD_NOT_FOUND;
import static com.hydroyura.eta.chatbot.view.Messages.SUPPORT_ANSWER_PREFIX;

@RequiredArgsConstructor
public class InputAdminThreadViewTransition implements Transition<Action.Input> {

    private final AppendSupportMessage appendSupportMessage;

    private final FindSupportThreads findSupportThreads;

    private final FindSupportMessages findSupportMessages;

    private final TeacherDirectory teacherDirectory;

    private final BotNotifier botNotifier;

    private final AdminGuard adminGuard;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        if (!adminGuard.isAdmin(chat)) {
            return new ActionResult.TextResponse(ADMIN_ACCESS_DENIED);
        }
        var threadId = threadId(chat);
        var thread = findSupportThreads.findById(threadId).orElse(null);
        if (thread == null) {
            return new ActionResult.TextResponse(ADMIN_THREAD_NOT_FOUND);
        }

        appendSupportMessage.append(new AppendSupportMessageCommand(threadId, SupportAuthor.ADMIN, input.text()));

        teacherDirectory.findTelegramChatId(thread.teacherId())
                .ifPresent(targetChatId -> botNotifier.notify(targetChatId, SUPPORT_ANSWER_PREFIX + input.text()));

        var teacherName = teacherDirectory.findById(thread.teacherId())
                .map(info -> info.name())
                .orElse("?");
        return AdminView.threadView(teacherName, findSupportMessages.findByThreadId(threadId));
    }

    private SupportThreadId threadId(Chat chat) {
        var raw = Objects.requireNonNull((String) chat.getContext().get(SUPPORT_THREAD_ID.getValue()),
                "supportThreadId must be present in chat context");
        return new SupportThreadId(UUID.fromString(raw));
    }
}
