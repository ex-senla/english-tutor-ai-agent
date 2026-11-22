package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.hydroyura.eta.chatbot.view.Buttons;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.view.Messages.ONBOARDING_REVIEW;
import static org.assertj.core.api.Assertions.assertThat;

class OnboardingE2ETest extends BotE2eTestBase {

    private static final Long TEACHER_B_CHAT_ID = 888L;

    @Test
    void shouldShowOnboardingReviewFromAccountWithoutChangingFlag() {
        register("Иван");

        botTester.button(Buttons.ACCOUNT);
        assertThat(botTester.chatState()).isEqualTo("ACCOUNT");

        var profileButtons = botTester.lastMessage().inlineButtons().stream()
                .flatMap(List::stream)
                .toList();
        assertThat(profileButtons)
                .anyMatch(button -> button.label().equals("📖 Справка")
                        && button.callbackData().equals("account:onboarding"));

        botTester.reset();
        botTester.callback("account:onboarding");

        assertThat(botTester.chatState()).isEqualTo("ACCOUNT");
        assertThat(botTester.lastMessage().text()).contains(ONBOARDING_REVIEW);
        assertThat(botTester.rows("teacher").get(0).get("onboarding_completed")).isEqualTo(true);
    }

    @Test
    void shouldShowReviewOnImpersonatedAccountWithoutChangingActingTeacherFlag() {
        register(CHAT_ID, "Admin");
        register(TEACHER_B_CHAT_ID, "TeacherB");
        setAdmin(CHAT_ID, true);

        botTester.command("/start");
        botTester.button(Buttons.ADMIN_PANEL);
        botTester.callback("adm:teachers");
        var teacherBId = teacherId(TEACHER_B_CHAT_ID);
        botTester.callback("adm:teacher:" + teacherBId);
        botTester.callback("adm:imp:" + teacherBId);
        assertThat(botTester.chatState()).isEqualTo("ACTIVE");

        botTester.button(Buttons.ACCOUNT);
        assertThat(botTester.chatState()).isEqualTo("ACCOUNT");

        botTester.reset();
        botTester.callback("account:onboarding");

        assertThat(botTester.chatState()).isEqualTo("ACCOUNT");
        assertThat(botTester.lastMessage().text()).contains(ONBOARDING_REVIEW);
        assertThat(onboardingCompletedFor(teacherBId)).isEqualTo(true);
    }

    private void register(String name) {
        botTester.command("/register");
        botTester.text(name);
    }

    private void register(Long chatId, String name) {
        var tester = tester(chatId);
        tester.command("/register");
        tester.text(name);
    }

    private void setAdmin(Long chatId, boolean admin) {
        jdbcTemplate.update("UPDATE teacher SET admin = ? WHERE id = ?", admin, UUID.fromString(teacherId(chatId)));
    }

    private String teacherId(Long chatId) {
        return jdbcTemplate.queryForObject("""
                SELECT t.id FROM teacher t
                JOIN teacher_identifier i ON i.teacher_id = t.id
                WHERE i.identifier_type = 'TELEGRAM' AND i.identifier_value = ?
                """, String.class, chatId.toString());
    }

    private Boolean onboardingCompletedFor(String teacherId) {
        return jdbcTemplate.queryForObject("SELECT onboarding_completed FROM teacher WHERE id = ?",
                Boolean.class, UUID.fromString(teacherId));
    }
}
