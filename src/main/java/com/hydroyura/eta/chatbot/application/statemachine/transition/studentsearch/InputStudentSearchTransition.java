package com.hydroyura.eta.chatbot.application.statemachine.transition.studentsearch;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotSearchProperties;
import com.hydroyura.eta.chatbot.application.statemachine.transition.ActingTeacherResolver;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.student.api.student.SearchStudentsQuery;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InputStudentSearchTransition implements Transition<Action.Input> {

    private final ActingTeacherResolver resolver;

    private final StudentQuery studentQuery;

    private final ChatbotSearchProperties properties;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var fragment = input.text().trim();
        if (fragment.isEmpty()) {
            return StudentView.searchNothingFound();
        }

        var studentIds = resolver.actingStudentIds(chat, chat.getId().chatId());
        var students = studentQuery.searchByNameFragment(new SearchStudentsQuery(studentIds, fragment));

        if (students.isEmpty()) {
            return StudentView.searchNothingFound();
        }

        return StudentView.searchResults(students, properties.getSuggestionsLimit());
    }
}
