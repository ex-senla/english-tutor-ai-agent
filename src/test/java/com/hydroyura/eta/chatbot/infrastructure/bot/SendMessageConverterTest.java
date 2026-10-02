package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendMessageConverterTest {

    private final SendMessageConverter converter = new SendMessageConverter();

    @Test
    void shouldConvertSingleSendDocument() {
        var result = new ActionResult.SendDocument("exercise.pdf", "data".getBytes(StandardCharsets.UTF_8), "cap");

        var doc = converter.convertDocument(result, 123L);

        assertThat(doc.getChatId()).isEqualTo("123");
        assertThat(doc.getCaption()).isEqualTo("cap");
        assertThat(doc.getDocument()).isNotNull();
    }

    @Test
    void shouldConvertSingleSendDocumentViaConvert() {
        var result = new ActionResult.SendDocument("exercise.pdf", "data".getBytes(StandardCharsets.UTF_8), "cap");

        var response = converter.convert(result, 123L);

        assertThat(response).isInstanceOf(SendDocument.class);
    }

    @Test
    void shouldRejectSendDocumentsInConvert() {
        var result = new ActionResult.SendDocuments(List.of(), "menu", List.of());

        assertThatThrownBy(() -> converter.convert(result, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
