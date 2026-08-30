package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.item.Buttons;
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
            var callbackQuery = update.getCallbackQuery();
            var data = callbackQuery.getData();
            var messageId = callbackQuery.getMessage().getMessageId();
            // "student:<uuid>" -> prefix="student", payload="<uuid>"
            var idx = data.indexOf(':');
            var prefix = idx < 0 ? data : data.substring(0, idx);
            var payload = idx < 0 ? "" : data.substring(idx + 1);
            return new Action.Callback(prefix, payload, messageId);
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

            var text = update.getMessage().getText();

            if (isCommand) {
                return new Action.Command(text);
            }

            if (Buttons.getAllValues().contains(text)) {
                return new Action.Button(text);
            }

            return new Action.Input(text);
        }

        throw new RuntimeException("Can't parse Update object");
    }

}
