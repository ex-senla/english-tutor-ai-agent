package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static com.hydroyura.eta.chatbot.view.Messages.EXERCISE_GENERATED;
import static com.hydroyura.eta.chatbot.view.Messages.NO_WORDS_IN_DICTIONARY;

@Slf4j
@RequiredArgsConstructor
public class InputAwaitingExerciseTopicTransition implements Transition<Action.Input> {

    private final StudentQuery studentQuery;

    private final GenerateExercise generateExercise;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var topic = input.text();
        var grammarRule = (String) chat.getContext().get("grammarRule");
        var studentIdStr = (String) chat.getContext().get("selectedStudentId");
        var studentId = new StudentId(UUID.fromString(studentIdStr));
        var exerciseType = (ExerciseType) chat.getContext().get("exerciseType");

        var dictionaryId = studentQuery.getDictionaryId(studentId)
                .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentIdStr));

        try {
            var exercise = generateExercise.execute(
                    new GenerateExerciseCommand(exerciseType, grammarRule, topic, dictionaryId));

            // TODO (временно): JSON упражнения и ответов выводим только в лог, в чат — заглушку.
            // Вернуть полноценный показ упражнения в чат и проверку ответа (AWAITING_EXERCISE_ANSWER).
            try {
                log.info("Exercise generated (JSON): {}", objectMapper.writeValueAsString(exercise));
            } catch (Exception e) {
                log.warn("Не удалось сериализовать упражнение {} в JSON: {}", exercise.id(), e.getMessage());
            }

            chat.getContext().remove("grammarRule");
            chat.getContext().remove("exerciseType");
            chat.updateState(ChatState.STUDENT_OPTIONS);

            return new ActionResult.TextWithInlineKeyboard(EXERCISE_GENERATED, StudentView.optionsKeyboard());
        } catch (IllegalArgumentException e) {
            log.warn("Cannot generate exercise: {}", e.getMessage());
            chat.getContext().remove("grammarRule");
            chat.getContext().remove("exerciseType");
            chat.updateState(ChatState.STUDENT_OPTIONS);
            return new ActionResult.TextWithInlineKeyboard(NO_WORDS_IN_DICTIONARY, StudentView.optionsKeyboard());
        }
    }
}
