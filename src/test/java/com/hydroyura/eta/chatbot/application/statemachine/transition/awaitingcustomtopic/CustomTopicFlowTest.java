package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingcustomtopic;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic.TopicFlowSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopicconfirm.TopicCatalogCbAwaitingExerciseTopicConfirmTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopicconfirm.TopicConfirmCbAwaitingExerciseTopicConfirmTransition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.CustomTopic;
import com.hydroyura.eta.exercise.api.exercise.TopicValidationResult;
import com.hydroyura.eta.exercise.api.exercise.ValidateCustomTopic;
import com.hydroyura.eta.exercise.application.topic.GrammarTopicCatalog;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_CANONICAL_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_MATCHED_SLUG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomTopicFlowTest {

    private ValidateCustomTopic validateCustomTopic;

    private TopicFlowSupport support;

    private StudentId studentId;

    @BeforeEach
    void setUp() {
        var studentQuery = mock(StudentQuery.class);
        var findWords = mock(FindWords.class);
        studentId = StudentId.generate();
        var dictionaryId = DictionaryId.generate();

        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(dictionaryId));
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(Set.of());

        validateCustomTopic = mock(ValidateCustomTopic.class);
        support = new TopicFlowSupport(studentQuery, findWords,
                new GrammarTopicCatalog("/exercise/topics/test-grammar-topics.json"),
                new ChatbotPaginationProperties());
    }

    @Test
    void shouldRejectEmptyInput() {
        var transition = new InputAwaitingCustomTopicTransition(validateCustomTopic, support);
        var chat = chat(ChatState.AWAITING_CUSTOM_TOPIC);

        var result = transition.transit(chat, new Action.Input("   "));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_CUSTOM_TOPIC);
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldRejectTooLongInput() {
        var transition = new InputAwaitingCustomTopicTransition(validateCustomTopic, support);
        var chat = chat(ChatState.AWAITING_CUSTOM_TOPIC);

        var result = transition.transit(chat, new Action.Input("a".repeat(101)));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_CUSTOM_TOPIC);
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldShowConfirmForValidInput() {
        when(validateCustomTopic.validate(any())).thenReturn(
                new TopicValidationResult(true, true, "Past Simple", "past-simple", null));
        var transition = new InputAwaitingCustomTopicTransition(validateCustomTopic, support);
        var chat = chat(ChatState.AWAITING_CUSTOM_TOPIC);

        var result = transition.transit(chat, new Action.Input("past simple"));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_TOPIC_CONFIRM);
        assertThat(chat.getContext().get(TOPIC_CANONICAL_NAME.getValue())).isEqualTo("Past Simple");
        assertThat(chat.getContext().get(TOPIC_MATCHED_SLUG.getValue())).isEqualTo("past-simple");
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldStayOnInvalidInput() {
        when(validateCustomTopic.validate(any())).thenReturn(
                new TopicValidationResult(true, false, "asdfgh", null, "Не тема"));
        var transition = new InputAwaitingCustomTopicTransition(validateCustomTopic, support);
        var chat = chat(ChatState.AWAITING_CUSTOM_TOPIC);

        var result = transition.transit(chat, new Action.Input("asdfgh"));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_CUSTOM_TOPIC);
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldFailOpenToThematicTopic() {
        when(validateCustomTopic.validate(any())).thenReturn(
                new TopicValidationResult(false, true, "Past Simple", null, null));
        var transition = new InputAwaitingCustomTopicTransition(validateCustomTopic, support);
        var chat = chat(ChatState.AWAITING_CUSTOM_TOPIC);

        var result = transition.transit(chat, new Action.Input("Past Simple"));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);
        assertThat(chat.getContext().get(EXERCISE_TOPIC.getValue())).isEqualTo(new CustomTopic("Past Simple"));
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldUseCanonicalTopicOnConfirm() {
        var transition = new TopicConfirmCbAwaitingExerciseTopicConfirmTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC_CONFIRM);
        chat.getContext().put(TOPIC_CANONICAL_NAME.getValue(), "Past Simple");
        chat.getContext().put(TOPIC_MATCHED_SLUG.getValue(), "past-simple");

        var result = transition.transit(chat, new Action.Callback("topicconfirm", "use", 1));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);
        assertThat(chat.getContext().get(EXERCISE_TOPIC.getValue())).isEqualTo(new CustomTopic("Past Simple"));
    }

    @Test
    void shouldReenterCustomTopic() {
        var transition = new TopicConfirmCbAwaitingExerciseTopicConfirmTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC_CONFIRM);
        chat.getContext().put(TOPIC_CANONICAL_NAME.getValue(), "Past Simple");

        var result = transition.transit(chat, new Action.Callback("topicconfirm", "reenter", 1));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_CUSTOM_TOPIC);
        assertThat(chat.getContext()).doesNotContainKey(TOPIC_CANONICAL_NAME.getValue());
    }

    @Test
    void shouldBackToTopicSelection() {
        var transition = new TopicConfirmCbAwaitingExerciseTopicConfirmTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC_CONFIRM);
        chat.getContext().put(TOPIC_CANONICAL_NAME.getValue(), "Past Simple");

        var result = transition.transit(chat, new Action.Callback("topicconfirm", "back", 1));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_TOPIC);
        assertThat(chat.getContext()).doesNotContainKey(TOPIC_CANONICAL_NAME.getValue());
    }

    @Test
    void shouldUseCatalogTopicOnCatalogConfirm() {
        var transition = new TopicCatalogCbAwaitingExerciseTopicConfirmTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC_CONFIRM);
        chat.getContext().put(TOPIC_CANONICAL_NAME.getValue(), "Past Simple");
        chat.getContext().put(TOPIC_MATCHED_SLUG.getValue(), "past-simple");

        var result = transition.transit(chat, new Action.Callback("topiccatalog", "past-simple", 1));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);
        assertThat(chat.getContext().get(EXERCISE_TOPIC.getValue())).isEqualTo(new CatalogTopic("past-simple"));
    }

    private Chat chat(ChatState state) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(state);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        return chat;
    }
}
