package com.hydroyura.eta.chatbot.application.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "eta.chatbot.pagination")
public class ChatbotPaginationProperties {

    private int pageSize = 8;
}
