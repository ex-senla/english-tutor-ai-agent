package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.exercise.application.topic.GrammarTopicCatalog;
import com.hydroyura.eta.student.api.student.StudentQuery;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_SEARCH_QUERY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InputAwaitingExerciseTopicTransitionTest {

    @Test
    void shouldRepromptOnEmptyQuery() {
        var transition = new InputAwaitingExerciseTopicTransition(support());

        var chat = chat();
        var result = transition.transit(chat, new Action.Input("   "));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_TOPIC);
        assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
        assertThat(((ActionResult.TextResponse) result).text()).contains("поиск");
    }

    @Test
    void shouldSearchAndStoreQuery() {
        var transition = new InputAwaitingExerciseTopicTransition(support());

        var chat = chat();
        var result = transition.transit(chat, new Action.Input("simple"));

        assertThat(chat.getContext().get(TOPIC_SEARCH_QUERY.getValue())).isEqualTo("simple");
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    private TopicFlowSupport support() {
        var studentQuery = mock(StudentQuery.class);
        var findWords = mock(FindWords.class);
        var exerciseTopics = new GrammarTopicCatalog("/exercise/topics/test-grammar-topics.json");
        return new TopicFlowSupport(studentQuery, findWords, exerciseTopics, new ChatbotPaginationProperties());
    }

    private Chat chat() {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_EXERCISE_TOPIC);
        return chat;
    }
}
