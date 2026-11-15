package com.hydroyura.eta.chatbot.application.statemachine.transition.studentoptions;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.DICTIONARY_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActionCbStudentOptionsTransitionTest {

    @Test
    void shouldOpenDictionary() {
        var startLesson = mock(StartLesson.class);
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        var findWords = mock(FindWords.class);

        var studentId = StudentId.generate();
        var dictionaryId = DictionaryId.generate();
        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(dictionaryId));
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(Set.of(
                new WordProjection(WordInfoId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN,
                        WordStatus.NEW)));

        var transition = new ActionCbStudentOptionsTransition(startLesson, findTeacher, studentQuery, findWords,
                new ChatbotPaginationProperties());

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENT_OPTIONS);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Anna");

        var result = transition.transit(chat, new Action.Callback(Callbacks.ACTION, Callbacks.DICTIONARY, 1));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_DICTIONARY);
        assertThat(chat.getContext().get(DICTIONARY_PAGE.getValue())).isEqualTo(0);
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
    }
}
