package com.example.meta_backend.service.generator.config;

import java.io.IOException;
import java.nio.file.*;

public class ApplicationPropertiesGenerator {
    public void generateApplicationProperties(String projectName) throws IOException {
        String baseDir = projectName + "/src/main/resources/";
        Files.createDirectories(Paths.get(baseDir));

        String props = """
            spring.application.name=%s
            server.port=8081
            spring.datasource.url=jdbc:postgresql://localhost:5432/%sdb
            spring.datasource.driverClassName=org.postgresql.Driver
            spring.datasource.username=postgres
            spring.datasource.password=postgres
            spring.jpa.hibernate.ddl-auto=update
            server.error.include-message=always
            server.error.include-stacktrace=never
            """.formatted(projectName, projectName.toLowerCase());

        Files.writeString(Paths.get(baseDir + "application.properties"), props);
    }
}
