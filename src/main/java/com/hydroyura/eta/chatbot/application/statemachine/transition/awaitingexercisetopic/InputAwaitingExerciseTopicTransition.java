package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static com.hydroyura.eta.chatbot.view.Messages.EXERCISE_ENTER_ANSWER;
import static com.hydroyura.eta.chatbot.view.Messages.FILL_IN_THE_BLANK;
import static com.hydroyura.eta.chatbot.view.Messages.MULTIPLE_CHOICE;

@Slf4j
@RequiredArgsConstructor
public class InputAwaitingExerciseTopicTransition implements Transition<Action.Input> {

    private final StudentQuery studentQuery;

    private final GenerateExercise generateExercise;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var topic = input.text();
        var studentIdStr = (String) chat.getContext().get("selectedStudentId");
        var studentId = new StudentId(UUID.fromString(studentIdStr));
        var exerciseType = (ExerciseType) chat.getContext().get("exerciseType");

        var dictionaryId = studentQuery.getDictionaryId(studentId)
                .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentIdStr));

        var exercise = generateExercise.execute(
                new GenerateExerciseCommand(exerciseType, topic, dictionaryId));

        chat.getContext().put("exerciseId", exercise.id());
        chat.getContext().put("exerciseTopic", topic);
        chat.updateState(ChatState.AWAITING_EXERCISE_ANSWER);

        var typeLabel = exerciseType == ExerciseType.FILL_IN_THE_BLANK ? FILL_IN_THE_BLANK : MULTIPLE_CHOICE;

        log.info("Exercise {} generated: type={}, topic={}", exercise.id(), exerciseType, topic);
        return new ActionResult.TextResponse(EXERCISE_ENTER_ANSWER.formatted(typeLabel, topic, exercise.content()));
    }

}
