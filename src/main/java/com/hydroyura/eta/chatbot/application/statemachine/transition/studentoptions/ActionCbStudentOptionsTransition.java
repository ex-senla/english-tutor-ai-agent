package com.hydroyura.eta.chatbot.application.statemachine.transition.studentoptions;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.exercise.ExerciseView;
import com.hydroyura.eta.chatbot.view.lesson.LessonView;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.lesson.StartLessonCommand;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import static com.hydroyura.eta.chatbot.view.Messages.LESSON;
import static com.hydroyura.eta.chatbot.view.Messages.LESSON_STARTED;
import static com.hydroyura.eta.chatbot.view.Messages.NO_STUDENTS;
import static com.hydroyura.eta.chatbot.view.Messages.USE_BUTTONS_BELOW;

/**
 * Обрабатывает все колбэки с префиксом «action» (payload: startlesson/details/exercise/back).
 */
@Slf4j
@RequiredArgsConstructor
public class ActionCbStudentOptionsTransition implements Transition<Action.Callback> {

    private final StartLesson startLesson;

    private final FindTeacher findTeacher;

    private final StudentQuery studentQuery;

    private final Map<String, BiFunction<Chat, Integer, ActionResult>> methodStrategy = Map.of(
            Callbacks.START_LESSON, this::startLesson,
            Callbacks.DETAILS, this::getStudentDetails,
            Callbacks.EXERCISE, this::defineExerciseType,
            Callbacks.BACK, this::studentsListEdit
    );

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var method = methodStrategy.get(callback.payload());

        if (method == null) {
            return new ActionResult.TextResponse(USE_BUTTONS_BELOW);
        }

        return method.apply(chat, callback.messageId());
    }

    private ActionResult defineExerciseType(Chat chat, int messageId) {
        chat.updateState(ChatState.AWAITING_EXERCISE_TYPE);
        return ExerciseView.exerciseTypeEdit(messageId);
    }

    private ActionResult getStudentDetails(Chat chat, int messageId) {
        chat.updateState(ChatState.STUDENT_DETAILS);
        return StudentView.studentDetails(messageId);
    }

    private ActionResult startLesson(Chat chat, int messageId) {
        var studentIdStr = (String) chat.getContext().get("selectedStudentId");
        var studentId = new StudentId(UUID.fromString(studentIdStr));
        var name = (String) chat.getContext().getOrDefault("selectedStudentName", "?");

        var lessonId = startLesson.execute(new StartLessonCommand(studentId, LESSON.formatted(name)));
        chat.getContext().put("activeLessonId", lessonId.value().toString());
        chat.updateState(ChatState.IN_LESSON);
        log.info("Lesson {} started for student {}", lessonId, studentIdStr);

        return LessonView.lessonKeyboard(LESSON_STARTED.formatted(name), messageId);
    }

    private ActionResult studentsListEdit(Chat chat, int messageId) {
        chat.updateState(ChatState.STUDENTS_LIST);

        var studentIds = findTeacher.getStudentIds(chat.getId().chatId());

        if (studentIds.isEmpty()) {
            return new ActionResult.EditMessageText(messageId, NO_STUDENTS, List.of());
        }

        var students = studentQuery.findStudentsByIds(studentIds);
        return StudentView.listEdit(messageId, students);
    }

}
