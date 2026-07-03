package com.hydroyura.eta.chatbot.application.commands;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.Result;
import com.hydroyura.eta.chatbot.domain.statemachine.CommandType;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import java.util.List;

public class StudentsCmd implements Command {

    private final FindTeacher findTeacher;
    private final StudentQuery studentQuery;

    public StudentsCmd(FindTeacher findTeacher, StudentQuery studentQuery) {
        this.findTeacher = findTeacher;
        this.studentQuery = studentQuery;
    }

    @Override
    public CommandType type() { return CommandType.STUDENTS; }

    @Override
    public boolean matches(String text) { return text.startsWith("/students"); }

    @Override
    public Result execute(StateMachine sm, String userMessage) {
        sm.clearPendingCommand();

        var studentIds = findTeacher.getStudentIds(sm.getId().chatId());
        if (studentIds.isEmpty()) {
            return Result.stay("No students yet. Use /newstudent to add one.", type());
        }

        var students = studentQuery.findStudentsByIds(studentIds);

        var keyboard = students.stream()
            .map(s -> List.of(s.name()))
            .toList();

        var lines = new StringBuilder("📋 Your students:\n\n");
        for (int i = 0; i < students.size(); i++) {
            lines.append(i + 1).append(". ").append(students.get(i).name()).append("\n");
        }
        lines.append("\nClick on a student to see details.");

        return Result.stay(lines.toString(), type(), keyboard);
    }
}
