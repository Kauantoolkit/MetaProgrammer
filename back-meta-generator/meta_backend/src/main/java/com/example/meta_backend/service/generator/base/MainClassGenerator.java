package com.example.meta_backend.service.generator.base;

import java.io.IOException;
import java.nio.file.*;

import org.springframework.stereotype.Component;

@Component
public class MainClassGenerator {
    public void generateMainClass(String projectName) throws IOException {
        String baseDir = projectName + "/src/main/java/com/metagen/backend/generated/";
    String className = AppNameUtils.toClassName(projectName) + "Application";

    String content = """
        package com.metagen.backend.generated;

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;

        @SpringBootApplication
        public class %s {
            public static void main(String[] args) {
                SpringApplication.run(%s.class, args);
            }
        }
        """.formatted(className, className);

    // Cria diretório base
    Files.createDirectories(Paths.get(baseDir));
    // Salva arquivo com o nome correto da classe pública
    Files.writeString(Paths.get(baseDir + className + ".java"), content);
}


// Using AppNameUtils.toClassName() instead - handles both hyphens and underscores
}
