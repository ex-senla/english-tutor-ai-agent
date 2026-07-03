package com.hydroyura.eta.chatbot.application.commands;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.Result;
import com.hydroyura.eta.chatbot.domain.statemachine.*;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.AddWordToLessonCommand;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import java.util.*;

public class AddWordCmd implements Command {

    private static final String STEP = "addWord.step";
    private static final String WORD = "addWord.word";
    private static final String POS = "addWord.pos";
    private static final String LESSON_ID = "lessonId";

    private final AddWordToDictionary addWordToDictionary;
    private final AddWordToLesson addWordToLesson;
    private final StudentQuery studentQuery;
    private final FindTeacher findTeacher;

    public AddWordCmd(AddWordToDictionary awd, AddWordToLesson awl, StudentQuery sq, FindTeacher ft) {
        this.addWordToDictionary = awd; this.addWordToLesson = awl; this.studentQuery = sq; this.findTeacher = ft;
    }

    @Override public CommandType type() { return CommandType.ADD_WORD; }
    @Override public boolean matches(String text) { return text.startsWith("/add"); }

    @Override
    public Result execute(StateMachine sm, String userMessage) {
        if (sm.getPendingCommandSafely().isPresent())
            return handleStep(sm, userMessage);

        sm.setPendingCommand(AddWordCmd.class);
        if (userMessage.startsWith("/add ")) {
            resetContext(sm);
            return handleStep(sm, userMessage.substring(5).trim());
        }
        resetContext(sm);
        return Result.stay("Enter word:", type());
    }

    private Result handleStep(StateMachine sm, String input) {
        int step = getStep(sm);
        return switch (step) {
            case 0 -> stepWord(sm, input);
            case 1 -> stepPos(sm, input);
            case 2 -> stepTranslation(sm, input);
            default -> { sm.clearPendingCommand(); yield Result.stay("✅ Word added", type()); }
        };
    }

    private Result stepWord(StateMachine sm, String input) {
        if (input.isBlank()) return Result.stay("Enter word:", type());
        sm.getContext().put(WORD, input.trim());
        sm.getContext().put(STEP, 1);
        return Result.stay("Enter part of speech (NOUN/VERB/ADJECTIVE):", type());
    }

    private Result stepPos(StateMachine sm, String input) {
        try {
            PartOfSpeech.valueOf(input.trim().toUpperCase());
            sm.getContext().put(POS, input.trim().toUpperCase());
            sm.getContext().put(STEP, 2);
            return Result.stay("Enter translation:", type());
        } catch (IllegalArgumentException e) {
            return Result.stay("Invalid. Use NOUN, VERB or ADJECTIVE:", type());
        }
    }

    private Result stepTranslation(StateMachine sm, String input) {
        if (input.isBlank()) return Result.stay("Enter translation:", type());

        var word = (String) sm.getContext().get(WORD);
        var posStr = (String) sm.getContext().get(POS);
        var pos = PartOfSpeech.valueOf(posStr);

        var translations = new HashSet<>(Arrays.asList(input.split(";")));
        translations.removeIf(String::isBlank);

        // Find student and dictionary
        var studentIds = findTeacher.getStudentIds(sm.getId().chatId());
        var studentId = findActiveLessonStudent(sm, studentIds);
        if (studentId.isEmpty()) {
            resetContext(sm); sm.clearPendingCommand();
            return Result.stay("No active lesson — word not saved", type());
        }
        var dictId = studentQuery.getDictionaryId(studentId.get()).orElseThrow();
        var wid = addWordToDictionary.execute(new AddWordCommand(dictId, word, translations, pos));

        // Add to lesson if one is active
        var lessonId = (java.util.UUID) sm.getContext().get(LESSON_ID);
        if (lessonId != null) {
            addWordToLesson.execute(new AddWordToLessonCommand(
                new com.hydroyura.eta.student.api.lesson.LessonId(lessonId), wid));
        }

        resetContext(sm);
        sm.clearPendingCommand();
        return Result.stay("✅ \"" + word + "\" (" + pos + ") added", type());
    }

    private java.util.Optional<com.hydroyura.eta.student.api.student.StudentId> findActiveLessonStudent(
        StateMachine sm, java.util.Set<com.hydroyura.eta.student.api.student.StudentId> studentIds) {
        for (var sid : studentIds) {
            var dictId = studentQuery.getDictionaryId(sid);
            if (dictId.isPresent()) return java.util.Optional.of(sid);
        }
        return java.util.Optional.empty();
    }

    private int getStep(StateMachine sm) {
        Object s = sm.getContext().get(STEP);
        return s instanceof Integer i ? i : 0;
    }

    private void resetContext(StateMachine sm) {
        sm.getContext().put(STEP, 0);
        sm.getContext().put(WORD, null);
        sm.getContext().put(POS, null);
    }
}
