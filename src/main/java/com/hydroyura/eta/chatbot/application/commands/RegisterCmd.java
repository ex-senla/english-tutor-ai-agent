package com.hydroyura.eta.chatbot.application.commands;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.Result;
import com.hydroyura.eta.chatbot.domain.statemachine.*;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;

public class RegisterCmd implements Command {

    private final RegisterTeacher registerTeacher;
    private final FindTeacher findTeacher;

    public RegisterCmd(RegisterTeacher registerTeacher, FindTeacher findTeacher) {
        this.registerTeacher = registerTeacher;
        this.findTeacher = findTeacher;
    }

    @Override public CommandType type() { return CommandType.REGISTER; }
    @Override public boolean matches(String text) { return text.startsWith("/register"); }

    @Override
    public Result execute(StateMachine sm, String userMessage) {
        // If coming from pending, this IS the name
        if (sm.getPendingCommandSafely().isPresent()) {
            return doRegister(sm, userMessage);
        }
        // From dispatch — userMessage is "/register Yury" or just "/register"
        if (userMessage.startsWith("/register ")) {
            return doRegister(sm, userMessage.substring(10).trim());
        }
        sm.setPendingCommand(RegisterCmd.class);
        return Result.stay("Enter your name:", type());
    }

    private Result doRegister(StateMachine sm, String name) {
        if (name.isBlank()) {
            sm.setPendingCommand(RegisterCmd.class);
            return Result.stay("Enter your name:", type());
        }
        // Check if already registered
        if (findTeacher.findByTelegramChatId(sm.getId().chatId()).isPresent()) {
            sm.clearPendingCommand();
            return Result.transition("Already registered!", type(), State.ACTIVE, new Context());
        }
        registerTeacher.execute(new RegisterTeacherCommand(sm.getId().chatId(), name));
        sm.clearPendingCommand();
        return Result.transition("✅ Registered!", type(), State.ACTIVE, new Context());
    }
}
