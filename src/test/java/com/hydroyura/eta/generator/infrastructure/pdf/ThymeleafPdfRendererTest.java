package com.hydroyura.eta.generator.infrastructure.pdf;

import com.hydroyura.eta.generator.application.view.AnswerItemView;
import com.hydroyura.eta.generator.application.view.ExerciseItemView;
import com.hydroyura.eta.generator.application.view.UsedWordView;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThymeleafPdfRendererTest {

    @Test
    void shouldRenderExerciseTemplateWithCyrillic() {
        var renderer = new ThymeleafPdfRenderer();

        var model = Map.<String, Object>of(
                "type", "FILL_IN_THE_BLANK",
                "topic", "Животные",
                "items", List.of(new ExerciseItemView("<b>travel</b> к горам ___", List.of("A) вариант", "B) вариант")))
        );

        var bytes = renderer.render("exercise", model);

        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
    }

    @Test
    void shouldRenderAnswersTemplate() {
        var renderer = new ThymeleafPdfRenderer();

        var model = Map.<String, Object>of(
                "type", "MULTIPLE_CHOICE",
                "topic", "Animals",
                "answers", List.of(new AnswerItemView(1, "travelled")),
                "usedWords", List.of(new UsedWordView("travel", "путешествовать"))
        );

        var bytes = renderer.render("answers", model);

        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
    }
}
