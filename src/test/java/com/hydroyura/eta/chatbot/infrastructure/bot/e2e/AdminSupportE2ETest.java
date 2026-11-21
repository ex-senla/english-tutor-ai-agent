package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.hydroyura.eta.chatbot.view.Buttons;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_ACCESS_DENIED;
import static com.hydroyura.eta.chatbot.view.Messages.SUPPORT_ANSWER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

class AdminSupportE2ETest extends BotE2eTestBase {

    private static final Long TEACHER_B_CHAT_ID = 888L;

    @Test
    void shouldDenyAdminForNonAdmin() {
        register(CHAT_ID, "Иван");

        botTester.command("/admin");

        assertThat(botTester.chatState()).isEqualTo("ACTIVE");
        assertThat(botTester.lastMessage().text()).isEqualTo(ADMIN_ACCESS_DENIED);
    }

    @Test
    void shouldOpenAccountAndNavigateBack() {
        register(CHAT_ID, "Анна");

        botTester.button(Buttons.ACCOUNT);

        assertThat(botTester.chatState()).isEqualTo("ACCOUNT");
        assertThat(botTester.lastMessage().text()).contains("Анна").contains("0");

        botTester.callback("account:back");

        assertThat(botTester.chatState()).isEqualTo("ACTIVE");
        assertThat(botTester.lastMessage().keyboard()).isEqualTo(KeyboardKind.REPLY);
    }

    @Test
    void shouldPersistSupportThreadAndPushAdminAnswer() {
        register(CHAT_ID, "Admin");
        register(TEACHER_B_CHAT_ID, "TeacherB");
        setAdmin(CHAT_ID, true);

        var teacherTester = tester(TEACHER_B_CHAT_ID);
        teacherTester.command("/support");
        assertThat(teacherTester.chatState()).isEqualTo("SUPPORT");

        teacherTester.text("Помогите, пожалуйста");
        assertThat(botTester.count("support_thread")).isEqualTo(1);
        assertThat(botTester.count("support_message")).isEqualTo(1);

        var threadId = botTester.rows("support_thread").get(0).get("id").toString();

        botTester.command("/admin");
        assertThat(botTester.chatState()).isEqualTo("ADMIN_MENU");

        botTester.callback("adm:threads");
        assertThat(botTester.chatState()).isEqualTo("ADMIN_THREADS");

        botTester.callback("adm:thread:" + threadId);
        assertThat(botTester.chatState()).isEqualTo("ADMIN_THREAD_VIEW");

        botTester.text("Ответим в ближайшее время");

        assertThat(botTester.count("support_message")).isEqualTo(2);
        assertThat(teacherTester.chatState()).isEqualTo("SUPPORT");
        assertThat(outgoingCallsRecorder.messages())
                .anyMatch(message -> message.chatId().equals(TEACHER_B_CHAT_ID)
                        && message.text().contains(SUPPORT_ANSWER_PREFIX.trim()));
    }

    @Test
    void shouldImpersonateTeacherAndAddStudentToThatTeacher() {
        register(CHAT_ID, "Admin");
        register(TEACHER_B_CHAT_ID, "TeacherB");
        setAdmin(CHAT_ID, true);

        botTester.command("/start");
        assertThat(botTester.lastMessage().replyButtons()).contains(Buttons.ADMIN_PANEL);

        botTester.button(Buttons.ADMIN_PANEL);
        assertThat(botTester.chatState()).isEqualTo("ADMIN_MENU");

        botTester.callback("adm:teachers");
        assertThat(botTester.chatState()).isEqualTo("ADMIN_TEACHERS");

        var teacherBId = teacherId(TEACHER_B_CHAT_ID);
        botTester.callback("adm:teacher:" + teacherBId);
        assertThat(botTester.chatState()).isEqualTo("ADMIN_TEACHER_STUDENTS");

        botTester.callback("adm:imp:" + teacherBId);
        assertThat(botTester.chatState()).isEqualTo("ACTIVE");
        assertThat(botTester.lastMessage().text()).contains("TeacherB");

        botTester.button(Buttons.NEW_STUDENT);
        botTester.text("Пётр");
        botTester.callback("level:B1");

        assertThat(botTester.chatState()).isEqualTo("ACTIVE");
        var teacherStudentRows = botTester.rows("teacher_student");
        assertThat(teacherStudentRows).hasSize(1);
        assertThat(teacherStudentRows.get(0).get("teacher_id")).isEqualTo(java.util.UUID.fromString(teacherBId));
        assertThat(botTester.rows("student").get(0).get("name")).isEqualTo("Пётр");

        botTester.command("/cancel");
        assertThat(botTester.chatState()).isEqualTo("ADMIN_MENU");

        botTester.command("/start");
        assertThat(botTester.chatState()).isEqualTo("ACTIVE");
        assertThat(botTester.lastMessage().text()).doesNotContain("TeacherB");
        assertThat(botTester.lastMessage().replyButtons()).contains(Buttons.ADMIN_PANEL);

        setAdmin(CHAT_ID, false);
        botTester.command("/start");
        assertThat(botTester.lastMessage().replyButtons()).doesNotContain(Buttons.ADMIN_PANEL);
    }

    private void register(Long chatId, String name) {
        var tester = tester(chatId);
        tester.command("/register");
        tester.text(name);
    }

    private void setAdmin(Long chatId, boolean admin) {
        jdbcTemplate.update("UPDATE teacher SET admin = ? WHERE id = ?", admin, java.util.UUID.fromString(
                teacherId(chatId)));
    }

    private String teacherId(Long chatId) {
        return jdbcTemplate.queryForObject("""
                SELECT t.id FROM teacher t
                JOIN teacher_identifier i ON i.teacher_id = t.id
                WHERE i.identifier_type = 'TELEGRAM' AND i.identifier_value = ?
                """, String.class, chatId.toString());
    }
}
