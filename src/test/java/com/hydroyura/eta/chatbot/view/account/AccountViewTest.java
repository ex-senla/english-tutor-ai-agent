package com.hydroyura.eta.chatbot.view.account;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountViewTest {

    @Test
    void profileShouldContainOnboardingHelpButton() {
        var result = AccountView.profile("Иван", 0);

        var keyboard = ((ActionResult.TextWithInlineKeyboard) result).keyboard();
        var buttons = keyboard.stream().flatMap(java.util.Collection::stream).toList();

        assertThat(buttons)
                .anyMatch(button -> button.text().equals("📖 Справка")
                        && button.callbackData().equals("account:onboarding"));
    }
}
