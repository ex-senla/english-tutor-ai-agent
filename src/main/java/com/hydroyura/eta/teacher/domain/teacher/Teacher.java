package com.hydroyura.eta.teacher.domain.teacher;

import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

@Getter
@Entity
public class Teacher {

    @Identity
    private TeacherId id;

    private String name;

    private boolean admin;

    private boolean onboardingCompleted;

    private Identifiers identifiers;

    @Association
    private Set<StudentId> studentIds = new HashSet<>();

    private Teacher() {
    }

    public static Teacher create(TeacherId id, String name) {
        Objects.requireNonNull(id, "TeacherId must not be null");
        Objects.requireNonNull(name, "Name must not be null");

        var teacher = new Teacher();
        teacher.id = id;
        teacher.name = name;
        teacher.admin = false;
        teacher.onboardingCompleted = false;
        teacher.identifiers = new Identifiers();
        return teacher;
    }

    public static Teacher reconstitute(TeacherId id, String name, boolean admin, boolean onboardingCompleted) {
        var teacher = create(id, name);
        teacher.admin = admin;
        teacher.onboardingCompleted = onboardingCompleted;
        return teacher;
    }

    public void completeOnboarding() {
        this.onboardingCompleted = true;
    }

    public void addStudent(StudentId studentId) {
        Objects.requireNonNull(studentId, "StudentId must not be null");
        this.studentIds.add(studentId);
    }

    public Set<StudentId> getStudentIds() {
        return Collections.unmodifiableSet(studentIds);
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }
}
