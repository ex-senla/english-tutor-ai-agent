package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingtranslationchoice;

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
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;
import static com.hydroyura.eta.chatbot.view.Messages.WORD_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TranslationCbAwaitingTranslationChoiceTransitionTest {

    private AddWordToDictionary addWordToDictionary;

    private TranslationCbAwaitingTranslationChoiceTransition transition;

    private final StudentId studentId = StudentId.generate();

    @BeforeEach
    void setUp() {
        var studentQuery = mock(StudentQuery.class);
        addWordToDictionary = mock(AddWordToDictionary.class);
        var findActiveLesson = mock(FindActiveLesson.class);
        var addWordToLesson = mock(AddWordToLesson.class);

        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(DictionaryId.generate()));
        when(findActiveLesson.findByStudentId(studentId)).thenReturn(Optional.of(LessonId.generate()));
        when(addWordToDictionary.execute(any())).thenReturn(WordInfoId.generate());

        var support = new LessonWordSupport(studentQuery, addWordToDictionary, findActiveLesson, addWordToLesson);
        transition = new TranslationCbAwaitingTranslationChoiceTransition(support);
    }

    @Test
    void shouldAddSelectedTranslation() {
        var chat = chat();
        chat.getContext().put(WORD_TRANSLATIONS.getValue(), List.of("яблоко", "фрукт"));

        var result = transition.transit(chat, new Action.Callback("translation", "1", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(result).isInstanceOf(ActionResult.TextWithReplyKeyboard.class);

        var captor = ArgumentCaptor.forClass(AddWordCommand.class);
        verify(addWordToDictionary).execute(captor.capture());
        assertThat(captor.getValue().translations()).containsExactly("фрукт");
    }

    @Test
    void shouldMoveToManualTranslationOnOwn() {
        var chat = chat();

        var result = transition.transit(chat, new Action.Callback("translation", "own", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_MANUAL_TRANSLATION);
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        verifyNoInteractions(addWordToDictionary);
    }

    @Test
    void shouldReturnToLessonOnOutOfRangeIndex() {
        var chat = chat();
        chat.getContext().put(WORD_TRANSLATIONS.getValue(), List.of("яблоко"));

        var result = transition.transit(chat, new Action.Callback("translation", "5", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(((ActionResult.TextWithReplyKeyboard) result).text()).contains(WORD_NOT_FOUND);
    }

    @Test
    void shouldReturnToLessonOnNonNumericPayload() {
        var chat = chat();
        chat.getContext().put(WORD_TRANSLATIONS.getValue(), List.of("яблоко"));

        var result = transition.transit(chat, new Action.Callback("translation", "abc", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(((ActionResult.TextWithReplyKeyboard) result).text()).contains(WORD_NOT_FOUND);
    }

    @Test
    void shouldReturnToLessonWhenTranslationsMissing() {
        var chat = chat();

        var result = transition.transit(chat, new Action.Callback("translation", "0", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(((ActionResult.TextWithReplyKeyboard) result).text()).contains(WORD_NOT_FOUND);
    }

    private Chat chat() {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_TRANSLATION_CHOICE);
        chat.getContext().put(WORD_VALUE.getValue(), "apple");
        chat.getContext().put(WORD_POS.getValue(), PartOfSpeech.NOUN);
        chat.getContext().put("selectedStudentId", studentId.value().toString());
        return chat;
    }
}
