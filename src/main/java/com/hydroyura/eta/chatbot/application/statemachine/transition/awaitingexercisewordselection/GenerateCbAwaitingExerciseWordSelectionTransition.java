package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection;

import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.exercise.ExerciseWordSelectionView;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.exercise.api.exercise.ExerciseTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdf;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdfCommand;
import com.hydroyura.eta.generator.api.document.PdfRenderException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_THEMATIC_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;
import static com.hydroyura.eta.chatbot.view.Messages.EXERCISE_PDF_ERROR;

@RequiredArgsConstructor
@Slf4j
public class GenerateCbAwaitingExerciseWordSelectionTransition implements Transition<Action.Callback> {

    private final boolean withWords;

    private final ExerciseWordSelectionSupport support;

    private final GenerateExercise generateExercise;

    private final GenerateExercisePdf generateExercisePdf;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var selected = support.selectedWordIds(chat);
        if (withWords && selected.isEmpty()) {
            return ExerciseWordSelectionView.warning(callback.messageId());
        }

        return generate(chat, callback.messageId(), withWords ? selected : Set.of());
    }

    private ActionResult generate(Chat chat, int messageId, Set<WordInfoId> wordIds) {
        var topic = (ExerciseTopic) chat.getContext().get(EXERCISE_TOPIC.getValue());
        var thematicTopic = (String) chat.getContext().get(EXERCISE_THEMATIC_TOPIC.getValue());
        var exerciseType = (ExerciseType) chat.getContext().get(EXERCISE_TYPE.getValue());

        var studentId = StudentSupport.selectedStudentId(chat);
        var dictionaryId = support.dictionaryId(chat);
        var cefrLevel = support.cefrLevel(chat);

        var exercise = generateExercise.execute(new GenerateExerciseCommand(exerciseType, topic, thematicTopic,
                dictionaryId, cefrLevel.code(), wordIds));

        var vocabulary = withWords ? support.selectedWords(chat) : List.<WordProjection>of();
        var studentName = (String) chat.getContext().getOrDefault(SELECTED_STUDENT_NAME.getValue(), "?");

        try {
            var bundle = generateExercisePdf.execute(new GenerateExercisePdfCommand(exercise, vocabulary, studentName,
                    cefrLevel.code()));
            log.info("Exercise {} rendered to PDF (exercise + answers)", exercise.id());
            reset(chat);
            return new ActionResult.SendDocuments(List.of(
                    new ActionResult.SendDocument(bundle.exercise().fileName(), bundle.exercise().content(),
                            "Упражнение"),
                    new ActionResult.SendDocument(bundle.answers().fileName(), bundle.answers().content(), "Ответы")
            ), "Ученик: " + studentName, StudentView.optionsKeyboard());
        } catch (PdfRenderException e) {
            log.warn("PDF render failed for exercise {}: {}", exercise.id(), e.getMessage());
            reset(chat);
            return new ActionResult.TextWithInlineKeyboard(EXERCISE_PDF_ERROR, StudentView.optionsKeyboard());
        }
    }

    private void reset(Chat chat) {
        chat.getContext().remove(EXERCISE_TYPE.getValue());
        chat.getContext().remove(EXERCISE_TOPIC.getValue());
        chat.getContext().remove(EXERCISE_THEMATIC_TOPIC.getValue());
        chat.getContext().remove(WORD_SELECTION.getValue());
        chat.getContext().remove(WORD_FILTER.getValue());
        chat.getContext().remove(SELECTION_PAGE.getValue());
        chat.updateState(ChatState.STUDENT_OPTIONS);
    }
}
