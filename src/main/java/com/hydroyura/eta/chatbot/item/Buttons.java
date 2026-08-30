package com.hydroyura.eta.chatbot.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Buttons {

    ADD_STUDENT("➕ Новый студент"),
    LIST_STUDENT("👥 Мои студенты"),
    ADD_WORD("➕ Добавить слово"),
    FINISH_LESSON("🏁 Завершить урок");

    private final String value;

    private static final Set<String> buttons = Arrays.stream(Buttons.values()).map(Buttons::getValue).collect(Collectors.toSet());

    public static Set<String> getAllValues() {
        return buttons;
    }

}
