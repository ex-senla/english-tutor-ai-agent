package com.hydroyura.eta.chatbot.infrastructure.bot.e2e.cucumber;

import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.BotTester;
import org.springframework.stereotype.Component;

/**
 * Per-scenario состояние Cucumber-прогона. Singleton-бин: один на весь прогон, сбрасывается
 * хуком {@code @Before} в {@link CucumberE2eHooks} перед каждым сценарием.
 */
@Component
public final class CucumberScenarioState {

    public static final Long CHAT_ID = 777L;

    private BotTester botTester;

    private Long chatId;

    private String studentId;

    private String studentName;

    private String studentLevel;

    private String wordInfoId;

    public BotTester getBotTester() {
        return botTester;
    }

    public void setBotTester(BotTester botTester) {
        this.botTester = botTester;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentLevel() {
        return studentLevel;
    }

    public void setStudentLevel(String studentLevel) {
        this.studentLevel = studentLevel;
    }

    public String getWordInfoId() {
        return wordInfoId;
    }

    public void setWordInfoId(String wordInfoId) {
        this.wordInfoId = wordInfoId;
    }
}
