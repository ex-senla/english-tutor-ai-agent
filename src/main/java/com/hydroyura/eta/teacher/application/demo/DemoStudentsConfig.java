package com.hydroyura.eta.teacher.application.demo;

public interface DemoStudentsConfig {

    boolean isEnabled();

    int getStudentCount();

    int getMinWordsPerStudent();

    int getMaxWordsPerStudent();
}
