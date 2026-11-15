package com.hydroyura.eta.chatbot.application.statemachine.transition.word;

import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.lesson.LessonView;
import com.hydroyura.eta.chatbot.view.word.WordView;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.WordAlreadyExistsException;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.AddWordToLessonCommand;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.hydroyura.eta.chatbot.view.Messages.WORD_ALREADY_EXISTS;
import static com.hydroyura.eta.chatbot.view.Messages.WORD_POS_TRANSLATIONS;

/**
 * Общая логика добавления слова в словарь выбранного ученика и в активный урок.
 */
@Slf4j
@RequiredArgsConstructor
public class LessonWordSupport {

    private final StudentQuery studentQuery;

    private final AddWordToDictionary addWordToDictionary;

    private final FindActiveLesson findActiveLesson;

    private final AddWordToLesson addWordToLesson;

    /**
     * Добавляет слово в словарь и активный урок, не обрабатывая доменные исключения.
     */
    public WordInfoId addWordToDictionaryAndLesson(Chat chat, String wordValue, Set<String> translations,
            PartOfSpeech pos) {
        var studentId = StudentSupport.selectedStudentId(chat);
        var dictionaryId = studentQuery.getDictionaryId(studentId)
                .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentId));
        var wordId = addWordToDictionary.execute(new AddWordCommand(dictionaryId, wordValue, translations, pos));

        var lessonId = findActiveLesson.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalStateException("No active lesson for student " + studentId));
        addWordToLesson.execute(new AddWordToLessonCommand(lessonId, wordId));
        log.info("Word '{}' added to lesson {} for student {}", wordValue, lessonId, studentId);
        return wordId;
    }

    /**
     * Добавляет слово и возвращает готовый {@link ActionResult} с обработкой дубликата.
     */
    public ActionResult addWord(Chat chat, String wordValue, Set<String> translations, PartOfSpeech pos) {
        try {
            addWordToDictionaryAndLesson(chat, wordValue, translations, pos);
        } catch (WordAlreadyExistsException e) {
            chat.updateState(ChatState.IN_LESSON);
            return LessonView.lessonKeyboard(
                    WORD_ALREADY_EXISTS.formatted(wordValue, WordView.posLabel(pos), String.join(", ", translations)));
        }
        chat.updateState(ChatState.IN_LESSON);
        return LessonView.lessonKeyboard(
                WORD_POS_TRANSLATIONS.formatted(wordValue, WordView.posLabel(pos), String.join(", ", translations)));
    }
}
