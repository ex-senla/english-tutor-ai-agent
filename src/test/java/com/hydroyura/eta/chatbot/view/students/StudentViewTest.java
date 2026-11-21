package com.hydroyura.eta.chatbot.view.students;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.Messages;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentViewTest {

    @Test
    void detailsKeyboardShouldContainDeleteButton() {
        var details = new StudentDetails("Anna", new DictionaryStats(0, 0, 0, 0), false, CefrLevel.B1);

        var result = StudentView.studentDetails(1, details);
        var keyboard = ((ActionResult.EditMessageText) result).keyboard();

        var deleteButton = keyboard.stream()
                .flatMap(List::stream)
                .filter(b -> b.text().equals(Buttons.DELETE_STUDENT))
                .findFirst()
                .orElseThrow();

        assertThat(deleteButton.callbackData()).isEqualTo(Callbacks.STUDENT_DELETE);
    }

    @Test
    void deleteConfirmationShouldContainNameAndButtons() {
        var result = StudentView.deleteConfirmation(42, "Anna");
        var edit = (ActionResult.EditMessageText) result;

        assertThat(edit.text()).contains("Anna");
        var buttons = edit.keyboard().stream().flatMap(List::stream).toList();
        assertThat(buttons).extracting(ActionResult.InlineButton::text)
                .containsExactly(Buttons.DELETE_CONFIRM, Buttons.DELETE_CANCEL);
        assertThat(buttons.get(0).callbackData()).isEqualTo(Callbacks.STUDENT_DELETE_CONFIRM);
        assertThat(buttons.get(1).callbackData()).isEqualTo(Callbacks.BACK + ":" + Callbacks.MAIN);
    }

    @Test
    void deletedStudentsListShouldShowEmptyMessageWithoutKeyboard() {
        var result = StudentView.deletedStudentsList(1, List.of());
        var edit = (ActionResult.EditMessageText) result;

        assertThat(edit.text()).isEqualTo(Messages.NO_STUDENTS);
        assertThat(edit.keyboard()).isEmpty();
    }

    @Test
    void deletedStudentsListShouldShowRemainingStudents() {
        var student = new StudentInfo(StudentId.generate(), "Bob");

        var result = StudentView.deletedStudentsList(1, List.of(student));
        var edit = (ActionResult.EditMessageText) result;

        assertThat(edit.text()).isEqualTo(Messages.STUDENT_DELETED);
        var buttons = edit.keyboard().stream().flatMap(List::stream).toList();
        assertThat(buttons).extracting(ActionResult.InlineButton::text).contains("Bob", Buttons.BACK);
    }

    @Test
    void studentNotFoundOrDeletedShouldShowMessageAndList() {
        var student = new StudentInfo(StudentId.generate(), "Bob");

        var result = StudentView.studentNotFoundOrDeleted(1, List.of(student));
        var edit = (ActionResult.EditMessageText) result;

        assertThat(edit.text()).isEqualTo(Messages.STUDENT_NOT_FOUND_OR_DELETED);
        assertThat(edit.keyboard()).isNotEmpty();
    }

    @Test
    void studentsListMenuShouldContainNewStudentButton() {
        var student = new StudentInfo(StudentId.generate(), "Bob");

        var result = StudentView.studentsListMenu(List.of(student));
        var keyboard = ((ActionResult.TextWithInlineKeyboard) result).keyboard();

        var newStudentButton = keyboard.stream()
                .flatMap(List::stream)
                .filter(b -> b.text().equals(Buttons.NEW_STUDENT))
                .findFirst()
                .orElseThrow();

        assertThat(newStudentButton.callbackData()).isEqualTo(Callbacks.NEW_STUDENT);
    }
}
