package com.hydroyura.eta.chatbot.view.admin;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.Messages;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.support.api.support.SupportAuthor;
import com.hydroyura.eta.support.api.support.SupportMessageInfo;
import com.hydroyura.eta.support.api.support.SupportThreadInfo;
import com.hydroyura.eta.teacher.api.teacher.TeacherInfo;

import java.util.List;

import static com.hydroyura.eta.chatbot.view.util.ViewUtils.createCallbackData;

public final class AdminView {

    private AdminView() {
    }

    public record ThreadRow(SupportThreadInfo info, String teacherName) {
    }

    public static ActionResult menu() {
        return new ActionResult.TextWithInlineKeyboard(Messages.ADMIN_MENU, List.of(
                List.of(button("👥 Учителя", Callbacks.TEACHERS)),
                List.of(button("💬 Обращения", Callbacks.THREADS)),
                List.of(backButton("🔙 В меню", Callbacks.MAIN))));
    }

    public static ActionResult teachers(List<TeacherInfo> teachers) {
        if (teachers.isEmpty()) {
            return new ActionResult.TextWithInlineKeyboard("Учителей нет", List.of(List.of(backButton("🔙 Назад",
                    Callbacks.MENU))));
        }
        var keyboard = teachers.stream()
                .map(teacher -> List.of(button(teacher.name() + (teacher.admin() ? " 🛡" : ""),
                        Callbacks.TEACHER + ":" + teacher.id().value())))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        keyboard.add(List.of(backButton("🔙 Назад", Callbacks.MENU)));
        return new ActionResult.TextWithInlineKeyboard("Учителя:", keyboard);
    }

    public static ActionResult teacher(String name, List<StudentInfo> students, String teacherId) {
        var text = new StringBuilder("Учитель: ").append(name).append("\n\nУченики:\n");
        if (students.isEmpty()) {
            text.append("нет");
        } else {
            students.forEach(s -> text.append("• ").append(s.name()).append('\n'));
        }
        var keyboard = new java.util.ArrayList<List<ActionResult.InlineButton>>();
        keyboard.add(List.of(button("👤 Войти как учитель", Callbacks.IMPERSONATE + ":" + teacherId)));
        keyboard.add(List.of(backButton("🔙 Назад", Callbacks.TEACHERS)));
        return new ActionResult.TextWithInlineKeyboard(text.toString(), keyboard);
    }

    public static ActionResult threads(List<ThreadRow> rows) {
        if (rows.isEmpty()) {
            return new ActionResult.TextWithInlineKeyboard("Обращений нет", List.of(List.of(backButton("🔙 Назад",
                    Callbacks.MENU))));
        }
        var keyboard = rows.stream()
                .map(row -> List.of(button(row.teacherName(),
                        Callbacks.THREAD + ":" + row.info().id().value())))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        keyboard.add(List.of(backButton("🔙 Назад", Callbacks.MENU)));
        return new ActionResult.TextWithInlineKeyboard("Обращения:", keyboard);
    }

    public static ActionResult threadView(String teacherName, List<SupportMessageInfo> messages) {
        var builder = new StringBuilder("Диалог: ").append(teacherName).append('\n');
        if (messages.isEmpty()) {
            builder.append("\nИстория пуста.");
        } else {
            messages.forEach(message -> builder.append('\n')
                    .append(message.author() == SupportAuthor.TEACHER ? "👨‍🏫 Учитель" : "🛡 Админ")
                    .append(": ")
                    .append(message.text()));
        }
        builder.append("\n\n").append(Messages.ADMIN_THREAD_VIEW_HINT);
        return new ActionResult.TextWithInlineKeyboard(builder.toString(),
                List.of(List.of(backButton("🔙 К списку", Callbacks.THREADS))));
    }

    private static ActionResult.InlineButton button(String label, String payload) {
        return new ActionResult.InlineButton(label, createCallbackData(Callbacks.ADMIN, payload));
    }

    private static ActionResult.InlineButton backButton(String label, String target) {
        return new ActionResult.InlineButton(label, createCallbackData(Callbacks.ADMIN, Callbacks.BACK + ":" + target));
    }
}
