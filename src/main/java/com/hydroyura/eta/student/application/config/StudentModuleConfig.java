package com.hydroyura.eta.student.application.config;

import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.student.CreateStudent;
import com.hydroyura.eta.student.api.lesson.EndLesson;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.student.application.usecase.AddWordToLessonUseCase;
import com.hydroyura.eta.student.application.usecase.CreateStudentUseCase;
import com.hydroyura.eta.student.application.usecase.EndLessonUseCase;
import com.hydroyura.eta.student.application.usecase.FindActiveLessonService;
import com.hydroyura.eta.student.application.usecase.StartLessonUseCase;
import com.hydroyura.eta.student.application.usecase.StudentQueryService;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StudentModuleConfig {

    @Bean
    CreateStudent createStudent(StudentRepository repository) {
        return new CreateStudentUseCase(repository);
    }

    @Bean
    StartLesson startLesson(LessonRepository repository) {
        return new StartLessonUseCase(repository);
    }

    @Bean
    AddWordToLesson addWordToLesson(LessonRepository repository) {
        return new AddWordToLessonUseCase(repository);
    }

    @Bean
    EndLesson endLesson(LessonRepository repository) {
        return new EndLessonUseCase(repository);
    }

    @Bean
    StudentQuery studentQuery(StudentRepository studentRepository,
            FindWords findWords,
            FindActiveLesson findActiveLesson) {
        return new StudentQueryService(studentRepository, findWords, findActiveLesson);
    }

    @Bean
    FindActiveLesson findActiveLesson(LessonRepository lessonRepository) {
        return new FindActiveLessonService(lessonRepository);
    }
}
