package com.hydroyura.eta.chatbot.application.commands;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.Result;
import com.hydroyura.eta.chatbot.domain.statemachine.CommandType;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.exercise.api.exercise.CheckExercise;
import com.hydroyura.eta.exercise.api.exercise.CheckExerciseCommand;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import java.util.UUID;

public class ExerciseCmd implements Command {

    private static final String CTX_EXERCISE_ID = "exerciseId";

    private final FindTeacher findTeacher;
    private final StudentQuery studentQuery;
    private final GenerateExercise generateExercise;
    private final CheckExercise checkExercise;

    public ExerciseCmd(FindTeacher findTeacher,
                       StudentQuery studentQuery,
                       GenerateExercise generateExercise,
                       CheckExercise checkExercise) {
        this.findTeacher = findTeacher;
        this.studentQuery = studentQuery;
        this.generateExercise = generateExercise;
        this.checkExercise = checkExercise;
    }

    @Override
    public CommandType type() { return CommandType.EXERCISE; }

    @Override
    public boolean matches(String text) { return text.startsWith("/exercise"); }

    @Override
    public Result execute(StateMachine sm, String userMessage) {
        if (sm.getPendingCommandSafely().isPresent()) {
            // Check if there's an active exercise to answer
            var exerciseId = getCurrentExerciseId(sm);
            if (exerciseId != null) {
                return doCheck(sm, exerciseId, userMessage);
            }
            // Second phase: parse args for generation
            return doGenerate(sm, userMessage.substring("/exercise".length()).trim());
        }

        sm.setPendingCommand(ExerciseCmd.class);
        var args = userMessage.substring("/exercise".length()).trim();

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

        // Store exercise id and keep pending for answer
        sm.getContext().put(CTX_EXERCISE_ID, result.id().value().toString());

        return Result.stay(result.content() + "\n\nReply with your answer:", type());
    }

    private Result doCheck(StateMachine sm, ExerciseId exerciseId, String userAnswer) {
        var command = new CheckExerciseCommand(exerciseId, userAnswer);
        var result = checkExercise.execute(command);

        sm.getContext().remove(CTX_EXERCISE_ID);
        sm.clearPendingCommand();

        var response = result.feedback();
        if (result.correct()) {
            response += "\n\nUse /exercise to try another one!";
        } else {
            response += "\n\nTry /exercise for a new exercise.";
        }

        return Result.stay(response, type());
    }

    private ExerciseId getCurrentExerciseId(StateMachine sm) {
        var raw = sm.getContext().get(CTX_EXERCISE_ID);
        if (raw instanceof String idStr) {
            try {
                return new ExerciseId(UUID.fromString(idStr));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
