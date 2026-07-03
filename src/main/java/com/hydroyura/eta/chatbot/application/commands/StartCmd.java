package com.hydroyura.eta.chatbot.application.commands;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.Result;
import com.hydroyura.eta.chatbot.domain.statemachine.*;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;

public class StartCmd implements Command {

    private final FindTeacher findTeacher;

    public StartCmd(FindTeacher findTeacher) { this.findTeacher = findTeacher; }

    @Override public CommandType type() { return CommandType.START; }
    @Override public boolean matches(String text) { return text.equals("/start"); }

    @Override
    public Result execute(StateMachine sm, String userMessage) {
        if (findTeacher.findByTelegramChatId(sm.getId().chatId()).isPresent())
            return Result.transition("Welcome back!", type(), State.ACTIVE, new Context());
        return Result.stay("Welcome! /register <name>", type());
    }
}
