package com.hydroyura.eta.chatbot.view.exercise;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciseWordSelectionViewTest {

    @Test
    void shouldRenderSelectedWordWithCheckMarkAndUnselectedWithDot() {
        var selectedId = WordInfoId.generate();
        var unselectedId = WordInfoId.generate();

        var words = List.of(
                new WordProjection(selectedId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW),
                new WordProjection(unselectedId, "run", Set.of("бежать"), PartOfSpeech.VERB, WordStatus.IN_PROGRESS));

        var result = ExerciseWordSelectionView.selection(1, words, 0, 1,
                Set.of(selectedId.value().toString()), ExerciseWordSelectionView.FILTER_ALL);

        var wordButtons = wordButtons(result);
        assertThat(wordButtons).anySatisfy(button -> {
            assertThat(button.text()).startsWith("✓ apple");
            assertThat(button.callbackData()).isEqualTo(Callbacks.EXERCISE_WORD + ":" + selectedId.value());
        });
        assertThat(wordButtons).anySatisfy(button -> {
            assertThat(button.text()).startsWith("· run");
            assertThat(button.callbackData()).isEqualTo(Callbacks.EXERCISE_WORD + ":" + unselectedId.value());
        });
    }

    @Test
    void shouldMarkCurrentFilterAsSelected() {
        var result = ExerciseWordSelectionView.selection(1, List.of(), 0, 0, Set.of(),
                ExerciseWordSelectionView.FILTER_IN_PROGRESS);

        var edit = (ActionResult.EditMessageText) result;
        var filterButtons = edit.keyboard().stream()
                .flatMap(List::stream)
                .filter(button -> button.callbackData().startsWith(Callbacks.EXERCISE_WORD_FILTER))
                .toList();

        assertThat(filterButtons).anySatisfy(button -> {
            assertThat(button.callbackData()).isEqualTo(
                    Callbacks.EXERCISE_WORD_FILTER + ":" + Callbacks.WORD_FILTER_IN_PROGRESS);
            assertThat(button.text()).startsWith("● ");
        });
    }

    @Test
    void shouldKeepWordButtonLabelWithinTelegramLimit() {
        var longWord = new WordProjection(WordInfoId.generate(), "a".repeat(50), Set.of("перевод"),
                PartOfSpeech.NOUN, WordStatus.LEARNED);

        var result = ExerciseWordSelectionView.selection(1, List.of(longWord), 0, 1, Set.of(),
                ExerciseWordSelectionView.FILTER_ALL);

        var wordButton = wordButtons(result).get(0);
        assertThat(wordButton.text().getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);
    }

    @Test
    void shouldRenderNoWordButtonsForEmptyFilterButKeepActions() {
        var result = ExerciseWordSelectionView.selection(1, List.of(), 0, 0, Set.of(),
                ExerciseWordSelectionView.FILTER_ALL);

        assertThat(wordButtons(result)).isEmpty();
        assertThat(buttonTexts(result)).contains("✔️ Готово", "🔙 Назад");
    }

    @Test
    void shouldMapFilterPayloads() {
        assertThat(ExerciseWordSelectionView.filterFromPayload(Callbacks.WORD_FILTER_IN_PROGRESS))
                .isEqualTo(ExerciseWordSelectionView.FILTER_IN_PROGRESS);
        assertThat(ExerciseWordSelectionView.filterFromPayload(Callbacks.WORD_FILTER_NEW))
                .isEqualTo(ExerciseWordSelectionView.FILTER_NEW);
        assertThat(ExerciseWordSelectionView.filterFromPayload(Callbacks.WORD_FILTER_LEARNED))
                .isEqualTo(ExerciseWordSelectionView.FILTER_LEARNED);
        assertThat(ExerciseWordSelectionView.filterFromPayload(Callbacks.WORD_FILTER_ALL))
                .isEqualTo(ExerciseWordSelectionView.FILTER_ALL);
    }

    private static List<ActionResult.InlineButton> wordButtons(ActionResult result) {
        var edit = (ActionResult.EditMessageText) result;
        return edit.keyboard().stream()
                .flatMap(List::stream)
                .filter(button -> button.callbackData().startsWith(Callbacks.EXERCISE_WORD + ":"))
                .toList();
    }

    private static List<String> buttonTexts(ActionResult result) {
        var edit = (ActionResult.EditMessageText) result;
        return edit.keyboard().stream()
                .flatMap(List::stream)
                .map(ActionResult.InlineButton::text)
                .toList();
    }
}
