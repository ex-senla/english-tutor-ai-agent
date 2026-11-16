package com.hydroyura.eta.chatbot.application.statemachine.transition.cancel;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.lesson.LessonView;
import com.hydroyura.eta.chatbot.view.menu.MenuView;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTIVE_LESSON_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.PENDING_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;
import static com.hydroyura.eta.chatbot.view.Messages.CANCELLED;
import static com.hydroyura.eta.chatbot.view.Messages.NOTHING_TO_CANCEL;
import static org.assertj.core.api.Assertions.assertThat;

class CancelCmdTransitionTest {

    @Test
    void shouldReturnToActiveAndRemovePendingName() {
        var rule = new CancelRule(ChatState.ACTIVE, List.of(PENDING_STUDENT_NAME));
        var renderer = (CancelScreenRenderer) chat -> MenuView.activeMenu();
        var transition = new CancelCmdTransition(rule, renderer);

        var chat = chat(ChatState.AWAITING_STUDENT_NAME);
        chat.getContext().put(PENDING_STUDENT_NAME.getValue(), "Иван");
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), "id");
        chat.getContext().put(ACTIVE_LESSON_ID.getValue(), "lesson");

        var result = transition.transit(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.ACTIVE);
        assertThat(chat.getContext()).doesNotContainKey(PENDING_STUDENT_NAME.getValue());
        assertThat(chat.getContext()).containsKeys(SELECTED_STUDENT_ID.getValue(), ACTIVE_LESSON_ID.getValue());
        assertThat(((ActionResult.TextWithReplyKeyboard) result).text()).contains(CANCELLED);
    }

    @Test
    void shouldRemoveOnlyWordValueFromAwaitingPos() {
        var rule = new CancelRule(ChatState.IN_LESSON, List.of(WORD_VALUE));
        var renderer = (CancelScreenRenderer) chat -> LessonView.lessonKeyboard("lesson");
        var transition = new CancelCmdTransition(rule, renderer);

        var chat = chat(ChatState.AWAITING_POS);
        chat.getContext().put(WORD_VALUE.getValue(), "apple");
        chat.getContext().put(WORD_POS.getValue(), "noun");
        chat.getContext().put(WORD_TRANSLATIONS.getValue(), List.of("яблоко"));

        transition.transit(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(chat.getContext()).doesNotContainKey(WORD_VALUE.getValue());
        assertThat(chat.getContext()).containsKeys(WORD_POS.getValue(), WORD_TRANSLATIONS.getValue());
    }

    @Test
    void shouldRemoveAllWordFlowKeysFromTranslationChoice() {
        var rule = new CancelRule(ChatState.IN_LESSON, List.of(WORD_VALUE, WORD_POS, WORD_TRANSLATIONS));
        var renderer = (CancelScreenRenderer) chat -> LessonView.lessonKeyboard("lesson");
        var transition = new CancelCmdTransition(rule, renderer);

        var chat = chat(ChatState.AWAITING_TRANSLATION_CHOICE);
        chat.getContext().put(WORD_VALUE.getValue(), "apple");
        chat.getContext().put(WORD_POS.getValue(), "noun");
        chat.getContext().put(WORD_TRANSLATIONS.getValue(), List.of("яблоко"));

        transition.transit(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(chat.getContext()).doesNotContainKeys(WORD_VALUE.getValue(), WORD_POS.getValue(),
                WORD_TRANSLATIONS.getValue());
    }

    @Test
    void shouldRemoveExerciseFlowKeysAndPreserveSelection() {
        var rule = new CancelRule(ChatState.STUDENT_OPTIONS, List.of(EXERCISE_TYPE, EXERCISE_TOPIC));
        var renderer = (CancelScreenRenderer) chat -> StudentView.optionsFresh("Anna");
        var transition = new CancelCmdTransition(rule, renderer);

        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC);
        chat.getContext().put(EXERCISE_TYPE.getValue(), "FILL_IN_THE_BLANK");
        chat.getContext().put(EXERCISE_TOPIC.getValue(), "Past Simple");
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), "id");
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Anna");

        var result = transition.transit(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext()).doesNotContainKeys(EXERCISE_TYPE.getValue(), EXERCISE_TOPIC.getValue());
        assertThat(chat.getContext()).containsKeys(SELECTED_STUDENT_ID.getValue(), SELECTED_STUDENT_NAME.getValue());
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldNotChangeStateWhenNothingToCancel() {
        var transition = new NoCancelCmdTransition();

        var chat = chat(ChatState.ACTIVE);

        var result = transition.transit(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.ACTIVE);
        assertThat(((ActionResult.TextResponse) result).text()).isEqualTo(NOTHING_TO_CANCEL);
    }

    private Chat chat(ChatState state) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(state);
        return chat;
    }
}
