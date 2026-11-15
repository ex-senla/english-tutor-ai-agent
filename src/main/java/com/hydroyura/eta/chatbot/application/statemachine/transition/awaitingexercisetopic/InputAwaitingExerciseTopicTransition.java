package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection.ExerciseWordSelectionSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.exercise.ExerciseWordSelectionView;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;

@RequiredArgsConstructor
public class InputAwaitingExerciseTopicTransition implements Transition<Action.Input> {

    private final StudentQuery studentQuery;

    private final FindWords findWords;

    private final ChatbotPaginationProperties paginationProperties;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        chat.getContext().put(EXERCISE_TOPIC.getValue(), input.text());
        chat.getContext().put(WORD_FILTER.getValue(), ExerciseWordSelectionView.FILTER_IN_PROGRESS);
        chat.getContext().put(SELECTION_PAGE.getValue(), 0);

        var studentId = StudentSupport.selectedStudentId(chat);
        var dictionaryId = studentQuery.getDictionaryId(studentId)
                .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentId));

        var selectedIds = findWords.findByDictionaryId(dictionaryId).stream()
                .filter(word -> word.status() == WordStatus.IN_PROGRESS)
                .map(word -> word.id().value().toString())
                .collect(Collectors.toSet());
        chat.getContext().put(WORD_SELECTION.getValue(), selectedIds);

        chat.updateState(ChatState.AWAITING_EXERCISE_WORD_SELECTION);

        var support = new ExerciseWordSelectionSupport(studentQuery, findWords, paginationProperties);
        return support.renderFresh(chat);
    }
}
