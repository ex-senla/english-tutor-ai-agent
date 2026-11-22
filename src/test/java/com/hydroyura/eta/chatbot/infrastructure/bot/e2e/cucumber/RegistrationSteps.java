package com.hydroyura.eta.chatbot.infrastructure.bot.e2e.cucumber;

import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.KeyboardKind;
import com.hydroyura.eta.chatbot.view.Buttons;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import org.springframework.beans.factory.annotation.Autowired;

import static com.hydroyura.eta.chatbot.view.Messages.ENTER_YOUR_NAME;
import static com.hydroyura.eta.chatbot.view.Messages.ONBOARDING_REVIEW;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Шаги сценария регистрации учителя (AC-1), трассируемого на
 * {@code e2e-bot-testing / E2E-сценарий регистрации / Регистрация учителя}.
 */
public class RegistrationSteps {

    @Autowired
    private CucumberScenarioState state;

    @Дано("чистый чат")
    public void чистыйЧат() {
        // БД уже очищена хуком @Before перед сценарием
    }

    @Когда("учитель отправляет команду {string}")
    public void учительОтправляетКоманду(String command) {
        state.getBotTester().command(command);
    }

    @Когда("учитель отправляет имя {string}")
    public void учительОтправляетИмя(String name) {
        state.getBotTester().text(name);
    }

    @Тогда("бот просит ввести имя и переходит в состояние {string}")
    public void ботПроситВвестиИмя(String chatState) {
        var botTester = state.getBotTester();
        assertThat(botTester.chatState()).isEqualTo(chatState);
        assertThat(botTester.messages()).hasSize(1);
        assertThat(botTester.messages().get(0).text()).isEqualTo(ENTER_YOUR_NAME);
    }

    @Тогда("бот показывает главное меню и переходит в состояние {string}")
    public void ботПоказываетГлавноеМеню(String chatState) {
        var botTester = state.getBotTester();
        assertThat(botTester.chatState()).isEqualTo(chatState);
        var last = botTester.lastMessage();
        assertThat(last.keyboard()).isEqualTo(KeyboardKind.REPLY);
        assertThat(last.replyButtons()).contains(Buttons.NEW_STUDENT, Buttons.LIST_STUDENT, Buttons.FIND_STUDENT);
    }

    @И("бот показывает онбординг-обзор")
    public void ботПоказываетОнбордингОбзор() {
        var last = state.getBotTester().lastMessage();
        assertThat(last.text()).contains(ONBOARDING_REVIEW);
    }

    @И("в базе строка {string} имеет onboarding_completed = true")
    public void вБазеСтрокаИмеетОнбордингЗавершён(String table) {
        var botTester = state.getBotTester();
        assertThat(botTester.rows(table)).hasSize(1);
        assertThat(botTester.rows(table).get(0).get("onboarding_completed")).isEqualTo(true);
    }
}
