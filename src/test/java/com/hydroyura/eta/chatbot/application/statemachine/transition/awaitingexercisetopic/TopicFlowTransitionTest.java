package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisethematictopic.InputAwaitingExerciseThematicTopicTransition;
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
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.application.topic.GrammarTopicCatalog;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_NODE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_SEARCH_QUERY;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TopicFlowTransitionTest {

    private TopicFlowSupport support;

    private StudentId studentId;

    private DictionaryId dictionaryId;

    @BeforeEach
    void setUp() {
        var studentQuery = mock(StudentQuery.class);
        var findWords = mock(FindWords.class);
        studentId = StudentId.generate();
        dictionaryId = DictionaryId.generate();

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
    void shouldNavigateToNodeAndShowChildren() {
        var transition = new TopicNavCbAwaitingExerciseTopicTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC);

        var result = transition.transit(chat, new Action.Callback("topicnav", "present", 1));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_TOPIC);
        assertThat(chat.getContext().get(TOPIC_NODE.getValue())).isEqualTo("present");
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
    }

    @Test
    void shouldSelectCatalogTopicAndEnterThematicTopic() {
        var transition = new TopicCbAwaitingExerciseTopicTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC);

        var result = transition.transit(chat, new Action.Callback("topic", "present-simple", 1));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);
        assertThat(chat.getContext().get(EXERCISE_TOPIC.getValue())).isEqualTo(new CatalogTopic("present-simple"));
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldSearchByText() {
        var transition = new InputAwaitingExerciseTopicTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC);

        var result = transition.transit(chat, new Action.Input("simple"));

        assertThat(chat.getContext().get(TOPIC_SEARCH_QUERY.getValue())).isEqualTo("simple");
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldShowNothingFoundForSearchWithoutResults() {
        var transition = new InputAwaitingExerciseTopicTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC);

        var result = transition.transit(chat, new Action.Input("zzz"));

        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
        assertThat(((ActionResult.TextWithInlineKeyboard) result).text()).contains("Ничего не найдено");
    }

    @Test
    void shouldEnterSelfTopic() {
        var transition = new TopicSelfCbAwaitingExerciseTopicTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC);

        var result = transition.transit(chat, new Action.Callback("topicself", "", 1));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_CUSTOM_TOPIC);
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
    }

    @Test
    void shouldPreselectInProgressWordsOnWordSelection() {
        var topicTransition = new TopicCbAwaitingExerciseTopicTransition(support);
        var thematicTransition = new InputAwaitingExerciseThematicTopicTransition(support);
        var chat = chat(ChatState.AWAITING_EXERCISE_TOPIC);

        topicTransition.transit(chat, new Action.Callback("topic", "present-simple", 1));
        thematicTransition.transit(chat, new Action.Input("Travel"));

        var selection = (Set<?>) chat.getContext().get(WORD_SELECTION.getValue());
        assertThat(selection).isNotEmpty();
        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_WORD_SELECTION);
    }

    private Chat chat(ChatState state) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(state);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        return chat;
    }
}
