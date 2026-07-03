package com.hydroyura.eta.chatbot.application.commands;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.Result;
import com.hydroyura.eta.chatbot.domain.statemachine.CommandType;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseType;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;

public class ExerciseCmd implements Command {

    private final FindTeacher findTeacher;
    private final StudentQuery studentQuery;
    private final GenerateExercise generateExercise;

    public ExerciseCmd(FindTeacher findTeacher, StudentQuery studentQuery, GenerateExercise generateExercise) {
        this.findTeacher = findTeacher;
        this.studentQuery = studentQuery;
        this.generateExercise = generateExercise;
    }

    @Override
    public CommandType type() { return CommandType.EXERCISE; }

    @Override
    public boolean matches(String text) { return text.startsWith("/exercise"); }

    @Override
    public Result execute(StateMachine sm, String userMessage) {
        var args = userMessage.substring("/exercise".length()).trim();

        if (sm.getPendingCommandSafely().isPresent()) {
            return doGenerate(sm, args);
        }
        sm.setPendingCommand(ExerciseCmd.class);
        if (args.isBlank()) {
            return Result.stay("Enter type (FILL_IN_THE_BLANK, MATCHING, TRANSLATION, MULTIPLE_CHOICE) and topic:", type());
        }
        return doGenerate(sm, args);
    }

    private Result doGenerate(StateMachine sm, String args) {
        if (args.isBlank()) {
            return Result.stay("Enter type (FILL_IN_THE_BLANK, MATCHING, TRANSLATION, MULTIPLE_CHOICE) and topic:", type());
        }

        var parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            return Result.stay("Need both type and topic. Example: FILL_IN_THE_BLANK Animals", type());
        }

        ExerciseType type;
        try {
            type = ExerciseType.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sm.clearPendingCommand();
            return Result.stay("Unknown type. Use: FILL_IN_THE_BLANK, MATCHING, TRANSLATION, MULTIPLE_CHOICE", type());
        }
        var topic = parts[1];

        var students = findTeacher.getStudentIds(sm.getId().chatId());
        if (students.isEmpty()) {
            sm.clearPendingCommand();
            return Result.stay("No students. Use /newstudent first", type());
        }

        var firstStudent = students.iterator().next();
        var dictId = studentQuery.getDictionaryId(firstStudent).orElse(null);
        if (dictId == null) {
            sm.clearPendingCommand();
            return Result.stay("Student has no dictionary", type());
        }

        var command = new GenerateExerciseCommand(type, topic, dictId);
        var result = generateExercise.execute(command);

        sm.clearPendingCommand();
        return Result.stay(result.content(), type());
    }
}
