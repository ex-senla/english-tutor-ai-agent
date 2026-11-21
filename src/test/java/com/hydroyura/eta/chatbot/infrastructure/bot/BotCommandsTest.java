package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.view.Commands;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BotCommandsTest {

    @Test
    void shouldContainCancelAndFindButNotAddWord() {
        var commands = BotCommands.list();

        assertThat(commands).extracting("command")
                .contains(Commands.CANCEL, Commands.FIND)
                .doesNotContain("/addword");
    }

    @Test
    void shouldContainAllPublicCommands() {
        var commands = BotCommands.list();

        assertThat(commands).extracting("command").containsExactly(
                Commands.START,
                Commands.REGISTER,
                Commands.HELP,
                Commands.NEW,
                Commands.LIST,
                Commands.FIND,
                Commands.CANCEL,
                Commands.FINISH_LESSON,
                Commands.SUPPORT,
                Commands.ADMIN);
    }
}
