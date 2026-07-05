package com.hydroyura.eta.chatbot.application.config;

import com.hydroyura.eta.chatbot.application.commands.TeacherRegistrationCommand;
import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.inputchain.Chain;
import com.hydroyura.eta.chatbot.domain.inputchain.DefaultInputProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.hydroyura.eta.chatbot.domain.statemachine.ContextKeys.TEACHER_NAME;

@Configuration
class CommandsConfig {

    @Bean
    Command teacherRegistrationCommand() {
        var inputProcessor = new DefaultInputProcessor("Введите Ваше имя", TEACHER_NAME, String.class);
        var chain = new Chain(List.of(inputProcessor), "Имя добавлено успешно");
        return new TeacherRegistrationCommand(chain);
    }

}
