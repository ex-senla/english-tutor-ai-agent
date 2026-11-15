package com.hydroyura.eta.chatbot.application.statemachine.transition.cancel;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.view.Messages.CANCELLED;

@RequiredArgsConstructor
public class CancelCmdTransition implements Transition<Action.Command> {

    private final CancelRule rule;

    private final CancelScreenRenderer renderer;

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        rule.keysToRemove().forEach(key -> chat.getContext().remove(key.getValue()));
        chat.updateState(rule.target());
        return withCancelledPrefix(renderer.render(chat));
    }

    private ActionResult withCancelledPrefix(ActionResult screen) {
        return switch (screen) {
            case ActionResult.TextResponse(var text) -> new ActionResult.TextResponse(prefix(text));
            case ActionResult.TextWithReplyKeyboard(var text, var keyboard, var cleanupMessageId) ->
                    new ActionResult.TextWithReplyKeyboard(prefix(text), keyboard, cleanupMessageId);
            case ActionResult.TextWithInlineKeyboard(var text, var keyboard) ->
                    new ActionResult.TextWithInlineKeyboard(prefix(text), keyboard);
            default -> screen;
        };
    }

    private static String prefix(String text) {
        return CANCELLED + "\n\n" + text;
    }
}
