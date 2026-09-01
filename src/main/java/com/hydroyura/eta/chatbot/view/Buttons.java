package com.hydroyura.eta.chatbot.view;

import java.util.Set;

public final class Buttons {

    public static final String NEW_STUDENT = "➕ Новый студент";
    
    public static final String LIST_STUDENT = "👥 Мои студенты";
    
    public static final String ADD_WORD = "➕ Добавить слово";
    
    public static final String FINISH_LESSON = "🏁 Завершить урок";
    
    public static final String START_LESSON = "▶ Start Lesson";
    
    public static final String DETAILS = "📋 Details";
    
    public static final String EXERCISE = "🎯 Exercise";
    
    public static final String BACK = "◀ Back";

    public static final String NOUN = "📛 Noun";

    public static final String VERB = "🏃 Verb";

    public static final String ADJECTIVE = "🎨 Adjective";

    public static final Set<String> REPLY_BUTTONS = Set.of(NEW_STUDENT, LIST_STUDENT, ADD_WORD, FINISH_LESSON);

    public static Set<String> getReplyButtons() {
        return REPLY_BUTTONS;
    }

}
