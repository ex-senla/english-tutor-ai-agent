package com.hydroyura.eta.chatbot.view.menu;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.teacher.api.teacher.TeacherInfo;

import java.util.ArrayList;
import java.util.List;

import static com.hydroyura.eta.chatbot.view.Messages.IMPERSONATION_INDICATOR;

public final class MenuView {

    private MenuView() {
    }

    public static ActionResult activeMenu() {
        return activeMenu(null, null, null);
    }

    public static ActionResult activeMenu(Integer cleanupMessageId) {
        return activeMenu(null, null, cleanupMessageId);
    }

    public static ActionResult activeMenu(TeacherInfo teacherInfo, String actingTeacherName) {
        return activeMenu(teacherInfo, actingTeacherName, null);
    }

    public static ActionResult activeMenuWithMessage(String message) {
        return activeMenuWithMessage(message, null, null);
    }

    public static ActionResult activeMenuWithMessage(String message, TeacherInfo teacherInfo,
            String actingTeacherName) {
        var text = activeMenuText(teacherInfo, actingTeacherName);
        if (message != null && !message.isBlank()) {
            text = message + "\n\n" + text;
        }
        return new ActionResult.TextWithReplyKeyboard(text, keyboard(teacherInfo), null);
    }

    public static ActionResult activeMenu(TeacherInfo teacherInfo, String actingTeacherName,
            Integer cleanupMessageId) {
        return new ActionResult.TextWithReplyKeyboard(activeMenuText(teacherInfo, actingTeacherName),
                keyboard(teacherInfo), cleanupMessageId);
    }

    private static List<List<String>> keyboard(TeacherInfo teacherInfo) {
        var rows = new ArrayList<List<String>>();
        rows.add(List.of(Buttons.NEW_STUDENT, Buttons.LIST_STUDENT));
        rows.add(List.of(Buttons.FIND_STUDENT, Buttons.ACCOUNT));
        if (teacherInfo != null && teacherInfo.admin()) {
            rows.add(List.of(Buttons.ADMIN_PANEL));
        }
        return rows;
    }

    private static String activeMenuText(TeacherInfo teacherInfo, String actingTeacherName) {
        var text = new StringBuilder("Главное меню");
        if (actingTeacherName != null && !actingTeacherName.isBlank()) {
            text.append("\n\n").append(IMPERSONATION_INDICATOR.formatted(actingTeacherName));
        }
        text.append("\n\n")
                .append(Buttons.NEW_STUDENT).append(" — добавить ученика\n")
                .append(Buttons.LIST_STUDENT).append(" — список учеников\n")
                .append(Buttons.FIND_STUDENT).append(" — найти ученика\n")
                .append(Buttons.ACCOUNT).append(" — личный кабинет");
        if (teacherInfo != null && teacherInfo.admin()) {
            text.append('\n').append(Buttons.ADMIN_PANEL).append(" — админ-панель");
        }
        return text.toString();
    }
}
