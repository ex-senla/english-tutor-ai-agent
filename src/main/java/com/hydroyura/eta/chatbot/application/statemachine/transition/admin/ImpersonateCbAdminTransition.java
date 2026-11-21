package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActiveMenuSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTIVE_LESSON_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTING_TEACHER_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.DICTIONARY_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_THEMATIC_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.PENDING_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_CANONICAL_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_MATCHED_SLUG;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_NODE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_SEARCH_QUERY;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;

@RequiredArgsConstructor
public class ImpersonateCbAdminTransition implements Transition<Action.Callback> {

    private static final List<com.hydroyura.eta.chatbot.domain.chat.ContextKey> TEACHER_BOUND_KEYS = List.of(
            SELECTED_STUDENT_ID, SELECTED_STUDENT_NAME, ACTIVE_LESSON_ID, PENDING_STUDENT_NAME,
            DICTIONARY_PAGE, WORD_POS, WORD_VALUE, WORD_TRANSLATIONS,
            EXERCISE_ID, EXERCISE_TYPE, EXERCISE_TOPIC, EXERCISE_THEMATIC_TOPIC,
            TOPIC_SEARCH_QUERY, TOPIC_PAGE, TOPIC_NODE, TOPIC_CANONICAL_NAME, TOPIC_MATCHED_SLUG,
            WORD_SELECTION, WORD_FILTER, SELECTION_PAGE);

    private final AdminGuard adminGuard;

    private final ActiveMenuSupport activeMenuSupport;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        if (!adminGuard.isAdmin(chat)) {
            return new ActionResult.TextResponse(com.hydroyura.eta.chatbot.view.Messages.ADMIN_ACCESS_DENIED);
        }

        var teacherId = new TeacherId(UUID.fromString(callback.payload()
                .substring((com.hydroyura.eta.chatbot.view.Callbacks.IMPERSONATE + ":").length())));
        chat.getContext().put(ACTING_TEACHER_ID.getValue(), teacherId.value().toString());
        TEACHER_BOUND_KEYS.forEach(key -> chat.getContext().remove(key.getValue()));
        chat.updateState(ChatState.ACTIVE);
        return activeMenuSupport.render(chat);
    }
}
