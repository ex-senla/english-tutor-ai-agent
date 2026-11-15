package com.hydroyura.eta.chatbot.application.statemachine.transition.studentdictionary;

import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.dictionary.api.dictionary.ResetWordLearning;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.shared.api.DomainException;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ResetCbStudentDictionaryTransition implements Transition<Action.Callback> {

    private final StudentQuery studentQuery;

    private final ResetWordLearning resetWordLearning;

    private final StudentDictionarySupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var wordInfoId = new WordInfoId(UUID.fromString(callback.payload()));

        try {
            var studentId = StudentSupport.selectedStudentId(chat);
            var dictionaryId = studentQuery.getDictionaryId(studentId)
                    .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentId));
            resetWordLearning.execute(dictionaryId, wordInfoId);
        } catch (DomainException e) {
            log.warn("Word learning reset skipped for {}: {}", wordInfoId, e.getMessage());
        }

        return support.render(chat, callback.messageId());
    }
}
