package com.hydroyura.eta.chatbot.domain.chat;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Chat {

    @Getter
    private final ChatId id;

    @Getter
    private ChatState state;

    @Getter
    private final Map<String, Object> context = new HashMap<>();

    public static Chat ofDefaults(ChatId id) {
        var chat = new Chat(id);
        chat.updateState(ChatState.INITIAL);
        return chat;
    }

    public void updateState(ChatState chatState) {

        this.state = chatState;
    }
}
