package com.hydroyura.eta.chatbot.application.commands;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.Result;
import com.hydroyura.eta.chatbot.domain.statemachine.CommandType;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.student.api.student.FindStudentByNameQuery;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;

public class StudentCmd implements Command {

    private final FindTeacher findTeacher;
    private final StudentQuery studentQuery;

    public StudentCmd(FindTeacher findTeacher, StudentQuery studentQuery) {
        this.findTeacher = findTeacher;
        this.studentQuery = studentQuery;
    }

    @Override
    public CommandType type() { return CommandType.STUDENT; }

    @Override
    public boolean matches(String text) { return text.startsWith("/student"); }

    @Override
    public Result execute(StateMachine sm, String userMessage) {
        sm.clearPendingCommand();

        var args = userMessage.substring("/student".length()).trim();
        if (args.isBlank()) {
            return Result.stay("Usage: /student <name>", type());
        }

        var studentIds = findTeacher.getStudentIds(sm.getId().chatId());
        if (studentIds.isEmpty()) {
            return Result.stay("No students found. Use /newstudent first.", type());
        }

        var studentId = studentQuery.findByNameIn(
            new FindStudentByNameQuery(studentIds, args));

        if (studentId.isEmpty()) {
            return Result.stay("Student not found: " + args + ". Check /students for the list.", type());
        }

        var details = studentQuery.findStudentDetails(studentId.get());
        if (details.isEmpty()) {
            return Result.stay("Could not load student details.", type());
        }

        var d = details.get();
        var stats = d.dictionaryStats();
        var msg = """
            👤 %s
                        
            📚 Dictionary: %d words
               • New: %d
               • In progress: %d
               • Learned: %d
                        
            📖 Active lesson: %s
            """.formatted(
            d.name(),
            stats.totalWords(), stats.newCount(), stats.inProgressCount(), stats.learnedCount(),
            d.hasActiveLesson() ? "✅ Yes" : "❌ No"
        );

        return Result.stay(msg, type());
    }
}
