package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.hydroyura.eta.chatbot.view.Buttons;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StartCommandResetE2ETest extends BotE2eTestBase {

    @Test
    void shouldResetFromInLesson() {
        reachInLesson();
        botTester.reset();

        botTester.command("/start");

        assertThat(botTester.chatState()).isEqualTo("ACTIVE");
        var last = botTester.lastMessage();
        assertThat(last.keyboard()).isEqualTo(KeyboardKind.REPLY);
        assertThat(last.replyButtons()).contains(Buttons.NEW_STUDENT, Buttons.LIST_STUDENT, Buttons.FIND_STUDENT);
    }

    @Test
    void shouldContinueFromPersistedState() {
        register();
        assertThat(botTester.chatState()).isEqualTo("ACTIVE");

        // «холодный» рестарт: чистим persistence context, чтобы чат читался из PostgreSQL
        clearSession();
        botTester.reset();

        botTester.button(Buttons.NEW_STUDENT);

        assertThat(botTester.chatState()).isEqualTo("AWAITING_STUDENT_NAME");
        assertThat(botTester.count("chat")).isEqualTo(1);
        assertThat(botTester.rows("chat").get(0).get("chat_id")).isEqualTo(CHAT_ID);
    }

    private void register() {
        botTester.command("/register");
        botTester.text("Иван");
    }

    private void reachInLesson() {
        register();
        addStudentViaMainMenu("Иван", "B1");

        botTester.command("/list");
        botTester.callback("student:" + studentId());
        botTester.callback("action:startlesson");
    }

    private void addStudentViaMainMenu(String name, String levelCode) {
        botTester.button(Buttons.NEW_STUDENT);
        botTester.text(name);
        botTester.callback("level:" + levelCode);
    }

    private String studentId() {
        return botTester.rows("student").get(0).get("id").toString();
    }
}
