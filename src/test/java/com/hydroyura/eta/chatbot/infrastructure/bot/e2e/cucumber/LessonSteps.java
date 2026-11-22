package com.hydroyura.eta.chatbot.infrastructure.bot.e2e.cucumber;

import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.BotButton;
import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.KeyboardKind;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordPosTranslations;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Шаги комбинированного сценария урока и добавления слова (AC-3 + AC-4), трассируемого на
 * {@code e2e-bot-testing / E2E-сценарий урока со словом / Старт урока и Добавление слова через
 * one-shot LLM-подсказку}.
 */
public class LessonSteps {

    @Autowired
    private CucumberScenarioState state;

    @Autowired
    private WordTranslator wordTranslator;

    @И("переводчик предлагает для слова {string} часть речи {string} и переводы {string}")
    public void переводчикПредлагает(String word, String partOfSpeech, String translations) {
        when(wordTranslator.suggestPosAndTranslations(word))
                .thenReturn(new WordPosTranslations(PartOfSpeech.valueOf(partOfSpeech),
                        List.of(translations.split(",\\s*"))));
    }

    @Тогда("бот начинает урок и переходит в состояние {string}")
    public void ботНачинаетУрок(String chatState) {
        var botTester = state.getBotTester();
        assertThat(botTester.chatState()).isEqualTo(chatState);
        var lessonMessage = botTester.lastMessage();
        assertThat(lessonMessage.keyboard()).isEqualTo(KeyboardKind.REPLY);
        assertThat(lessonMessage.replyButtons()).contains(Buttons.FINISH_LESSON);
    }

    @И("в контексте чата есть {string}")
    public void вКонтекстеЧатаЕсть(String key) {
        assertThat(state.getBotTester().chatContext()).containsKey(key);
    }

    @Когда("учитель отправляет слово {string}")
    public void учительОтправляетСлово(String word) {
        state.getBotTester().text(word);
    }

    @Тогда("бот показывает варианты перевода для слова {string} и переходит в состояние {string}")
    public void ботПоказываетВариантыПеревода(String word, String chatState) {
        var botTester = state.getBotTester();
        assertThat(botTester.chatState()).isEqualTo(chatState);
        var confirmation = botTester.lastMessage();
        assertThat(confirmation.keyboard()).isEqualTo(KeyboardKind.INLINE);
        assertThat(confirmation.text()).contains(word);
        assertThat(flattenCallbacks(confirmation.inlineButtons())).contains("translation:0", "translation:1");
        verify(wordTranslator).suggestPosAndTranslations(word);
    }

    @Когда("учитель выбирает перевод {string}")
    public void учительВыбираетПеревод(String translation) {
        state.getBotTester().callback(translation);
    }

    @Тогда("бот возвращается в урок и переходит в состояние {string}")
    public void ботВозвращаетсяВУрок(String chatState) {
        assertThat(state.getBotTester().chatState()).isEqualTo(chatState);
    }

    private static List<String> flattenCallbacks(List<List<BotButton>> buttons) {
        return buttons.stream()
                .flatMap(List::stream)
                .map(BotButton::callbackData)
                .toList();
    }
}
