package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.teacher.api.teacher.CompleteOnboarding;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
public class CompleteOnboardingUseCase implements CompleteOnboarding {

    private final TeacherRepository teacherRepository;

    @Override
    public void execute(TeacherId teacherId) {
        Objects.requireNonNull(teacherId, "TeacherId must not be null");
        var teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalStateException("Teacher not found: " + teacherId));
        teacher.completeOnboarding();
        teacherRepository.save(teacher);
    }
}
