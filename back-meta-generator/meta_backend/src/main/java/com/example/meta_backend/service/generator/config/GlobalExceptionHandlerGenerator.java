package com.example.meta_backend.service.generator.config;

import java.io.IOException;
import java.nio.file.*;

import org.springframework.stereotype.Component;

@Component
public class GlobalExceptionHandlerGenerator {

    public void generateGlobalExceptionHandler(String baseDir) throws IOException {
        Path path = Paths.get(baseDir + "exception/");
        Files.createDirectories(path);

        String code = """
            package com.metagen.backend.generated.exception;

            import org.springframework.http.*;
            import org.springframework.web.bind.annotation.*;

            @RestControllerAdvice
            public class GlobalExceptionHandler {
                @ExceptionHandler(Exception.class)
                public ResponseEntity<String> handleException(Exception ex) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erro: " + ex.getMessage());
                }
            }
            """;

        Files.writeString(path.resolve("GlobalExceptionHandler.java"), code);
    }
}
