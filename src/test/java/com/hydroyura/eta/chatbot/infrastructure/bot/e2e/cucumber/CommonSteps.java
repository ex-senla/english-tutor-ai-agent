package com.hydroyura.eta.chatbot.infrastructure.bot.e2e.cucumber;

import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Общие шаги Cucumber-сценариев: предусловия (регистрация учителя, сидинг ученика со словарём
 * через use-case-порты), универсальные шаги навигации по диалогу и персистентные ассерты по БД.
 */
public class CommonSteps {

    @Autowired
    private CucumberScenarioState state;

    @Autowired
    private FindTeacher findTeacher;

    @Autowired
    private CreateStudentWithDictionary createStudentWithDictionary;

    @Дано("зарегистрированный учитель")
    public void зарегистрированныйУчитель() {
        var botTester = state.getBotTester();
        botTester.command("/register");
        botTester.text("Иван");
        botTester.reset();
    }

    @Дано("ученик {string} со словарём \\(уровень {string})")
    public void ученикСоСловарём(String name, String level) {
        var botTester = state.getBotTester();
        botTester.command("/register");
        botTester.text("Иван");

        var teacherId = findTeacher.findByTelegramChatId(state.getChatId()).orElseThrow();
        var studentId = createStudentWithDictionary.execute(
                new CreateStudentWithDictionaryCommand(teacherId, name, "Словарь " + name, CefrLevel.valueOf(level)))
                .value()
                .toString();
        state.setStudentId(studentId);
        state.setStudentName(name);
        state.setStudentLevel(level);
        botTester.reset();
    }

    @Когда("учитель открывает список учеников")
    public void учительОткрываетСписокУчеников() {
        state.getBotTester().command("/list");
    }

    @Когда("учитель выбирает ученика {string}")
    public void учительВыбираетУченика(String name) {
        state.getBotTester().callback("student:" + state.getStudentId());
    }

    @Когда("учитель нажимает callback {string}")
    public void учительНажимаетCallback(String data) {
        state.getBotTester().callback(data);
    }

    @Тогда("бот показывает опции ученика")
    public void ботПоказываетОпцииУченика() {
        assertThat(state.getBotTester().chatState()).isEqualTo("STUDENT_OPTIONS");
    }

    @Тогда("бот переходит в состояние {string}")
    public void ботПереходитВСостояние(String chatState) {
        assertThat(state.getBotTester().chatState()).isEqualTo(chatState);
    }

    @И("в базе ровно одна строка {string}")
    public void вБазеОднаСтрока(String table) {
        assertThat(state.getBotTester().count(table)).isEqualTo(1);
    }

    @И("в базе ровно одна строка {string} с именем {string}")
    public void вБазеСтрокаСИменем(String table, String name) {
        var botTester = state.getBotTester();
        assertThat(botTester.count(table)).isEqualTo(1);
        assertThat(botTester.rows(table).get(0).get("name")).isEqualTo(name);
    }

    @И("в базе ровно одна строка {string} с chatId сценария")
    public void вБазеСтрокаСChatId(String table) {
        var botTester = state.getBotTester();
        assertThat(botTester.count(table)).isEqualTo(1);
        assertThat(botTester.rows(table).get(0).get("identifier_value")).isEqualTo(state.getChatId().toString());
    }

    @И("в базе ровно одна строка {string} с уровнем {string}")
    public void вБазеСтрокаСУровнем(String table, String level) {
        var botTester = state.getBotTester();
        assertThat(botTester.count(table)).isEqualTo(1);
        assertThat(botTester.rows(table).get(0).get("cefr_level")).isEqualTo(level);
    }

    @И("в базе ровно одна строка {string} со статусом {string}")
    public void вБазеСтрокаСоСтатусом(String table, String status) {
        var botTester = state.getBotTester();
        assertThat(botTester.count(table)).isEqualTo(1);
        assertThat(botTester.rows(table).get(0).get("status")).isEqualTo(status);
    }

    @И("в базе ровно одна строка {string} со словом {string}")
    public void вБазеСтрокаСоСловом(String table, String word) {
        var botTester = state.getBotTester();
        assertThat(botTester.count(table)).isEqualTo(1);
        assertThat(botTester.rows(table).get(0).get("word_value")).isEqualTo(word);
    }

    @И("в базе ровно одна строка {string} с переводом {string}")
    public void вБазеСтрокаСПереводом(String table, String translation) {
        var botTester = state.getBotTester();
        assertThat(botTester.count(table)).isEqualTo(1);
        assertThat(botTester.rows(table).get(0).get("translation")).isEqualTo(translation);
    }
}
