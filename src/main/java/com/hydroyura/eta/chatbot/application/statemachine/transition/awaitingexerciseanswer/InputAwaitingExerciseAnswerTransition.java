package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexerciseanswer;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.exercise.api.exercise.CheckExercise;
import com.hydroyura.eta.exercise.api.exercise.CheckExerciseCommand;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.hydroyura.eta.chatbot.view.Messages.EXERCISE_NOT_FOUND;
import static com.hydroyura.eta.chatbot.view.Messages.STUDENT_FEEDBACK;

@Slf4j
@RequiredArgsConstructor
public class InputAwaitingExerciseAnswerTransition implements Transition<Action.Input> {

    private final CheckExercise checkExercise;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var answer = input.text();
        var exerciseId = (ExerciseId) chat.getContext().get("exerciseId");
        var studentName = (String) chat.getContext().getOrDefault("selectedStudentName", "?");

        if (exerciseId == null) {
            chat.updateState(ChatState.STUDENT_OPTIONS);
            return new ActionResult.TextResponse(EXERCISE_NOT_FOUND);
        }

        var result = checkExercise.execute(new CheckExerciseCommand(exerciseId, answer));

        chat.getContext().remove("exerciseId");
        chat.getContext().remove("exerciseType");
        chat.getContext().remove("exerciseTopic");
        chat.updateState(ChatState.STUDENT_OPTIONS);

        log.info("Exercise {} checked: correct={}", exerciseId, result.correct());

        return new ActionResult.TextWithInlineKeyboard(
                STUDENT_FEEDBACK.formatted(studentName, result.feedback()), StudentView.optionsKeyboard());
    }

}
