package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdf;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdfCommand;
import com.hydroyura.eta.generator.api.document.PdfRenderException;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.GRAMMAR_RULE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.view.Messages.EXERCISE_PDF_ERROR;
import static com.hydroyura.eta.chatbot.view.Messages.NO_WORDS_IN_DICTIONARY;

@Slf4j
@RequiredArgsConstructor
public class InputAwaitingExerciseTopicTransition implements Transition<Action.Input> {

    private final StudentQuery studentQuery;

    private final GenerateExercise generateExercise;

    private final FindWords findWords;

    private final GenerateExercisePdf generateExercisePdf;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var topic = input.text();
        var grammarRule = (String) chat.getContext().get(GRAMMAR_RULE.getValue());
        var studentIdStr = (String) chat.getContext().get(SELECTED_STUDENT_ID.getValue());
        var studentId = new StudentId(UUID.fromString(studentIdStr));
        var exerciseType = (ExerciseType) chat.getContext().get(EXERCISE_TYPE.getValue());

        var dictionaryId = studentQuery.getDictionaryId(studentId)
                .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentIdStr));

        ExerciseDto exercise;
        try {
            exercise = generateExercise.execute(new GenerateExerciseCommand(exerciseType, grammarRule, topic,
                    dictionaryId));
        } catch (IllegalArgumentException e) {
            log.warn("Cannot generate exercise: {}", e.getMessage());
            reset(chat);
            return new ActionResult.TextWithInlineKeyboard(NO_WORDS_IN_DICTIONARY, StudentView.optionsKeyboard());
        }

        var vocabulary = new ArrayList<>(findWords.findByDictionaryId(dictionaryId));
        vocabulary.sort(Comparator.comparing(WordProjection::value, String.CASE_INSENSITIVE_ORDER));

        try {
            var bundle = generateExercisePdf.execute(new GenerateExercisePdfCommand(exercise, vocabulary));
            log.info("Exercise {} rendered to PDF (exercise + answers)", exercise.id());
            var name = (String) chat.getContext().getOrDefault("selectedStudentName", "?");
            reset(chat);
            return new ActionResult.SendDocuments(List.of(
                    new ActionResult.SendDocument(bundle.exercise().fileName(), bundle.exercise().content(),
                            "Упражнение"),
                    new ActionResult.SendDocument(bundle.answers().fileName(), bundle.answers().content(), "Ответы")
            ), "Ученик: " + name, StudentView.optionsKeyboard());
        } catch (PdfRenderException e) {
            log.warn("PDF render failed for exercise {}: {}", exercise.id(), e.getMessage());
            reset(chat);
            return new ActionResult.TextWithInlineKeyboard(EXERCISE_PDF_ERROR, StudentView.optionsKeyboard());
        }
    }

    private void reset(Chat chat) {
        chat.getContext().remove("grammarRule");
        chat.getContext().remove("exerciseType");
        chat.updateState(ChatState.STUDENT_OPTIONS);
    }
}
