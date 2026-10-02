package com.hydroyura.eta.generator.application.config;

import com.hydroyura.eta.generator.api.document.GenerateExercisePdf;
import com.hydroyura.eta.generator.application.usecase.GenerateExercisePdfUseCase;
import com.hydroyura.eta.generator.domain.document.ExercisePdfRenderer;
import com.hydroyura.eta.generator.infrastructure.pdf.ThymeleafPdfRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneratorModuleConfig {

    @Bean
    ExercisePdfRenderer exercisePdfRenderer() {
        return new ThymeleafPdfRenderer();
    }

    @Bean
    GenerateExercisePdf generateExercisePdf(ExercisePdfRenderer renderer) {
        return new GenerateExercisePdfUseCase(renderer);
    }
}
