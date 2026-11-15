package com.hydroyura.eta.chatbot.application.statemachine.transition.studentdictionary;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.students.StudentDictionaryView;
import com.hydroyura.eta.chatbot.view.util.ViewUtils;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.ArrayList;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.DICTIONARY_PAGE;

@RequiredArgsConstructor
public final class StudentDictionarySupport {

    private final StudentQuery studentQuery;

    private final FindWords findWords;

    private final ChatbotPaginationProperties paginationProperties;

    public ActionResult render(Chat chat, int messageId) {
        var words = sortedWords(chat);
        if (words.isEmpty()) {
            return StudentDictionaryView.empty(messageId);
        }

        var totalPages = ViewUtils.pageCount(words.size(), paginationProperties.getPageSize());
        var page = clampPage(currentPage(chat), totalPages);
        var pageWords = ViewUtils.pageSlice(words, page, paginationProperties.getPageSize());

        return StudentDictionaryView.dictionary(messageId, pageWords, page, totalPages);
    }

    private java.util.List<WordProjection> sortedWords(Chat chat) {
        var studentId = StudentSupport.selectedStudentId(chat);
        var dictionaryId = studentQuery.getDictionaryId(studentId)
                .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentId));

        var words = new ArrayList<>(findWords.findByDictionaryId(dictionaryId));
        words.sort(Comparator.comparing(WordProjection::value, String.CASE_INSENSITIVE_ORDER));
        return words;
    }

    private static int currentPage(Chat chat) {
        var raw = chat.getContext().get(DICTIONARY_PAGE.getValue());
        return raw instanceof Integer page ? page : 0;
    }

    private static int clampPage(int page, int totalPages) {
        return Math.min(Math.max(page, 0), Math.max(totalPages - 1, 0));
    }
}
