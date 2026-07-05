package com.hydroyura.eta.chatbot.application.commands;

import com.hydroyura.eta.chatbot.domain.command.AbstractStepsCommand;
import com.hydroyura.eta.chatbot.domain.command.Type;
import com.hydroyura.eta.chatbot.domain.inputchain.Chain;

import static com.hydroyura.eta.chatbot.domain.command.Type.TEACHER_REGISTRATION;

public class TeacherRegistrationCommand extends AbstractStepsCommand {

    public TeacherRegistrationCommand(Chain chain) {
        super(chain);
    }

    @Override
    public Type getType() {
        return TEACHER_REGISTRATION;
    }

}
