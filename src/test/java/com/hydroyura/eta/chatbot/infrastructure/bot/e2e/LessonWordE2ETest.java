package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordPosTranslations;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LessonWordE2ETest extends BotE2eTestBase {

    @Test
    void shouldStartLesson() {
        register();
        addStudentViaMainMenu("Иван", "B1");
        botTester.reset();

        botTester.command("/list");
        botTester.callback("student:" + studentId());
        assertThat(botTester.chatState()).isEqualTo("STUDENT_OPTIONS");

        botTester.callback("action:startlesson");
        assertThat(botTester.chatState()).isEqualTo("IN_LESSON");
        var lessonMessage = botTester.lastMessage();
        assertThat(lessonMessage.keyboard()).isEqualTo(KeyboardKind.REPLY);
        assertThat(lessonMessage.replyButtons()).contains(Buttons.FINISH_LESSON);
        assertThat(botTester.count("lesson")).isEqualTo(1);
        assertThat(botTester.chatContext()).containsKey("activeLessonId");
    }

    @Test
    void shouldAddWordWithSuggestedTranslations() {
        reachInLesson();
        when(wordTranslator.suggestPosAndTranslations("apple"))
                .thenReturn(new WordPosTranslations(PartOfSpeech.NOUN, List.of("яблоко", "плод")));
        botTester.reset();

        botTester.text("apple");
        assertThat(botTester.chatState()).isEqualTo("AWAITING_TRANSLATION_CHOICE");
        var confirmation = botTester.lastMessage();
        assertThat(confirmation.keyboard()).isEqualTo(KeyboardKind.INLINE);
        assertThat(confirmation.text()).contains("apple");
        assertThat(flattenCallbacks(confirmation.inlineButtons())).contains("translation:0", "translation:1");
        verify(wordTranslator).suggestPosAndTranslations("apple");

        botTester.callback("translation:0");
        assertThat(botTester.chatState()).isEqualTo("IN_LESSON");
        assertThat(botTester.count("word_info")).isEqualTo(1);
        assertThat(botTester.count("word_info_translation")).isEqualTo(1);
        assertThat(botTester.count("lesson_word_info")).isEqualTo(1);
        assertThat(botTester.rows("word_info").get(0).get("word_value")).isEqualTo("apple");
        assertThat(botTester.rows("word_info_translation").get(0).get("translation")).isEqualTo("яблоко");
    }

    private void register() {
        botTester.command("/register");
        botTester.text("Иван");
    }

    private void reachInLesson() {
        register();
        addStudentViaMainMenu("Иван", "B1");

        botTester.command("/list");
        botTester.callback("student:" + studentId());
        botTester.callback("action:startlesson");
    }

    private void addStudentViaMainMenu(String name, String levelCode) {
        botTester.button(Buttons.NEW_STUDENT);
        botTester.text(name);
        botTester.callback("level:" + levelCode);
    }

    private String studentId() {
        return botTester.rows("student").get(0).get("id").toString();
    }

    private static List<String> flattenCallbacks(List<List<BotButton>> buttons) {
        return buttons.stream()
                .flatMap(List::stream)
                .map(BotButton::callbackData)
                .toList();
    }
}
