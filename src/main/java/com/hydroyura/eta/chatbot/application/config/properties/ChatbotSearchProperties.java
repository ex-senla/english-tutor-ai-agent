package com.hydroyura.eta.chatbot.application.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "eta.chatbot.search")
public class ChatbotSearchProperties {

    private int suggestionsLimit = 10;
}
