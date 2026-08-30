package com.hydroyura.eta.chatbot.application.statemachine.transition.awatingexercisetopic;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class InputAwatingExerciseTopicTransition implements Transition<Action.Input> {

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

        var typeLabel = exerciseType == ExerciseType.FILL_IN_THE_BLANK
                ? "✏️ Fill in the blank" : "🔤 Multiple choice";

        log.info("Exercise {} generated: type={}, topic={}", exercise.id(), exerciseType, topic);
        return new ActionResult.TextResponse(
                typeLabel + " | Тема: " + topic + "\n\n" + exercise.content()
                        + "\n\n✍️ Введите ваш ответ:");
    }

    @Override
    public String getName() {
        return "InputAwatingExerciseTopicTransition";
    }
}
