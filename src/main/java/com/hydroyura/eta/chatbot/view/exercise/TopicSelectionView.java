package com.hydroyura.eta.chatbot.view.exercise;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.util.ViewUtils;
import com.hydroyura.eta.exercise.api.exercise.TopicView;
import java.util.ArrayList;
import java.util.List;

import static com.hydroyura.eta.chatbot.view.Messages.TOPIC_CONFIRM;
import static com.hydroyura.eta.chatbot.view.Messages.TOPIC_INVALID;
import static com.hydroyura.eta.chatbot.view.Messages.TOPIC_SEARCH;
import static com.hydroyura.eta.chatbot.view.Messages.TOPIC_SEARCH_RESULTS;
import static com.hydroyura.eta.chatbot.view.Messages.TOPIC_SELF_INPUT;
import static com.hydroyura.eta.chatbot.view.util.ViewUtils.createCallbackData;

/**
 * Рендер экранов выбора грамматической темы (дерево/поиск/своя/подтверждение).
 */
public final class TopicSelectionView {

    private TopicSelectionView() {
    }

    public record TopicButton(String slug, String label) {
    }

    public static ActionResult tree(int messageId, String title, List<TopicView> nodes, int page, int totalPages,
            String currentSlug, String parentSlug) {
        return new ActionResult.EditMessageText(messageId, title, treeKeyboard(nodes, page, totalPages, currentSlug,
                parentSlug));
    }

    public static ActionResult searchResults(int messageId, List<TopicButton> results, int page, int totalPages,
            String backSlug) {
        return new ActionResult.EditMessageText(messageId, TOPIC_SEARCH_RESULTS,
                searchKeyboard(results, page, totalPages, backSlug));
    }

    public static ActionResult searchResultsFresh(List<TopicButton> results, int page, int totalPages,
            String backSlug) {
        return new ActionResult.TextWithInlineKeyboard(TOPIC_SEARCH_RESULTS,
                searchKeyboard(results, page, totalPages, backSlug));
    }

    public static ActionResult searchPrompt(int messageId, String backSlug) {
        return new ActionResult.EditMessageText(messageId, TOPIC_SEARCH,
                List.of(List.of(backButton(backSlug))));
    }

    public static ActionResult searchNothingFound(int messageId, String backSlug) {
        return new ActionResult.EditMessageText(messageId, com.hydroyura.eta.chatbot.view.Messages.SEARCH_NOTHING_FOUND,
                List.of(List.of(backButton(backSlug))));
    }

    public static ActionResult searchNothingFoundFresh(String backSlug) {
        return new ActionResult.TextWithInlineKeyboard(com.hydroyura.eta.chatbot.view.Messages.SEARCH_NOTHING_FOUND,
                List.of(List.of(backButton(backSlug))));
    }

    public static ActionResult selfInput(int messageId) {
        return new ActionResult.EditMessageText(messageId, TOPIC_SELF_INPUT, List.of(List.of(
                new ActionResult.InlineButton(Buttons.TOPIC_CANCEL,
                        createCallbackData(Callbacks.TOPIC_CONFIRM, Callbacks.TOPIC_CONFIRM_BACK)))));
    }

    public static ActionResult selfInputFresh() {
        return new ActionResult.TextWithInlineKeyboard(TOPIC_SELF_INPUT, List.of(List.of(
                new ActionResult.InlineButton(Buttons.TOPIC_CANCEL,
                        createCallbackData(Callbacks.TOPIC_CONFIRM, Callbacks.TOPIC_CONFIRM_BACK)))));
    }

    public static ActionResult customPrompt(String message) {
        return new ActionResult.TextWithInlineKeyboard(message, List.of(List.of(
                new ActionResult.InlineButton(Buttons.TOPIC_CANCEL,
                        createCallbackData(Callbacks.TOPIC_CONFIRM, Callbacks.TOPIC_CONFIRM_BACK)))));
    }

    public static ActionResult thematicPromptFresh() {
        return thematicPrompt(com.hydroyura.eta.chatbot.view.Messages.THEMATIC_TOPIC_PROMPT);
    }

    public static ActionResult thematicPrompt(String message) {
        return new ActionResult.TextWithInlineKeyboard(message, List.of(List.of(
                new ActionResult.InlineButton(Buttons.TOPIC_SKIP, createCallbackData(Callbacks.TOPIC_SKIP)))));
    }

