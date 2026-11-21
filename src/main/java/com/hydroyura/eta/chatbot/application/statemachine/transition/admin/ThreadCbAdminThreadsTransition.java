package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.admin.AdminView;
import com.hydroyura.eta.support.api.support.FindSupportMessages;
import com.hydroyura.eta.support.api.support.FindSupportThreads;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SUPPORT_THREAD_ID;
import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_ACCESS_DENIED;
import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_THREAD_NOT_FOUND;

@RequiredArgsConstructor
public class ThreadCbAdminThreadsTransition implements Transition<Action.Callback> {

    private final FindSupportThreads findSupportThreads;

    private final FindSupportMessages findSupportMessages;

    private final TeacherDirectory teacherDirectory;

    private final AdminGuard adminGuard;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        if (!adminGuard.isAdmin(chat)) {
            return new ActionResult.TextResponse(ADMIN_ACCESS_DENIED);
        }
        var threadId = new SupportThreadId(UUID.fromString(callback.payload()
                .substring((com.hydroyura.eta.chatbot.view.Callbacks.THREAD + ":").length())));
        var thread = findSupportThreads.findById(threadId).orElse(null);
        if (thread == null) {
            return new ActionResult.TextResponse(ADMIN_THREAD_NOT_FOUND);
        }

        var teacherName = teacherDirectory.findById(thread.teacherId())
                .map(info -> info.name())
                .orElse("?");

        chat.getContext().put(SUPPORT_THREAD_ID.getValue(), threadId.value().toString());
        chat.updateState(ChatState.ADMIN_THREAD_VIEW);
        return AdminView.threadView(teacherName, findSupportMessages.findByThreadId(threadId));
    }
}
