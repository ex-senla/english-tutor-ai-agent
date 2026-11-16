package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection.ExerciseWordSelectionSupport;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.exercise.ExerciseWordSelectionView;
import com.hydroyura.eta.chatbot.view.exercise.TopicSelectionView;
import com.hydroyura.eta.chatbot.view.util.ViewUtils;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseTopics;
import com.hydroyura.eta.exercise.api.exercise.TopicView;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_CANONICAL_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_MATCHED_SLUG;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_NODE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_SEARCH_QUERY;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;
import static com.hydroyura.eta.chatbot.view.Messages.TOPIC_SELECTION;

/**
 * Общая логика флоу выбора грамматической темы: дерево/поиск/своя тема и вход в выбор слов.
 */
@RequiredArgsConstructor
public final class TopicFlowSupport {

    public static final int MAX_TOPIC_LENGTH = 100;

    private final StudentQuery studentQuery;

    private final FindWords findWords;

    private final ExerciseTopics exerciseTopics;

    private final ChatbotPaginationProperties paginationProperties;

    public ActionResult enterTopicSelection(Chat chat, int messageId) {
        clearTopicNavigation(chat);
        chat.getContext().put(TOPIC_PAGE.getValue(), 0);
        return renderTree(chat, messageId);
    }

    public ActionResult renderTree(Chat chat, int messageId) {
        var current = currentTopicNode(chat);
        var nodes = current == null ? exerciseTopics.roots() : exerciseTopics.children(current);
        var totalPages = ViewUtils.pageCount(nodes.size(), paginationProperties.getPageSize());
        var page = clampPage(currentPage(chat), totalPages);
        var slice = ViewUtils.pageSlice(nodes, page, paginationProperties.getPageSize());

        var parentSlug = current == null ? null
                : exerciseTopics.parent(current).map(TopicView::slug).orElse(null);
        var title = current == null ? TOPIC_SELECTION : exerciseTopics.path(current) + " — выберите подтему";

        return TopicSelectionView.tree(messageId, title, slice, page, totalPages, current, parentSlug);
    }

    public ActionResult navigate(Chat chat, String slug, int messageId) {
        chat.getContext().remove(TOPIC_SEARCH_QUERY.getValue());
        if (slug == null || slug.isBlank()) {
            chat.getContext().remove(TOPIC_NODE.getValue());
        } else {
            chat.getContext().put(TOPIC_NODE.getValue(), slug);
        }
        chat.getContext().put(TOPIC_PAGE.getValue(), 0);
        return renderTree(chat, messageId);
    }

    public ActionResult page(Chat chat, String direction, int messageId) {
        var query = currentSearchQuery(chat);
        var current = currentTopicNode(chat);
        var nodes = query != null ? exerciseTopics.search(query)
                : (current == null ? exerciseTopics.roots() : exerciseTopics.children(current));
        var totalPages = ViewUtils.pageCount(nodes.size(), paginationProperties.getPageSize());
        var page = "next".equals(direction) ? currentPage(chat) + 1 : currentPage(chat) - 1;
        chat.getContext().put(TOPIC_PAGE.getValue(), clampPage(page, totalPages));
        return render(chat, messageId);
    }

    public ActionResult render(Chat chat, int messageId) {
        var query = currentSearchQuery(chat);
        if (query != null) {
            return renderSearch(chat, messageId, query, false);
        }
        return renderTree(chat, messageId);
    }

    public ActionResult search(Chat chat, String query) {
        chat.getContext().put(TOPIC_SEARCH_QUERY.getValue(), query);
        chat.getContext().put(TOPIC_PAGE.getValue(), 0);
        return renderSearch(chat, 0, query, true);
    }

    public ActionResult searchPrompt(Chat chat, int messageId) {
        return TopicSelectionView.searchPrompt(messageId, currentTopicNode(chat));
    }

    public ActionResult enterSelf(Chat chat, int messageId) {
        chat.updateState(ChatState.AWAITING_CUSTOM_TOPIC);
        return TopicSelectionView.selfInput(messageId);
    }

    public ActionResult backToTopicSelection(Chat chat, int messageId) {
        clearTopicNavigation(chat);
        chat.getContext().put(TOPIC_PAGE.getValue(), 0);
        chat.updateState(ChatState.AWAITING_EXERCISE_TOPIC);
        return renderTree(chat, messageId);
    }

    public ActionResult selectCatalogTopic(Chat chat, String slug) {
        return enterThematicTopicStep(chat, new CatalogTopic(slug));
    }

    public ActionResult enterThematicTopicStep(Chat chat, ExerciseTopic topic) {
        chat.getContext().put(EXERCISE_TOPIC.getValue(), topic);
        clearTopicNavigation(chat);
        chat.updateState(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);
        return TopicSelectionView.thematicPromptFresh();
    }

    public ActionResult enterWordSelection(Chat chat) {
        chat.getContext().put(WORD_FILTER.getValue(), ExerciseWordSelectionView.FILTER_IN_PROGRESS);
        chat.getContext().put(SELECTION_PAGE.getValue(), 0);
        clearTopicNavigation(chat);

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

    private ActionResult renderSearch(Chat chat, int messageId, String query, boolean fresh) {
        var results = exerciseTopics.search(query);
        var backSlug = currentTopicNode(chat);
        if (results.isEmpty()) {
            return fresh
                    ? TopicSelectionView.searchNothingFoundFresh(backSlug)
                    : TopicSelectionView.searchNothingFound(messageId, backSlug);
        }

        var totalPages = ViewUtils.pageCount(results.size(), paginationProperties.getPageSize());
        var page = clampPage(currentPage(chat), totalPages);
        var slice = ViewUtils.pageSlice(results, page, paginationProperties.getPageSize());
        var buttons = slice.stream()
                .map(topic -> new TopicSelectionView.TopicButton(topic.slug(), exerciseTopics.path(topic.slug())))
                .toList();

        if (fresh) {
            return TopicSelectionView.searchResultsFresh(buttons, page, totalPages, backSlug);
        }
        return TopicSelectionView.searchResults(messageId, buttons, page, totalPages, backSlug);
    }

    private void clearTopicNavigation(Chat chat) {
        chat.getContext().remove(TOPIC_SEARCH_QUERY.getValue());
        chat.getContext().remove(TOPIC_NODE.getValue());
        chat.getContext().remove(TOPIC_PAGE.getValue());
        chat.getContext().remove(TOPIC_CANONICAL_NAME.getValue());
        chat.getContext().remove(TOPIC_MATCHED_SLUG.getValue());
    }

    private static String currentTopicNode(Chat chat) {
        var raw = chat.getContext().get(TOPIC_NODE.getValue());
        return raw instanceof String slug ? slug : null;
    }

    private static String currentSearchQuery(Chat chat) {
        var raw = chat.getContext().get(TOPIC_SEARCH_QUERY.getValue());
        return raw instanceof String query ? query : null;
    }

    private static int currentPage(Chat chat) {
        var raw = chat.getContext().get(TOPIC_PAGE.getValue());
        return raw instanceof Integer page ? page : 0;
    }

    private static int clampPage(int page, int totalPages) {
        return Math.min(Math.max(page, 0), Math.max(totalPages - 1, 0));
    }
}
