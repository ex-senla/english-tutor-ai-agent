package com.hydroyura.eta.chatbot.item.menu;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;

import java.util.List;

public class MenuItem {

    public static ActionResult activeMenu(String userName) {
        return activeMenu(userName, null);
    }

    public static ActionResult activeMenu(String userName, Integer cleanupMessageId) {
        var text = activeMenuText(userName);
        var keyboard = List.of(
                List.of("➕ Новый студент", "👥 Мои студенты")
        );
        return new ActionResult.TextWithReplyKeyboard(text, keyboard, cleanupMessageId);
    }

    public static ActionResult activeMenuWithMessage(String message, String userName) {
        var text = message + "\n\n" + activeMenuText(userName);
        var keyboard = List.of(
                List.of("➕ Новый студент", "👥 Мои студенты")
        );
        return new ActionResult.TextWithReplyKeyboard(text, keyboard, null);
    }

    private static String activeMenuText(String userName) {
        return "Главное меню\n\n" +
                "➕ Новый студент — добавить ученика\n" +
                "👥 Мои студенты — список учеников";
    }
}
