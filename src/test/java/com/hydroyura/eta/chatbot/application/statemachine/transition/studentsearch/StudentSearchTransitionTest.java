package com.hydroyura.eta.chatbot.application.statemachine.transition.studentsearch;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotSearchProperties;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.view.Messages.SEARCH_NOTHING_FOUND;
import static com.hydroyura.eta.chatbot.view.Messages.STUDENT_NOT_FOUND_OR_DELETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StudentSearchTransitionTest {

    @Test
    void shouldEnterSearchState() {
        var transition = new StudentSearchTransition();

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.ACTIVE);

        var result = transition.transit(chat, new Action.Command("/find"));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_SEARCH);
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldReturnNothingFoundOnBlankFragmentWithoutQuery() {
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        var transition = new InputStudentSearchTransition(findTeacher, studentQuery, new ChatbotSearchProperties());

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENT_SEARCH);

        var result = transition.transit(chat, new Action.Input("   "));

        assertThat(((ActionResult.TextWithInlineKeyboard) result).text()).isEqualTo(SEARCH_NOTHING_FOUND);
        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_SEARCH);
        verifyNoInteractions(findTeacher, studentQuery);
    }

    @Test
    void shouldReturnNothingFoundOnEmptyResult() {
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(StudentId.generate()));
        when(studentQuery.searchByNameFragment(any())).thenReturn(List.of());
        var transition = new InputStudentSearchTransition(findTeacher, studentQuery, new ChatbotSearchProperties());

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENT_SEARCH);

        var result = transition.transit(chat, new Action.Input("zzz"));

        assertThat(((ActionResult.TextWithInlineKeyboard) result).text()).isEqualTo(SEARCH_NOTHING_FOUND);
        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_SEARCH);
    }

    @Test
    void shouldLimitResultsToConfiguredSuggestions() {
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        var properties = new ChatbotSearchProperties();
        properties.setSuggestionsLimit(3);
        var students = IntStream.range(0, 10)
                .mapToObj(i -> new StudentInfo(StudentId.generate(), "Student" + i))
                .toList();
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(StudentId.generate()));
        when(studentQuery.searchByNameFragment(any())).thenReturn(students);
        var transition = new InputStudentSearchTransition(findTeacher, studentQuery, properties);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENT_SEARCH);

        var result = transition.transit(chat, new Action.Input("st"));

        var keyboard = ((ActionResult.TextWithInlineKeyboard) result).keyboard();
        assertThat(keyboard).hasSize(4); // 3 suggestions + back
    }

    @Test
    void shouldSelectStudentAndMoveToOptions() {
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        var studentId = StudentId.generate();
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(studentId));
        when(studentQuery.findStudentsByIds(Set.of(studentId))).thenReturn(List.of(new StudentInfo(studentId, "Anna")));
        var transition = new StudentCbStudentSearchTransition(findTeacher, studentQuery);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENT_SEARCH);

        var result = transition.transit(chat, new Action.Callback("student", studentId.value().toString(), 42));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext().get(SELECTED_STUDENT_ID.getValue())).isEqualTo(studentId.value().toString());
        assertThat(chat.getContext().get(SELECTED_STUDENT_NAME.getValue())).isEqualTo("Anna");
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
    }

    @Test
    void shouldReturnToActiveMenu() {
        var transition = new BackCbStudentSearchTransition();

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENT_SEARCH);

        var result = transition.transit(chat, new Action.Callback("back", "main", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.ACTIVE);
        assertThat(result).isInstanceOf(ActionResult.TextWithReplyKeyboard.class);
    }

    @Test
    void shouldHandleDeletedStudentFromSearchWithoutPollutingContext() {
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        var deletedId = StudentId.generate();
        var aliveId = StudentId.generate();
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(deletedId, aliveId));
        when(studentQuery.findStudentsByIds(Set.of(deletedId, aliveId)))
                .thenReturn(List.of(new StudentInfo(aliveId, "Bob")));
        var transition = new StudentCbStudentSearchTransition(findTeacher, studentQuery);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENT_SEARCH);

        var result = transition.transit(chat, new Action.Callback("student", deletedId.value().toString(), 42));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENTS_LIST);
        assertThat(chat.getContext()).doesNotContainKeys(SELECTED_STUDENT_ID.getValue(), SELECTED_STUDENT_NAME
                .getValue());
        assertThat(((ActionResult.EditMessageText) result).text()).isEqualTo(STUDENT_NOT_FOUND_OR_DELETED);
    }

    @Test
    void shouldHandleNonExistingStudentFromSearch() {
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        var aliveId = StudentId.generate();
        var unknownId = StudentId.generate();
        when(findTeacher.getStudentIds(1L)).thenReturn(Set.of(aliveId));
        when(studentQuery.findStudentsByIds(Set.of(aliveId))).thenReturn(List.of(new StudentInfo(aliveId, "Bob")));
        var transition = new StudentCbStudentSearchTransition(findTeacher, studentQuery);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.STUDENT_SEARCH);

        var result = transition.transit(chat, new Action.Callback("student", unknownId.value().toString(), 42));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENTS_LIST);
        assertThat(chat.getContext()).doesNotContainKeys(SELECTED_STUDENT_ID.getValue(), SELECTED_STUDENT_NAME
                .getValue());
        assertThat(((ActionResult.EditMessageText) result).text()).isEqualTo(STUDENT_NOT_FOUND_OR_DELETED);
    }
}
