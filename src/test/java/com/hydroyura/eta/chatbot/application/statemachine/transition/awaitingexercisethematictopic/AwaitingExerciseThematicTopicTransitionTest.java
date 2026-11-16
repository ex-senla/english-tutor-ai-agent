package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisethematictopic;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic.TopicFlowSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.util.ViewUtils;
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.application.topic.GrammarTopicCatalog;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_THEMATIC_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;
import static com.hydroyura.eta.chatbot.view.Messages.THEMATIC_TOPIC_EMPTY;
import static com.hydroyura.eta.chatbot.view.Messages.THEMATIC_TOPIC_TOO_LONG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AwaitingExerciseThematicTopicTransitionTest {

    private TopicFlowSupport support;

    private StudentId studentId;

    @BeforeEach
    void setUp() {
        var studentQuery = mock(StudentQuery.class);
        var findWords = mock(FindWords.class);
        studentId = StudentId.generate();
        var dictionaryId = DictionaryId.generate();
        var inProgressId = WordInfoId.generate();

        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(dictionaryId));
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(Set.of(
                new WordProjection(inProgressId, "travel", Set.of("путешествовать"), PartOfSpeech.VERB,
                        WordStatus.IN_PROGRESS)));

        support = new TopicFlowSupport(studentQuery, findWords,
                new GrammarTopicCatalog("/exercise/topics/test-grammar-topics.json"),
                new ChatbotPaginationProperties());
    }

    @Test
    void shouldRejectEmptyInputAndStayOnStep() {
        var transition = new InputAwaitingExerciseThematicTopicTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);

        var result = transition.transit(chat, new Action.Input("   "));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);
        assertThat(chat.getContext()).doesNotContainKey(EXERCISE_THEMATIC_TOPIC.getValue());
        assertThat(((ActionResult.TextWithInlineKeyboard) result).text()).isEqualTo(THEMATIC_TOPIC_EMPTY);
    }

    @Test
    void shouldRejectTooLongInputAndStayOnStep() {
        var transition = new InputAwaitingExerciseThematicTopicTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);

        var result = transition.transit(chat, new Action.Input("a".repeat(101)));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);
        assertThat(chat.getContext()).doesNotContainKey(EXERCISE_THEMATIC_TOPIC.getValue());
        assertThat(((ActionResult.TextWithInlineKeyboard) result).text()).isEqualTo(THEMATIC_TOPIC_TOO_LONG);
    }

    @Test
    void shouldTrimAndSaveThematicTopicThenEnterWordSelection() {
        var transition = new InputAwaitingExerciseThematicTopicTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);
        chat.getContext().put(EXERCISE_TOPIC.getValue(), new CatalogTopic("present-simple"));

        var result = transition.transit(chat, new Action.Input("  Food & drinks!!  "));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_WORD_SELECTION);
        assertThat(chat.getContext().get(EXERCISE_THEMATIC_TOPIC.getValue())).isEqualTo("Food & drinks!!");
        assertThat(chat.getContext().get(EXERCISE_TOPIC.getValue())).isEqualTo(new CatalogTopic("present-simple"));
        assertThat(chat.getContext().get(WORD_FILTER.getValue())).isEqualTo("IN_PROGRESS");
        assertThat(chat.getContext().get(SELECTION_PAGE.getValue())).isEqualTo(0);
        assertThat(chat.getContext().get(WORD_SELECTION.getValue())).isInstanceOf(Set.class);
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldSkipAndNotSaveThematicTopic() {
        var transition = new SkipCbAwaitingExerciseThematicTopicTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);
        chat.getContext().put(EXERCISE_THEMATIC_TOPIC.getValue(), "stale");

        var result = transition.transit(chat, new Action.Callback("topicskip", "", 1));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_WORD_SELECTION);
        assertThat(chat.getContext()).doesNotContainKey(EXERCISE_THEMATIC_TOPIC.getValue());
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldDefineTopicSkipCallbackWithinTelegramLimit() {
        var callbackData = ViewUtils.createCallbackData(Callbacks.TOPIC_SKIP);

        assertThat(Callbacks.TOPIC_SKIP).isEqualTo("topicskip");
        assertThat(callbackData).isEqualTo("topicskip");
        assertThat(callbackData.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isLessThan(64);
    }

    private Chat chat(ChatState state) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(state);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        return chat;
    }
}
