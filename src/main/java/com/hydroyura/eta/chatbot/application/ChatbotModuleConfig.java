package com.hydroyura.eta.chatbot.application;

import com.hydroyura.eta.chatbot.domain.command.CommandDispatcher;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachineRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatbotModuleConfig {

    @Bean
    StateMachineAppService stateMachineAppService(StateMachineRepository repo, CommandDispatcher disp) {
        return new StateMachineAppService(repo, disp);
    }
}
