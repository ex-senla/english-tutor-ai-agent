package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
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
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.GRAMMAR_RULE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InputAwaitingExerciseTopicTransitionTest {

    @Test
    void shouldMoveToWordSelectionAndPreselectInProgressWords() {
        var studentQuery = mock(StudentQuery.class);
        var findWords = mock(FindWords.class);

        var studentId = StudentId.generate();
        var dictionaryId = DictionaryId.generate();
        var inProgressId = WordInfoId.generate();
        var newId = WordInfoId.generate();
        var learnedId = WordInfoId.generate();

        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(dictionaryId));
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(Set.of(
                new WordProjection(inProgressId, "travel", Set.of("путешествовать"), PartOfSpeech.VERB,
                        WordStatus.IN_PROGRESS),
                new WordProjection(newId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW),
                new WordProjection(learnedId, "big", Set.of("большой"), PartOfSpeech.ADJECTIVE, WordStatus.LEARNED)));

        var transition = new InputAwaitingExerciseTopicTransition(studentQuery, findWords,
                new ChatbotPaginationProperties());

        var chat = chat(studentId);
        chat.getContext().put(GRAMMAR_RULE.getValue(), "Past Simple");
        chat.getContext().put(EXERCISE_TYPE.getValue(),
                com.hydroyura.eta.exercise.api.exercise.ExerciseType.FILL_IN_THE_BLANK);

        var result = transition.transit(chat, new Action.Input("Animals"));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_WORD_SELECTION);
        assertThat(chat.getContext().get(EXERCISE_TOPIC.getValue())).isEqualTo("Animals");
        assertThat(chat.getContext().get(WORD_FILTER.getValue())).isEqualTo("IN_PROGRESS");
        assertThat(chat.getContext().get(SELECTION_PAGE.getValue())).isEqualTo(0);

        var selection = (Set<String>) chat.getContext().get(WORD_SELECTION.getValue());
        assertThat(selection).containsExactlyInAnyOrder(inProgressId.value().toString());

        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    private Chat chat(StudentId studentId) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Alice");
        return chat;
    }
}
