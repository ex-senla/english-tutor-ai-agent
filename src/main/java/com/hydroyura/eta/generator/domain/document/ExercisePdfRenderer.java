package com.hydroyura.eta.generator.domain.document;

import java.util.Map;

public interface ExercisePdfRenderer {

    byte[] render(String templateName, Map<String, Object> model);
}
