package com.hydroyura.eta.chatbot.view.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemUtils {

    public static String createCallbackData(String... data) {
        return String.join(":", data);
    }

}
