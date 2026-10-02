package com.hydroyura.eta.generator.application.view;

import java.util.List;
import lombok.Value;

@Value
public class ExerciseItemView {

    String sentence;

    List<String> options;
}
