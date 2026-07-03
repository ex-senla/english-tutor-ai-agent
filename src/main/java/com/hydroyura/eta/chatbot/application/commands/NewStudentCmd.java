package com.hydroyura.eta.chatbot.application.commands;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.Result;
import com.hydroyura.eta.chatbot.domain.statemachine.*;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;

public class NewStudentCmd implements Command {

    private final CreateStudentWithDictionary createStudentWithDictionary;
    private final FindTeacher findTeacher;

    public NewStudentCmd(CreateStudentWithDictionary c, FindTeacher f) {
        this.createStudentWithDictionary = c; this.findTeacher = f;
    }

    @Override public CommandType type() { return CommandType.NEW_STUDENT; }
    @Override public boolean matches(String text) { return text.startsWith("/newstudent"); }

    @Override
    public Result execute(StateMachine sm, String userMessage) {
        if (sm.getPendingCommandSafely().isPresent()) {
            return doCreate(sm, userMessage);
        }
        if (userMessage.startsWith("/newstudent ")) {
            return doCreate(sm, userMessage.substring(12).trim());
        }
        sm.setPendingCommand(NewStudentCmd.class);
        return Result.stay("Enter student name:", type());
    }

    private Result doCreate(StateMachine sm, String name) {
        if (name.isBlank()) { sm.setPendingCommand(NewStudentCmd.class); return Result.stay("Enter student name:", type()); }
        var tid = findTeacher.findByTelegramChatId(sm.getId().chatId()).orElseThrow();
        createStudentWithDictionary.execute(new CreateStudentWithDictionaryCommand(tid, name, name + "'s Dictionary"));
        sm.clearPendingCommand();
        return Result.stay("✅ Student " + name + " created", type());
    }
}
