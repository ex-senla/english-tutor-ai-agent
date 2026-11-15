package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.exercise.ExerciseWordSelectionView;
import com.hydroyura.eta.chatbot.view.util.ViewUtils;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;

@RequiredArgsConstructor
public final class ExerciseWordSelectionSupport {

    private final StudentQuery studentQuery;

    private final FindWords findWords;

    private final ChatbotPaginationProperties paginationProperties;

    public ActionResult render(Chat chat, int messageId) {
        var words = sortedFilteredWords(chat);
        var totalPages = ViewUtils.pageCount(words.size(), paginationProperties.getPageSize());
        var page = clampPage(currentPage(chat), totalPages);
        var pageWords = ViewUtils.pageSlice(words, page, paginationProperties.getPageSize());

        return ExerciseWordSelectionView.selection(messageId, pageWords, page, totalPages,
                selectedIdStrings(chat), currentFilter(chat));
    }

    public ActionResult renderFresh(Chat chat) {
        var words = sortedFilteredWords(chat);
        var totalPages = ViewUtils.pageCount(words.size(), paginationProperties.getPageSize());
        var page = clampPage(currentPage(chat), totalPages);
        var pageWords = ViewUtils.pageSlice(words, page, paginationProperties.getPageSize());

        return ExerciseWordSelectionView.selectionFresh(pageWords, page, totalPages,
                selectedIdStrings(chat), currentFilter(chat));
    }

    public DictionaryId dictionaryId(Chat chat) {
        var studentId = StudentSupport.selectedStudentId(chat);
        return studentQuery.getDictionaryId(studentId)
                .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentId));
    }

    public CefrLevel cefrLevel(Chat chat) {
        var studentId = StudentSupport.selectedStudentId(chat);
        return studentQuery.getCefrLevel(studentId)
                .orElseThrow(() -> new IllegalStateException("No level for student " + studentId));
    }

    public Set<WordInfoId> selectedWordIds(Chat chat) {
        return selectedIdStrings(chat).stream()
                .map(UUID::fromString)
                .map(WordInfoId::new)
                .collect(Collectors.toSet());
    }

    public List<WordProjection> selectedWords(Chat chat) {
        var selected = selectedWordIds(chat);
        if (selected.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(findWords.findByDictionaryIdAndWordIds(dictionaryId(chat), selected)).stream()
                .sorted(Comparator.comparing(WordProjection::value, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<WordProjection> sortedFilteredWords(Chat chat) {
        var words = new ArrayList<>(findWords.findByDictionaryId(dictionaryId(chat)));
        words.sort(Comparator.comparing(WordProjection::value, String.CASE_INSENSITIVE_ORDER));
        return ExerciseWordSelectionView.applyFilter(Set.copyOf(words), currentFilter(chat)).stream()
                .sorted(Comparator.comparing(WordProjection::value, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public String currentFilter(Chat chat) {
        var raw = chat.getContext().get(WORD_FILTER.getValue());
        return raw instanceof String filter ? filter : ExerciseWordSelectionView.FILTER_IN_PROGRESS;
    }

    public void toggleWord(Chat chat, String id) {
        var selection = new HashSet<>(selectedIdStrings(chat));
        if (!selection.remove(id)) {
            selection.add(id);
        }
        chat.getContext().put(WORD_SELECTION.getValue(), selection);
    }

    public void setFilter(Chat chat, String filter) {
        chat.getContext().put(WORD_FILTER.getValue(), filter);
        chat.getContext().put(SELECTION_PAGE.getValue(), 0);
    }

    public void setPage(Chat chat, int page) {
        chat.getContext().put(SELECTION_PAGE.getValue(), Math.max(0, page));
    }

    public static int currentPage(Chat chat) {
        var raw = chat.getContext().get(SELECTION_PAGE.getValue());
        return raw instanceof Integer page ? page : 0;
    }

    private static Set<String> selectedIdStrings(Chat chat) {
        var raw = chat.getContext().get(WORD_SELECTION.getValue());
        return raw instanceof Set<?> selection
                ? selection.stream().map(String.class::cast).collect(Collectors.toSet())
                : Set.of();
    }

    private static int clampPage(int page, int totalPages) {
        return Math.min(Math.max(page, 0), Math.max(totalPages - 1, 0));
    }
}
