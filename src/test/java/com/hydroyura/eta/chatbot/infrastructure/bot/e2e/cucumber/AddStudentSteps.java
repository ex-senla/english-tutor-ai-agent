package com.hydroyura.eta.chatbot.infrastructure.bot.e2e.cucumber;

import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.BotButton;
import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.KeyboardKind;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import static com.hydroyura.eta.chatbot.view.Messages.ENTER_NEW_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.view.Messages.STUDENT_ADDED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Шаги сценария добавления ученика из главного меню (AC-2), трассируемого на
 * {@code e2e-bot-testing / E2E-сценарий добавления ученика / Добавление из главного меню}.
 */
public class AddStudentSteps {

    @Autowired
    private CucumberScenarioState state;

    @Когда("учитель нажимает кнопку {string}")
    public void учительНажимаетКнопку(String buttonText) {
        state.getBotTester().button(buttonText);
    }

    @Когда("учитель вводит имя ученика {string}")
    public void учительВводитИмяУченика(String name) {
        state.getBotTester().text(name);
    }

    @Когда("учитель выбирает уровень {string}")
    public void учительВыбираетУровень(String levelCode) {
        state.getBotTester().callback("level:" + levelCode);
    }

    @Тогда("бот просит ввести имя ученика и переходит в состояние {string}")
    public void ботПроситВвестиИмяУченика(String chatState) {
        var botTester = state.getBotTester();
        assertThat(botTester.chatState()).isEqualTo(chatState);
        assertThat(botTester.lastMessage().text()).isEqualTo(ENTER_NEW_STUDENT_NAME);
    }

    @Тогда("бот показывает выбор уровня CEFR и переходит в состояние {string}")
    public void ботПоказываетВыборУровня(String chatState) {
        var botTester = state.getBotTester();
        assertThat(botTester.chatState()).isEqualTo(chatState);
        var levelMessage = botTester.lastMessage();
        assertThat(levelMessage.keyboard()).isEqualTo(KeyboardKind.INLINE);
        assertThat(flattenCallbacks(levelMessage.inlineButtons()))
                .containsExactly("level:A1", "level:A2", "level:B1", "level:B2", "level:C1", "level:C2");
    }

    @Тогда("бот подтверждает добавление ученика {string}")
    public void ботПодтверждаетДобавление(String name) {
        var botTester = state.getBotTester();
        assertThat(botTester.chatState()).isEqualTo("ACTIVE");
        var added = botTester.lastMessage();
        assertThat(added.keyboard()).isEqualTo(KeyboardKind.REPLY);
        assertThat(added.text()).contains(STUDENT_ADDED.formatted(name));
    }

    private static List<String> flattenCallbacks(List<List<BotButton>> buttons) {
        return buttons.stream()
                .flatMap(List::stream)
                .map(BotButton::callbackData)
                .toList();
    }
}
