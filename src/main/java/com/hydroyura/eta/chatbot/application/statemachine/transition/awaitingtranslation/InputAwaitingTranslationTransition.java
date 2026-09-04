package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingtranslation;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.view.lesson.LessonView;
import com.hydroyura.eta.chatbot.view.word.WordView;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.AddWordToLessonCommand;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.UUID;

import static com.hydroyura.eta.chatbot.view.Messages.WORD_POS_TRANSLATIONS;

@Slf4j
@RequiredArgsConstructor
public class InputAwaitingTranslationTransition implements Transition<Action.Input> {

    private final StudentQuery studentQuery;

    private final AddWordToDictionary addWordToDictionary;

    private final FindActiveLesson findActiveLesson;

    private final AddWordToLesson addWordToLesson;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var studentIdStr = (String) chat.getContext().get("selectedStudentId");
        var studentId = new StudentId(UUID.fromString(studentIdStr));
        var wordValue = (String) chat.getContext().get("wordValue");
        var pos = (PartOfSpeech) chat.getContext().get("wordPos");

        var translations = Set.of(input.text().split("\\s*,\\s*"));

        var dictionaryId = studentQuery.getDictionaryId(studentId)
                .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentIdStr));

        var wordId = addWordToDictionary.execute(
                new AddWordCommand(dictionaryId, wordValue, translations, pos));

        var lessonId = findActiveLesson.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalStateException("No active lesson for student " + studentIdStr));

        addWordToLesson.execute(new AddWordToLessonCommand(lessonId, wordId));
        log.info("Word '{}' added to lesson {} for student {}", wordValue, lessonId, studentIdStr);

        chat.updateState(ChatState.IN_LESSON);
        return LessonView.lessonKeyboard(WORD_POS_TRANSLATIONS.formatted(wordValue, WordView.posLabel(pos), String.join(", ", translations)));
    }

}
