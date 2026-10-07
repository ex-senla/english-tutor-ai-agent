package com.hydroyura.eta.chatbot.view.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewUtils {

    public static String createCallbackData(String... data) {
        return String.join(":", data);
    }

}
