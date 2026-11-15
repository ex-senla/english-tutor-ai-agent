package com.hydroyura.eta.chatbot.view.students;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.util.ViewUtils;
import com.hydroyura.eta.chatbot.view.word.WordView;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.hydroyura.eta.chatbot.view.Messages.DICTIONARY_EMPTY;
import static com.hydroyura.eta.chatbot.view.util.ViewUtils.createCallbackData;

public final class StudentDictionaryView {

    private StudentDictionaryView() {
    }

    public static ActionResult dictionary(int messageId, List<WordProjection> pageWords, int page, int totalPages) {
        var text = pageWords.stream()
                .map(StudentDictionaryView::formatWord)
                .collect(Collectors.joining("\n"));

        var keyboard = new ArrayList<List<ActionResult.InlineButton>>();
        for (var word : pageWords) {
            if (word.status() == WordStatus.LEARNED) {
                keyboard.add(List.of(new ActionResult.InlineButton(
                        ViewUtils.truncateButtonLabel(Buttons.RESET + " " + word.value()),
                        createCallbackData(Callbacks.WORD_RESET, word.id().value().toString()))));
            }
        }

        var navigation = new ArrayList<ActionResult.InlineButton>();
        if (ViewUtils.hasPrev(page)) {
            navigation.add(new ActionResult.InlineButton(Buttons.PREV,
                    createCallbackData(Callbacks.WORD_DICTIONARY_PAGE, Callbacks.PREV)));
        }
        if (ViewUtils.hasNext(page, totalPages)) {
            navigation.add(new ActionResult.InlineButton(Buttons.NEXT,
                    createCallbackData(Callbacks.WORD_DICTIONARY_PAGE, Callbacks.NEXT)));
        }
        if (!navigation.isEmpty()) {
            keyboard.add(navigation);
        }

        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.BACK_TO_STUDENT,
                createCallbackData(Callbacks.WORD_DICTIONARY_BACK))));

        return new ActionResult.EditMessageText(messageId, text, keyboard);
    }

    public static ActionResult empty(int messageId) {
        return new ActionResult.EditMessageText(messageId, DICTIONARY_EMPTY, List.of(
                List.of(new ActionResult.InlineButton(Buttons.BACK_TO_STUDENT,
                        createCallbackData(Callbacks.WORD_DICTIONARY_BACK)))));
    }

    private static String formatWord(WordProjection word) {
        return "%s — %s (%s, %s)".formatted(
                word.value(),
                String.join(", ", word.translations()),
                WordView.posLabel(word.partOfSpeech()),
                WordView.statusLabel(word.status()));
    }
}
