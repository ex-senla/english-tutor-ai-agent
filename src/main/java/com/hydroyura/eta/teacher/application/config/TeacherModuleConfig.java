package com.hydroyura.eta.teacher.application.config;

import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionary;
import com.hydroyura.eta.student.api.student.CreateStudent;
import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionary;
import com.hydroyura.eta.student.api.student.CreateStudent;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.application.usecase.CreateStudentWithDictionaryUseCase;
import com.hydroyura.eta.teacher.application.usecase.FindTeacherService;
import com.hydroyura.eta.teacher.application.usecase.RegisterTeacherUseCase;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TeacherModuleConfig {

    @Bean
    RegisterTeacher registerTeacher(TeacherRepository repository) {
        return new RegisterTeacherUseCase(repository);
    }

    @Bean
    FindTeacher findTeacher(TeacherRepository repository) {
        return new FindTeacherService(repository);
    }

    @Bean
    CreateStudentWithDictionary createStudentWithDictionary(
            TeacherRepository teacherRepository,
            StudentQuery studentQuery,
            CreateDictionary createDictionary,
            CreateStudent createStudent
    ) {
        return new CreateStudentWithDictionaryUseCase(teacherRepository, studentQuery, createDictionary, createStudent);
    }
}
