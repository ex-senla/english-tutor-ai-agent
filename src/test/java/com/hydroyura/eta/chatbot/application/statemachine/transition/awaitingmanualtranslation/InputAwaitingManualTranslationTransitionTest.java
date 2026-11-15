package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingmanualtranslation;

import com.hydroyura.eta.chatbot.application.statemachine.transition.word.LessonWordSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InputAwaitingManualTranslationTransitionTest {

    private StudentQuery studentQuery;

    private AddWordToDictionary addWordToDictionary;

    private FindActiveLesson findActiveLesson;

    private AddWordToLesson addWordToLesson;

    private InputAwaitingManualTranslationTransition transition;

    private final StudentId studentId = StudentId.generate();

    @BeforeEach
    void setUp() {
        studentQuery = mock(StudentQuery.class);
        addWordToDictionary = mock(AddWordToDictionary.class);
        findActiveLesson = mock(FindActiveLesson.class);
        addWordToLesson = mock(AddWordToLesson.class);

        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(DictionaryId.generate()));
        when(findActiveLesson.findByStudentId(studentId)).thenReturn(Optional.of(LessonId.generate()));
        when(addWordToDictionary.execute(any())).thenReturn(WordInfoId.generate());

        var support = new LessonWordSupport(studentQuery, addWordToDictionary, findActiveLesson, addWordToLesson);
        transition = new InputAwaitingManualTranslationTransition(support);
    }

    @Test
    void shouldAddMultipleTranslations() {
        var chat = chat();
        var result = transition.transit(chat, new Action.Input("собака, пёс"));

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(result).isInstanceOf(ActionResult.TextWithReplyKeyboard.class);

        var captor = ArgumentCaptor.forClass(AddWordCommand.class);
        verify(addWordToDictionary).execute(captor.capture());
        assertThat(captor.getValue().translations()).containsExactlyInAnyOrder("собака", "пёс");
    }

    @Test
    void shouldDeduplicateTranslations() {
        var chat = chat();
        var result = transition.transit(chat, new Action.Input("собака, собака"));

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(result).isInstanceOf(ActionResult.TextWithReplyKeyboard.class);

        var captor = ArgumentCaptor.forClass(AddWordCommand.class);
        verify(addWordToDictionary).execute(captor.capture());
        assertThat(captor.getValue().translations()).containsExactly("собака");
    }

    @Test
    void shouldStayInManualTranslationOnValidationError() {
        when(addWordToDictionary.execute(any())).thenThrow(new WordValidationException("apple"));

        var chat = chat();
        var result = transition.transit(chat, new Action.Input("dog"));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_MANUAL_TRANSLATION);
        assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
    }

    @Test
    void shouldReturnErrorOnBlankTranslations() {
        var chat = chat();
        var result = transition.transit(chat, new Action.Input(", ,"));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_MANUAL_TRANSLATION);
        assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
        verifyNoInteractions(addWordToDictionary, addWordToLesson, findActiveLesson);
    }

    private Chat chat() {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_MANUAL_TRANSLATION);
        chat.getContext().put(WORD_VALUE.getValue(), "apple");
        chat.getContext().put(WORD_POS.getValue(), PartOfSpeech.NOUN);
        chat.getContext().put("selectedStudentId", studentId.value().toString());
        return chat;
    }
}
