package com.hydroyura.eta.support.application.config;

import com.hydroyura.eta.support.api.support.AppendSupportMessage;
import com.hydroyura.eta.support.api.support.FindSupportMessages;
import com.hydroyura.eta.support.api.support.FindSupportThreads;
import com.hydroyura.eta.support.api.support.OpenSupportThread;
import com.hydroyura.eta.support.application.usecase.AppendSupportMessageUseCase;
import com.hydroyura.eta.support.application.usecase.FindSupportMessagesUseCase;
import com.hydroyura.eta.support.application.usecase.FindSupportThreadsUseCase;
import com.hydroyura.eta.support.application.usecase.OpenSupportThreadUseCase;
import com.hydroyura.eta.support.domain.thread.SupportThreadRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupportConfig {

    @Bean
    OpenSupportThread openSupportThread(SupportThreadRepository repository) {
        return new OpenSupportThreadUseCase(repository);
    }

    @Bean
    AppendSupportMessage appendSupportMessage(SupportThreadRepository repository) {
        return new AppendSupportMessageUseCase(repository);
    }

    @Bean
    FindSupportThreads findSupportThreads(SupportThreadRepository repository) {
        return new FindSupportThreadsUseCase(repository);
    }

    @Bean
    FindSupportMessages findSupportMessages(SupportThreadRepository repository) {
        return new FindSupportMessagesUseCase(repository);
    }
}
