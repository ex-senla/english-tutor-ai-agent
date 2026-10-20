package com.hydroyura.eta.teacher.application.config.properties;

import com.hydroyura.eta.teacher.application.demo.DemoStudentsConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "eta.demo-students")
public class DemoStudentsSpringProperties implements DemoStudentsConfig {

    private boolean enabled = false;

    private int studentCount = 3;

    private int minWordsPerStudent = 3;

    private int maxWordsPerStudent = 5;
}
