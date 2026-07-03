package com.hydroyura.eta.chatbot.application.commands;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.Result;
import com.hydroyura.eta.chatbot.domain.statemachine.*;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.lesson.StartLessonCommand;
import com.hydroyura.eta.student.api.student.FindStudentByNameQuery;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;

public class StartLessonCmd implements Command {

    private final StartLesson startLesson;
    private final FindTeacher findTeacher;
    private final StudentQuery studentQuery;

    public StartLessonCmd(StartLesson s, FindTeacher f, StudentQuery sq) {
        this.startLesson = s; this.findTeacher = f; this.studentQuery = sq;
    }

    @Override public CommandType type() { return CommandType.START_LESSON; }
    @Override public boolean matches(String text) { return text.startsWith("/startlesson"); }

    @Override
    public Result execute(StateMachine sm, String userMessage) {
        if (sm.getPendingCommandSafely().isPresent()) {
            return doStart(sm, userMessage);
        }
        if (userMessage.startsWith("/startlesson ")) {
            return doStart(sm, userMessage.substring(13).trim());
        }
        sm.setPendingCommand(StartLessonCmd.class);
        return Result.stay("Enter student name:", type());
    }

    private Result doStart(StateMachine sm, String name) {
        if (name.isBlank()) { sm.setPendingCommand(StartLessonCmd.class); return Result.stay("Enter student name:", type()); }
        var ids = findTeacher.getStudentIds(sm.getId().chatId());
        var sid = studentQuery.findByNameIn(new FindStudentByNameQuery(ids, name));
        if (sid.isEmpty()) return Result.stay("Student not found", type());
        var lid = startLesson.execute(new StartLessonCommand(sid.get(), "Lesson"));
        var ctx = new Context();
        ctx.put("lessonId", lid.value());
        sm.clearPendingCommand();
        return Result.transition("✅ Lesson started", type(), State.IN_LESSON, ctx);
    }
}
