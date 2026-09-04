package com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.view.lesson.LessonView;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.student.api.lesson.EndLesson;
import com.hydroyura.eta.student.api.lesson.EndLessonCommand;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Ожидает и кнопку «🏁 Завершить урок», и команду /finishlesson —
 * логика не зависит от типа триггера, поэтому типизирован общим {@link Action}.
 */
@Slf4j
@RequiredArgsConstructor
public class FinishLessonInLessonTransition implements Transition<Action> {

    private final FindActiveLesson findActiveLesson;

    private final EndLesson endLesson;

    private final StudentQuery studentQuery;

    private final FindWords findWords;

    @Override
    public ActionResult transit(Chat chat, Action action) {
        var studentIdStr = (String) chat.getContext().get("selectedStudentId");
        var studentId = new StudentId(UUID.fromString(studentIdStr));

        var lessonId = findActiveLesson.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalStateException("No active lesson for student " + studentIdStr));

        var result = endLesson.execute(new EndLessonCommand(lessonId));
        log.info("Lesson {} ended for student {}", lessonId, studentIdStr);

        var dictionaryId = studentQuery.getDictionaryId(studentId)
                .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentIdStr));

        var wordValues = findWords.findByDictionaryId(dictionaryId).stream()
                .filter(wp -> result.wordIds().contains(wp.id()))
                .map(WordProjection::value)
                .toList();

        var date = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                .withZone(ZoneId.systemDefault())
                .format(result.startedAt());
        var duration = Duration.between(result.startedAt(), result.endedAt()).toMinutes();

        chat.updateState(ChatState.STUDENT_OPTIONS);

        var studentName = (String) chat.getContext().getOrDefault("selectedStudentName", "?");
        return LessonView.finishMenuWithKeyBoard(date, duration, wordValues, studentName);
    }

}
