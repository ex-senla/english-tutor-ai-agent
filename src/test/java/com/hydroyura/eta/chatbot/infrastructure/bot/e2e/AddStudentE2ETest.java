package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.hydroyura.eta.chatbot.view.Buttons;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.view.Messages.ENTER_NEW_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.view.Messages.STUDENT_ADDED;
import static org.assertj.core.api.Assertions.assertThat;

class AddStudentE2ETest extends BotE2eTestBase {

    @Test
    void shouldAddStudentFromMainMenu() {
        register();
        botTester.reset();

        botTester.button(Buttons.NEW_STUDENT);
        assertThat(botTester.chatState()).isEqualTo("AWAITING_STUDENT_NAME");
        assertThat(botTester.lastMessage().text()).isEqualTo(ENTER_NEW_STUDENT_NAME);

        botTester.text("Пётр");
        assertThat(botTester.chatState()).isEqualTo("AWAITING_STUDENT_LEVEL");
        var levelMessage = botTester.lastMessage();
        assertThat(levelMessage.keyboard()).isEqualTo(KeyboardKind.INLINE);
        assertThat(flattenCallbacks(levelMessage.inlineButtons()))
                .containsExactly("level:A1", "level:A2", "level:B1", "level:B2", "level:C1", "level:C2");

        botTester.callback("level:B1");
        assertThat(botTester.chatState()).isEqualTo("ACTIVE");
        var added = botTester.lastMessage();
        assertThat(added.keyboard()).isEqualTo(KeyboardKind.REPLY);
        assertThat(added.text()).contains(STUDENT_ADDED.formatted("Пётр"));
        assertThat(botTester.count("student")).isEqualTo(1);
        assertThat(botTester.count("dictionary")).isEqualTo(1);
        assertThat(botTester.count("teacher_student")).isEqualTo(1);
        assertThat(botTester.rows("student").get(0).get("cefr_level")).isEqualTo("B1");
    }

    @Test
    void shouldAddStudentFromStudentsList() {
        register();
        addStudentViaMainMenu("Первый", "A1");
        botTester.reset();

        botTester.command("/list");
        assertThat(botTester.chatState()).isEqualTo("STUDENTS_LIST");
        assertThat(flattenCallbacks(botTester.lastMessage().inlineButtons())).contains("newstudent");

        botTester.callback("newstudent");
        assertThat(botTester.chatState()).isEqualTo("AWAITING_STUDENT_NAME");
        assertThat(botTester.lastMessage().text()).isEqualTo(ENTER_NEW_STUDENT_NAME);

        botTester.text("Пётр");
        assertThat(botTester.chatState()).isEqualTo("AWAITING_STUDENT_LEVEL");

        botTester.callback("level:B1");
        assertThat(botTester.chatState()).isEqualTo("ACTIVE");
        assertThat(botTester.count("student")).isEqualTo(2);
        assertThat(botTester.count("dictionary")).isEqualTo(2);
        assertThat(botTester.count("teacher_student")).isEqualTo(2);
    }

    private void register() {
        botTester.command("/register");
        botTester.text("Иван");
    }

    private void addStudentViaMainMenu(String name, String levelCode) {
        botTester.button(Buttons.NEW_STUDENT);
        botTester.text(name);
        botTester.callback("level:" + levelCode);
    }

    private static List<String> flattenCallbacks(List<List<BotButton>> buttons) {
        return buttons.stream()
                .flatMap(List::stream)
                .map(BotButton::callbackData)
                .toList();
    }
}
