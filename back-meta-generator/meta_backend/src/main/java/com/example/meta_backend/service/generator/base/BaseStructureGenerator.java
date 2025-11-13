package com.example.meta_backend.service.generator.base;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class BaseStructureGenerator {

    private static final String BASE_PACKAGE = "com/metagen/backend/generated/";
    private static final String BASE_DIR = "generated_app/src/main/java/" + BASE_PACKAGE;
    private static final String RESOURCES_DIR = "generated_app/src/main/resources/";

    public void createBaseStructure() throws IOException {
        // Remove "mainclass" da lista
        for (String sub : List.of("entity", "dto", "repository", "service", "controller", "security", "exception", "mapper")) {
            Files.createDirectories(Paths.get(BASE_DIR + sub));
        }

        // Cria pasta de recursos
        Files.createDirectories(Paths.get(RESOURCES_DIR));
    }
}
