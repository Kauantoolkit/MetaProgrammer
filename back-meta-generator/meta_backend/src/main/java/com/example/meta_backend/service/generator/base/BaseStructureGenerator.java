package com.example.meta_backend.service.generator.base;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class BaseStructureGenerator {

    private static final String BASE_PACKAGE = "com/metagen/backend/generated/";

    public void createBaseStructure(String appName) throws IOException {
        Path rootDir = Paths.get(System.getProperty("user.dir"));
        String baseDir = rootDir.resolve(appName + "/src/main/java/" + BASE_PACKAGE).toString();
        String resourcesDir = rootDir.resolve(appName + "/src/main/resources/").toString();
        // Remove "mainclass" da lista
        for (String sub : List.of("entity", "dto", "repository", "service", "controller", "security", "exception", "mapper")) {
            Files.createDirectories(Paths.get(baseDir + sub));
        }

        // Cria pasta de recursos
        Files.createDirectories(Paths.get(resourcesDir));
    }
}
