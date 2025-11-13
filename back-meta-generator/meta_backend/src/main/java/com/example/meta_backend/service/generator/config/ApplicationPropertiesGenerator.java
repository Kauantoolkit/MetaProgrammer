package com.example.meta_backend.service.generator.config;

import java.io.IOException;
import java.nio.file.*;

public class ApplicationPropertiesGenerator {
    private static final String BASE_DIR = "generated_app/src/main/resources/";


    public void generateApplicationProperties(String projectName) throws IOException {
        Files.createDirectories(Paths.get(BASE_DIR));

        String props = """
            spring.application.name=%s
            server.port=8081
            spring.datasource.url=jdbc:h2:mem:%sdb
            spring.datasource.driverClassName=org.h2.Driver
            spring.datasource.username=sa
            spring.datasource.password=
            spring.jpa.hibernate.ddl-auto=update
            spring.h2.console.enabled=true
            """.formatted(projectName, projectName.toLowerCase());

        Files.writeString(Paths.get(BASE_DIR + "application.properties"), props);
    }
}
