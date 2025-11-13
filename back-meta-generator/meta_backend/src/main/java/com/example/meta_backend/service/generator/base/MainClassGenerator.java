package com.example.meta_backend.service.generator.base;

import java.io.IOException;
import java.nio.file.*;

public class MainClassGenerator {
    private static final String BASE_DIR = "generated_app/src/main/java/com/metagen/backend/generated/";

    public void generateMainClass(String projectName) throws IOException {
    String className = toCamelCase(projectName) + "Application";

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
    Files.createDirectories(Paths.get(BASE_DIR));
    // Salva arquivo com o nome correto da classe pública
    Files.writeString(Paths.get(BASE_DIR + className + ".java"), content);
}


    private String toCamelCase(String s) {
        String[] parts = s.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
