package com.hydroyura.eta.chatbot.view.menu;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;

import java.util.List;

public class MenuView {

    public static ActionResult activeMenu() {
        return activeMenu(null);
    }

    public static ActionResult activeMenu(Integer cleanupMessageId) {
        var text = activeMenuText();
        var keyboard = List.of(
                List.of(Buttons.NEW_STUDENT, Buttons.LIST_STUDENT)
        );
        return new ActionResult.TextWithReplyKeyboard(text, keyboard, cleanupMessageId);
    }

    public static ActionResult activeMenuWithMessage(String message) {
        var text = message + "\n\n" + activeMenuText();
        var keyboard = List.of(
                List.of(Buttons.NEW_STUDENT, Buttons.LIST_STUDENT)
        );
        return new ActionResult.TextWithReplyKeyboard(text, keyboard, null);
    }

    private static String activeMenuText() {
        return """
                Главное меню
                
                %s — добавить ученика
                %s — список учеников""".formatted(Buttons.NEW_STUDENT, Buttons.LIST_STUDENT);
    }
}
