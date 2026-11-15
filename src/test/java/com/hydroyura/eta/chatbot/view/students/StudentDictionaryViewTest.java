package com.hydroyura.eta.chatbot.view.students;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudentDictionaryViewTest {

    @Test
    void shouldRenderResetButtonOnlyForLearnedWords() {
        var learnedId = WordInfoId.generate();
        var newId = WordInfoId.generate();
        var pageWords = List.of(
                new WordProjection(learnedId, "big", java.util.Set.of("большой"), PartOfSpeech.ADJECTIVE,
                        WordStatus.LEARNED),
                new WordProjection(newId, "apple", java.util.Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW));

        var result = StudentDictionaryView.dictionary(1, pageWords, 0, 1);

        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        var keyboard = ((ActionResult.EditMessageText) result).keyboard();
        var resetButtons = keyboard.stream()
                .flatMap(List::stream)
                .filter(button -> button.text().startsWith("↺"))
                .toList();

        assertThat(resetButtons).hasSize(1);
        assertThat(resetButtons.get(0).callbackData()).isEqualTo(Callbacks.WORD_RESET + ":" + learnedId.value());
    }

    @Test
    void shouldKeepResetButtonLabelWithinTelegramLimit() {
        var word = new WordProjection(WordInfoId.generate(), "a".repeat(50), java.util.Set.of("перевод"),
                PartOfSpeech.NOUN, WordStatus.LEARNED);

        var result = StudentDictionaryView.dictionary(1, List.of(word), 0, 1);
        var keyboard = ((ActionResult.EditMessageText) result).keyboard();
        var resetButton = keyboard.stream().flatMap(List::stream)
                .filter(button -> button.text().startsWith("↺"))
                .findFirst()
                .orElseThrow();

        assertThat(resetButton.text().getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);
    }

    @Test
    void shouldRenderNavigationWithBoundaries() {
        var word = new WordProjection(WordInfoId.generate(), "apple", java.util.Set.of("яблоко"),
                PartOfSpeech.NOUN, WordStatus.NEW);

        var firstPage = StudentDictionaryView.dictionary(1, List.of(word), 0, 3);
        assertThat(buttonTexts(firstPage)).contains("▶").doesNotContain("◀");

        var lastPage = StudentDictionaryView.dictionary(1, List.of(word), 2, 3);
        assertThat(buttonTexts(lastPage)).contains("◀").doesNotContain("▶");
    }

    @Test
    void shouldRenderEmptyDictionaryWithoutPagination() {
        var result = StudentDictionaryView.empty(1);

        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        var edit = (ActionResult.EditMessageText) result;
        assertThat(buttonTexts(edit)).containsExactly("🔙 Назад");
    }

    private static List<String> buttonTexts(ActionResult result) {
        var edit = (ActionResult.EditMessageText) result;
        return edit.keyboard().stream()
                .flatMap(List::stream)
                .map(ActionResult.InlineButton::text)
                .toList();
    }
}
