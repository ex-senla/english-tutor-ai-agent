package com.hydroyura.eta.teacher.application.config;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionary;
import com.hydroyura.eta.student.api.student.CreateStudent;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.application.config.properties.DemoStudentsSpringProperties;
import com.hydroyura.eta.teacher.application.demo.DemoStudentsConfig;
import com.hydroyura.eta.teacher.application.demo.DemoStudentsSeeder;
import com.hydroyura.eta.teacher.application.usecase.CreateStudentWithDictionaryUseCase;
import com.hydroyura.eta.teacher.application.usecase.FindTeacherService;
import com.hydroyura.eta.teacher.application.usecase.RegisterTeacherUseCase;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import java.util.Random;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DemoStudentsSpringProperties.class)
public class TeacherModuleConfig {

    @Bean
    RegisterTeacher registerTeacher(TeacherRepository repository, DemoStudentsSeeder demoStudentsSeeder) {
        return new RegisterTeacherUseCase(repository, demoStudentsSeeder);
    }

    @Bean
    FindTeacher findTeacher(TeacherRepository repository) {
        return new FindTeacherService(repository);
    }

    @Bean
    DemoStudentsSeeder demoStudentsSeeder(
            DemoStudentsConfig config,
            CreateStudentWithDictionary createStudentWithDictionary,
            StudentQuery studentQuery,
            AddWordToDictionary addWordToDictionary) {
        return new DemoStudentsSeeder(config, createStudentWithDictionary, studentQuery, addWordToDictionary,
                new Random());
    }

    @Bean
    CreateStudentWithDictionary createStudentWithDictionary(
            TeacherRepository teacherRepository,
            StudentQuery studentQuery,
            CreateDictionary createDictionary,
            CreateStudent createStudent) {
        return new CreateStudentWithDictionaryUseCase(teacherRepository, studentQuery, createDictionary, createStudent);
    }
}
