package com.example.meta_backend.service.generator.config;

import java.io.IOException;
import java.nio.file.*;

import org.springframework.stereotype.Component;

@Component
public class ApplicationPropertiesGenerator {
    public void generateApplicationProperties(String projectName, String adminPassword, String javaClassName) throws IOException {
        String baseDir = projectName + "/src/main/resources/";
        Files.createDirectories(Paths.get(baseDir));

        // Use javaClassName for spring.application.name (no hyphens allowed)
        String props = """
            spring.application.name=%s
            server.port=8081
            spring.datasource.url=jdbc:postgresql://localhost:5432/%sdb
            spring.datasource.driverClassName=org.postgresql.Driver
            spring.datasource.username=postgres
            spring.datasource.password=postgres
            spring.jpa.hibernate.ddl-auto=validate
            spring.flyway.enabled=true
            spring.flyway.baseline-on-migrate=true
            spring.flyway.locations=classpath:db/migration
            server.error.include-message=always
            server.error.include-stacktrace=never
            
            # Admin user configuration
            admin.password=%s
            """.formatted(javaClassName, javaClassName.toLowerCase(), adminPassword);

        Files.writeString(Paths.get(baseDir + "application.properties"), props);
    }
}
