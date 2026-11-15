package com.hydroyura.eta.chatbot.application.statemachine.transition.word;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.WordAlreadyExistsException;
import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.AddWordToLessonCommand;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LessonWordSupportTest {

    private StudentQuery studentQuery;

    private AddWordToDictionary addWordToDictionary;

    private FindActiveLesson findActiveLesson;

    private AddWordToLesson addWordToLesson;

    private LessonWordSupport support;

    private final StudentId studentId = StudentId.generate();

    private final DictionaryId dictionaryId = DictionaryId.generate();

    private final LessonId lessonId = LessonId.generate();

    @BeforeEach
    void setUp() {
        studentQuery = mock(StudentQuery.class);
        addWordToDictionary = mock(AddWordToDictionary.class);
        findActiveLesson = mock(FindActiveLesson.class);
        addWordToLesson = mock(AddWordToLesson.class);
        support = new LessonWordSupport(studentQuery, addWordToDictionary, findActiveLesson, addWordToLesson);

        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(dictionaryId));
        when(findActiveLesson.findByStudentId(studentId)).thenReturn(Optional.of(lessonId));
    }

    @Test
    void shouldAddWordToDictionaryAndLesson() {
        var wordId = WordInfoId.generate();
        when(addWordToDictionary.execute(any())).thenReturn(wordId);
        var chat = chat();

        var result = support.addWord(chat, "apple", Set.of("яблоко"), PartOfSpeech.NOUN);

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(result).isInstanceOf(ActionResult.TextWithReplyKeyboard.class);

        var captor = ArgumentCaptor.forClass(AddWordToLessonCommand.class);
        verify(addWordToLesson).execute(captor.capture());
        assertThat(captor.getValue().lessonId()).isEqualTo(lessonId);
        assertThat(captor.getValue().wordInfoId()).isEqualTo(wordId);
    }

    @Test
    void shouldReturnDuplicateFeedback() {
        when(addWordToDictionary.execute(any())).thenThrow(new WordAlreadyExistsException("apple"));
        var chat = chat();

        var result = support.addWord(chat, "apple", Set.of("яблоко"), PartOfSpeech.NOUN);

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(((ActionResult.TextWithReplyKeyboard) result).text()).contains("уже есть в словаре");
    }

    @Test
    void shouldPropagateValidationExceptionFromRawAdd() {
        when(addWordToDictionary.execute(any())).thenThrow(new WordValidationException("apple"));

        assertThatThrownBy(() -> support.addWordToDictionaryAndLesson(chat(), "apple", Set.of("dog"),
                PartOfSpeech.NOUN)).isInstanceOf(WordValidationException.class);
    }

    private Chat chat() {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.getContext().put("selectedStudentId", studentId.value().toString());
        chat.updateState(ChatState.IN_LESSON);
        return chat;
    }
}
