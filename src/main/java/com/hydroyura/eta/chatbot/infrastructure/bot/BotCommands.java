package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.view.Commands;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

/**
 * Публичные команды бота для Telegram-меню ({@code SetMyCommands}).
 */
public final class BotCommands {

    private BotCommands() {
    }

    public static List<BotCommand> list() {
        return List.of(
                command(Commands.START, "Начать"),
                command(Commands.REGISTER, "Зарегистрироваться"),
                command(Commands.HELP, "Помощь"),
                command(Commands.NEW, "Новый ученик"),
                command(Commands.LIST, "Список учеников"),
                command(Commands.FIND, "Найти ученика"),
                command(Commands.CANCEL, "Отменить действие"),
                command(Commands.FINISH_LESSON, "Завершить урок"));
    }

    private static BotCommand command(String command, String description) {
        var botCommand = new BotCommand();
        botCommand.setCommand(command);
        botCommand.setDescription(description);
        return botCommand;
    }
}
