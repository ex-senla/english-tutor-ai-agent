package com.hydroyura.eta.chatbot.application.statemachine.transition.studentoptions;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.item.exercise.ExerciseItem;
import com.hydroyura.eta.chatbot.item.lesson.LessonItem;
import com.hydroyura.eta.chatbot.item.students.StudentItem;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.lesson.StartLessonCommand;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Обрабатывает все колбэки с префиксом «action» (payload: startlesson/details/exercise/back).
 */
@Slf4j
@RequiredArgsConstructor
public class ActionCbStudentOptionsTransition implements Transition<Action.Callback> {

    private final StartLesson startLesson;

    private final FindTeacher findTeacher;

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        return switch (callback.payload()) {
            case "startlesson" -> startLesson(chat, callback.messageId());
            case "details" -> {
                chat.updateState(ChatState.STUDENT_DETAILS);
                yield new ActionResult.EditMessageText(callback.messageId(), "Детали ученика (TODO)",
                        List.of(List.of(new ActionResult.InlineButton("◀ Back", "details:back"))));
            }
            case "exercise" -> {
                chat.updateState(ChatState.AWAITING_EXERCISE_TYPE);
                yield ExerciseItem.exerciseTypeEdit(callback.messageId());
            }
            case "back" -> {
                chat.updateState(ChatState.STUDENTS_LIST);
                yield studentsListEdit(chat, callback.messageId());
            }
            default -> new ActionResult.TextResponse("Используйте кнопки ниже");
        };
    }

    private ActionResult startLesson(Chat chat, int messageId) {
        var studentIdStr = (String) chat.getContext().get("selectedStudentId");
        var studentId = new StudentId(UUID.fromString(studentIdStr));
        var name = (String) chat.getContext().getOrDefault("selectedStudentName", "?");

        var lessonId = startLesson.execute(new StartLessonCommand(studentId, "Урок " + name));
        chat.getContext().put("activeLessonId", lessonId.value().toString());
        chat.updateState(ChatState.IN_LESSON);
        log.info("Lesson {} started for student {}", lessonId, studentIdStr);

        return LessonItem.lessonKeyboard("Урок начат для " + name + "!", messageId);
    }

    private ActionResult studentsListEdit(Chat chat, int messageId) {
        var studentIds = findTeacher.getStudentIds(chat.getId().chatId());

        if (studentIds.isEmpty()) {
            return new ActionResult.EditMessageText(messageId,
                    "У вас пока нет учеников.\n➕ Новый студент — добавить ученика", List.of());
        }

        var students = studentQuery.findStudentsByIds(studentIds);
        return StudentItem.listEdit(messageId, students);
    }

    @Override
    public String getName() {
        return "ActionCbStudentOptionsTransition";
    }
}
