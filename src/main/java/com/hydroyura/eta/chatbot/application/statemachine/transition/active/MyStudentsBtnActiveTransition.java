package com.hydroyura.eta.chatbot.application.statemachine.transition.active;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.item.students.StudentItem;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class MyStudentsBtnActiveTransition implements Transition<Action.Button> {

    private final FindTeacher findTeacher;

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action.Button button) {
        chat.updateState(ChatState.STUDENTS_LIST);
        Set<StudentId> studentIds = findTeacher.getStudentIds(chat.getId().chatId());
        List<StudentInfo> students = studentQuery.findStudentsByIds(studentIds);
        return StudentItem.studentsListMenu(chat, students);
    }

    @Override
    public String getName() {
        return "MyStudentsBtnActiveTransition";
    }
}