    public static ActionResult confirm(String canonicalName, boolean hasMatched, String matchedSlug) {
        var keyboard = new ArrayList<List<ActionResult.InlineButton>>();
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.TOPIC_CONFIRM_USE,
                createCallbackData(Callbacks.TOPIC_CONFIRM, Callbacks.TOPIC_CONFIRM_USE))));
        if (hasMatched) {
            keyboard.add(List.of(new ActionResult.InlineButton(Buttons.TOPIC_CONFIRM_CATALOG,
                    createCallbackData(Callbacks.TOPIC_CATALOG, matchedSlug))));
        }
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.TOPIC_CONFIRM_REENTER,
                createCallbackData(Callbacks.TOPIC_CONFIRM, Callbacks.TOPIC_CONFIRM_REENTER))));
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.TOPIC_BACK,
                createCallbackData(Callbacks.TOPIC_CONFIRM, Callbacks.TOPIC_CONFIRM_BACK))));
        return new ActionResult.TextWithInlineKeyboard(TOPIC_CONFIRM.formatted(canonicalName), keyboard);
    }

    public static ActionResult invalid(String reason) {
        var keyboard = List.of(
                List.of(new ActionResult.InlineButton(Buttons.TOPIC_CONFIRM_REENTER,
                        createCallbackData(Callbacks.TOPIC_CONFIRM, Callbacks.TOPIC_CONFIRM_REENTER))),
                List.of(new ActionResult.InlineButton(Buttons.TOPIC_BACK,
                        createCallbackData(Callbacks.TOPIC_CONFIRM, Callbacks.TOPIC_CONFIRM_BACK)))
        );
        return new ActionResult.TextWithInlineKeyboard(TOPIC_INVALID.formatted(reason), keyboard);
    }

    private static List<List<ActionResult.InlineButton>> treeKeyboard(List<TopicView> nodes, int page, int totalPages,
            String currentSlug, String parentSlug) {
        var keyboard = new ArrayList<List<ActionResult.InlineButton>>();

        keyboard.add(List.of(
                new ActionResult.InlineButton(Buttons.SEARCH, createCallbackData(Callbacks.TOPIC_SEARCH)),
                new ActionResult.InlineButton(Buttons.SELF_TOPIC, createCallbackData(Callbacks.TOPIC_SELF))));

        if (currentSlug != null) {
            keyboard.add(List.of(new ActionResult.InlineButton(Buttons.TOPIC_CHOOSE,
                    createCallbackData(Callbacks.TOPIC, currentSlug))));
        }

        for (var node : nodes) {
            var prefix = node.hasActiveChildren() ? Callbacks.TOPIC_NAV : Callbacks.TOPIC;
            keyboard.add(List.of(new ActionResult.InlineButton(
                    ViewUtils.truncateButtonLabel(node.name()),
                    createCallbackData(prefix, node.slug()))));
        }

        if (parentSlug != null) {
            keyboard.add(List.of(new ActionResult.InlineButton(Buttons.TOPIC_BACK,
                    createCallbackData(Callbacks.TOPIC_NAV, parentSlug))));
        }

        addPagination(keyboard, page, totalPages);
        return keyboard;
    }

    private static List<List<ActionResult.InlineButton>> searchKeyboard(List<TopicButton> results, int page,
            int totalPages, String backSlug) {
        var keyboard = new ArrayList<List<ActionResult.InlineButton>>();
        for (var result : results) {
            keyboard.add(List.of(new ActionResult.InlineButton(
                    ViewUtils.truncateButtonLabel(result.label()),
                    createCallbackData(Callbacks.TOPIC, result.slug()))));
        }
        keyboard.add(List.of(backButton(backSlug)));
        addPagination(keyboard, page, totalPages);
        return keyboard;
    }

    private static void addPagination(List<List<ActionResult.InlineButton>> keyboard, int page, int totalPages) {
        var navigation = new ArrayList<ActionResult.InlineButton>();
        if (ViewUtils.hasPrev(page)) {
            navigation.add(new ActionResult.InlineButton(Buttons.PREV,
                    createCallbackData(Callbacks.TOPIC_PAGE, Callbacks.PREV)));
        }
        if (ViewUtils.hasNext(page, totalPages)) {
            navigation.add(new ActionResult.InlineButton(Buttons.NEXT,
                    createCallbackData(Callbacks.TOPIC_PAGE, Callbacks.NEXT)));
        }
        if (!navigation.isEmpty()) {
            keyboard.add(navigation);
        }
    }

    private static ActionResult.InlineButton backButton(String backSlug) {
        return new ActionResult.InlineButton(Buttons.TOPIC_BACK,
                createCallbackData(Callbacks.TOPIC_NAV, backSlug == null ? "" : backSlug));
    }
}
