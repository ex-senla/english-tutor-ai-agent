package com.hydroyura.eta.chatbot.application.statemachine.transition.active;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.item.menu.MenuItem;
import com.hydroyura.eta.chatbot.item.students.StudentItem;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * Изначальный (монолитный) вариант обработки ACTIVE — оставлен как референс.
 * Рабочая логика разрезана на DefaultCmd/ListCmd/NewCmd/NewStudentBtn/MyStudentsBtn переходы.
 */
@RequiredArgsConstructor
public class ActiveTransition implements Transition<Action> {

    private final FindTeacher findTeacher;

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action action) {
        if (action instanceof Action.Command(var cmd)) {
            return switch (cmd) {
                case "/new" -> {
                    chat.updateState(ChatState.AWAITING_STUDENT_NAME);
                    yield new ActionResult.TextResponse("Введите имя нового ученика");
                }
                case "/list" -> {
                    chat.updateState(ChatState.STUDENTS_LIST);
                    Set<StudentId> studentIds = findTeacher.getStudentIds(chat.getId().chatId());
                    List<StudentInfo> students = studentQuery.findStudentsByIds(studentIds);
                    yield StudentItem.studentsListMenu(chat, students);
                }
                case "/help", "/start" -> MenuItem.activeMenu("");
                default -> new ActionResult.TextResponse(
                        "Неизвестная команда. /help — список команд");
            };
        }
        if (action instanceof Action.Input(var text)) {
            if ("➕ Новый студент".equals(text)) {
                chat.updateState(ChatState.AWAITING_STUDENT_NAME);
                return new ActionResult.TextResponse("Введите имя нового ученика");
            }
            if ("👥 Мои студенты".equals(text)) {
                chat.updateState(ChatState.STUDENTS_LIST);
                Set<StudentId> studentIds = findTeacher.getStudentIds(chat.getId().chatId());
                List<StudentInfo> students = studentQuery.findStudentsByIds(studentIds);
                return StudentItem.studentsListMenu(chat, students);
            }
        }
        return new ActionResult.TextResponse(
                "Неизвестная команда. /help — список команд");
    }

    @Override
    public String getName() {
        return "ActiveTransition";
    }
}
