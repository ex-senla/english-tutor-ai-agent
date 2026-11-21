package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.hydroyura.eta.chatbot.view.Buttons;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.view.Messages.ENTER_YOUR_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class RegistrationE2ETest extends BotE2eTestBase {

    @Test
    void shouldRegisterTeacher() {
        botTester.command("/register");

        assertThat(botTester.chatState()).isEqualTo("AWAITING_REGISTRATION_NAME");
        assertThat(botTester.messages()).hasSize(1);
        assertThat(botTester.messages().get(0).text()).isEqualTo(ENTER_YOUR_NAME);

        botTester.text("Иван");

        assertThat(botTester.chatState()).isEqualTo("ACTIVE");
        var last = botTester.lastMessage();
        assertThat(last.keyboard()).isEqualTo(KeyboardKind.REPLY);
        assertThat(last.replyButtons()).contains(Buttons.NEW_STUDENT, Buttons.LIST_STUDENT, Buttons.FIND_STUDENT);
        assertThat(botTester.count("teacher")).isEqualTo(1);
    }

    @Test
    void shouldPersistTeacherRow() {
        botTester.command("/register");
        botTester.text("Иван");

        var teachers = botTester.rows("teacher");
        assertThat(teachers).hasSize(1);
        assertThat(teachers.get(0).get("name")).isEqualTo("Иван");

        var identifiers = botTester.rows("teacher_identifier");
        assertThat(identifiers).hasSize(1);
        assertThat(identifiers.get(0).get("identifier_value")).isEqualTo(CHAT_ID.toString());
    }
}
