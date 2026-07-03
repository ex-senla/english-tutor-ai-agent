package com.hydroyura.eta.chatbot.application;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.exercise.api.exercise.CheckExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.EndLesson;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import org.springframework.stereotype.Component;

@Component
public record CommandDispatcherConfig(
    RegisterTeacher registerTeacher,
    CreateStudentWithDictionary createStudentWithDictionary,
    StartLesson startLesson,
    AddWordToDictionary addWordToDictionary,
    AddWordToLesson addWordToLesson,
    EndLesson endLesson,
    FindTeacher findTeacher,
    StudentQuery studentQuery,
    GenerateExercise generateExercise,
    CheckExercise checkExercise
) {}
