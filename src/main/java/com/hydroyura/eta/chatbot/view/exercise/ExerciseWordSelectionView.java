package com.hydroyura.eta.chatbot.view.exercise;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.util.ViewUtils;
import com.hydroyura.eta.chatbot.view.word.WordView;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.hydroyura.eta.chatbot.view.Messages.WORD_SELECTION_EMPTY_WARNING;
import static com.hydroyura.eta.chatbot.view.Messages.WORD_SELECTION_PROMPT;
import static com.hydroyura.eta.chatbot.view.util.ViewUtils.createCallbackData;

public final class ExerciseWordSelectionView {

    public static final String FILTER_IN_PROGRESS = "IN_PROGRESS";

    public static final String FILTER_NEW = "NEW";

    public static final String FILTER_LEARNED = "LEARNED";

    public static final String FILTER_ALL = "ALL";

    private ExerciseWordSelectionView() {
    }

    public static ActionResult selection(int messageId, List<WordProjection> pageWords, int page, int totalPages,
            Set<String> selectedIds, String filter) {
        return new ActionResult.EditMessageText(messageId, WORD_SELECTION_PROMPT,
                selectionKeyboard(pageWords, page, totalPages, selectedIds, filter));
    }

    public static ActionResult selectionFresh(List<WordProjection> pageWords, int page, int totalPages,
            Set<String> selectedIds, String filter) {
        return new ActionResult.TextWithInlineKeyboard(WORD_SELECTION_PROMPT,
                selectionKeyboard(pageWords, page, totalPages, selectedIds, filter));
    }

    public static ActionResult warning(int messageId) {
        return new ActionResult.EditMessageText(messageId, WORD_SELECTION_EMPTY_WARNING, List.of(
                List.of(new ActionResult.InlineButton(Buttons.CONTINUE,
                        createCallbackData(Callbacks.EXERCISE_WORD_CONTINUE))),
                List.of(new ActionResult.InlineButton(Buttons.BACK_TO_SELECTION,
                        createCallbackData(Callbacks.EXERCISE_WORD_ALERT_BACK)))));
    }

    public static String filterFromPayload(String payload) {
        return switch (payload) {
            case Callbacks.WORD_FILTER_IN_PROGRESS -> FILTER_IN_PROGRESS;
            case Callbacks.WORD_FILTER_NEW -> FILTER_NEW;
            case Callbacks.WORD_FILTER_LEARNED -> FILTER_LEARNED;
            case Callbacks.WORD_FILTER_ALL -> FILTER_ALL;
            default -> throw new IllegalArgumentException("Unknown word filter callback payload: " + payload);
        };
    }

    public static List<WordProjection> applyFilter(Set<WordProjection> words, String filter) {
        return switch (filter) {
            case FILTER_NEW -> words.stream()
                    .filter(word -> word.status() == WordStatus.NEW)
                    .toList();
            case FILTER_IN_PROGRESS -> words.stream()
                    .filter(word -> word.status() == WordStatus.IN_PROGRESS)
                    .toList();
            case FILTER_LEARNED -> words.stream()
                    .filter(word -> word.status() == WordStatus.LEARNED)
                    .toList();
            default -> List.copyOf(words);
        };
    }

    private static List<List<ActionResult.InlineButton>> selectionKeyboard(List<WordProjection> pageWords, int page,
            int totalPages, Set<String> selectedIds, String filter) {
        var keyboard = new ArrayList<List<ActionResult.InlineButton>>();

        for (var word : pageWords) {
            var selected = selectedIds.contains(word.id().value().toString());
            var label = (selected ? "✓ " : "· ") + word.value()
                    + " (" + WordView.posLabel(word.partOfSpeech()) + ", " + WordView.statusLabel(word.status()) + ")";
            keyboard.add(List.of(new ActionResult.InlineButton(
                    ViewUtils.truncateButtonLabel(label),
                    createCallbackData(Callbacks.EXERCISE_WORD, word.id().value().toString()))));
        }

        keyboard.add(List.of(
                filterButton("🔄 In progress", FILTER_IN_PROGRESS, Callbacks.WORD_FILTER_IN_PROGRESS, filter),
                filterButton("🆕 New", FILTER_NEW, Callbacks.WORD_FILTER_NEW, filter),
                filterButton("✅ Learned", FILTER_LEARNED, Callbacks.WORD_FILTER_LEARNED, filter),
                filterButton("📋 All", FILTER_ALL, Callbacks.WORD_FILTER_ALL, filter)));

        var navigation = new ArrayList<ActionResult.InlineButton>();
        if (ViewUtils.hasPrev(page)) {
            navigation.add(new ActionResult.InlineButton(Buttons.PREV,
                    createCallbackData(Callbacks.EXERCISE_WORD_PAGE, Callbacks.PREV)));
        }
        if (ViewUtils.hasNext(page, totalPages)) {
            navigation.add(new ActionResult.InlineButton(Buttons.NEXT,
                    createCallbackData(Callbacks.EXERCISE_WORD_PAGE, Callbacks.NEXT)));
        }
        if (!navigation.isEmpty()) {
            keyboard.add(navigation);
        }

        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.DONE,
                createCallbackData(Callbacks.EXERCISE_WORD_DONE))));
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.BACK_TO_STUDENT,
                createCallbackData(Callbacks.EXERCISE_WORD_SELECTION_BACK))));

        return keyboard;
    }

    private static ActionResult.InlineButton filterButton(String label, String filter, String payload, String current) {
        var prefix = filter.equals(current) ? "● " : "○ ";
        return new ActionResult.InlineButton(prefix + label,
                createCallbackData(Callbacks.EXERCISE_WORD_FILTER, payload));
    }
}
