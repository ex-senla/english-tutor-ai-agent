package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.domain.action.Action;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;

@RequiredArgsConstructor
@Slf4j
@Component
public final class UpdateParser {


    public Action parseUpdate(Update update) {

        // check if callBack
        if (update.hasCallbackQuery()) {
            return new Action.Callback(update.getCallbackQuery().getData());
        }

        // check if command or input param
        if (update.hasMessage()) {
            var isCommand = Optional.ofNullable(update.getMessage().getEntities())
                    .stream()
                    .flatMap(List::stream)
                    .findFirst()
                    .map(MessageEntity::getType)
                    .map("BOT_COMMAND"::equalsIgnoreCase)
                    .orElse(FALSE);

            if (isCommand) {
                var command = update.getMessage().getText();
                var userName = update.getMessage().getFrom().getFirstName();
                return new Action.Command(command, userName);
            } else {
                var text = update.getMessage().getText();
                return new Action.InputParam(text);
            }
        }

        throw new RuntimeException("Can't parse Update object");
    }

}
