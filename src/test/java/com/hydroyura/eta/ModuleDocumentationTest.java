package com.hydroyura.eta;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModuleDocumentationTest {

    @Test
    void generateComponentDiagram() throws Exception {
        var modules = ApplicationModules.of(EtaApplication.class);

        new org.springframework.modulith.docs.Documenter(modules)
                .writeDocumentation();

        System.out.println("Documentation generated in target/spring-modulith-docs/");
    }
}
