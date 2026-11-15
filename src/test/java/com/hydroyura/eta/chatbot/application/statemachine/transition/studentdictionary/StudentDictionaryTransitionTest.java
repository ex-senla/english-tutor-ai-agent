package com.hydroyura.eta.chatbot.application.statemachine.transition.studentdictionary;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.dictionary.ResetWordLearning;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.dictionary.domain.word.exception.WordResetNotAllowedException;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.DICTIONARY_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentDictionaryTransitionTest {

    private StudentQuery studentQuery;

    private FindWords findWords;

    private ResetWordLearning resetWordLearning;

    private StudentDictionarySupport support;

    private StudentId studentId;

    private DictionaryId dictionaryId;

    @BeforeEach
    void setUp() {
        studentQuery = mock(StudentQuery.class);
        findWords = mock(FindWords.class);
        resetWordLearning = mock(ResetWordLearning.class);
        support = new StudentDictionarySupport(studentQuery, findWords, new ChatbotPaginationProperties());

        studentId = StudentId.generate();
        dictionaryId = DictionaryId.generate();
        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(dictionaryId));
    }

    @Test
    void shouldNavigatePages() {
        var words = IntStream.range(0, 20)
                .mapToObj(i -> new WordProjection(WordInfoId.generate(), "word" + i, Set.of("перевод"),
                        PartOfSpeech.NOUN, WordStatus.NEW))
                .collect(Collectors.toSet());
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(words);

        var transition = new PageCbStudentDictionaryTransition(support);
        var chat = chat(ChatState.STUDENT_DICTIONARY);

        transition.transit(chat, new Action.Callback("wdpg", "next", 1));
        assertThat(chat.getContext().get(DICTIONARY_PAGE.getValue())).isEqualTo(1);

        transition.transit(chat, new Action.Callback("wdpg", "prev", 1));
        assertThat(chat.getContext().get(DICTIONARY_PAGE.getValue())).isEqualTo(0);

        transition.transit(chat, new Action.Callback("wdpg", "prev", 1));
        assertThat(chat.getContext().get(DICTIONARY_PAGE.getValue())).isEqualTo(0);
    }

    @Test
    void shouldResetWordAndReRender() {
        var wordId = WordInfoId.generate();
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(Set.of(
                new WordProjection(wordId, "big", Set.of("большой"), PartOfSpeech.ADJECTIVE, WordStatus.LEARNED)));

        var transition = new ResetCbStudentDictionaryTransition(studentQuery, resetWordLearning, support);
        var chat = chat(ChatState.STUDENT_DICTIONARY);

        var result = transition.transit(chat, new Action.Callback("wdreset", wordId.value().toString(), 1));

        verify(resetWordLearning).execute(dictionaryId, wordId);
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_DICTIONARY);
    }

    @Test
    void shouldReRenderWithoutMutationWhenResetIsRejected() {
        var wordId = WordInfoId.generate();
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(Set.of(
                new WordProjection(wordId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW)));
        doThrow(new WordResetNotAllowedException(WordStatus.NEW))
                .when(resetWordLearning).execute(dictionaryId, wordId);

        var transition = new ResetCbStudentDictionaryTransition(studentQuery, resetWordLearning, support);
        var chat = chat(ChatState.STUDENT_DICTIONARY);

        var result = transition.transit(chat, new Action.Callback("wdreset", wordId.value().toString(), 1));

        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_DICTIONARY);
    }

    @Test
    void shouldGoBackToStudentDetails() {
        when(studentQuery.findStudentDetails(studentId)).thenReturn(Optional.of(
                new StudentDetails("Anna", new DictionaryStats(1, 1, 0, 0), false, CefrLevel.B1)));

        var transition = new BackCbStudentDictionaryTransition(studentQuery);
        var chat = chat(ChatState.STUDENT_DICTIONARY);

        var result = transition.transit(chat, new Action.Callback("wdback", "", 1));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_DETAILS);
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
    }

    private Chat chat(ChatState state) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(state);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Anna");
        return chat;
    }
}
