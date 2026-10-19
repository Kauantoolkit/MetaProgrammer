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

            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.springframework.http.*;
            import org.springframework.web.bind.annotation.*;
            import org.springframework.web.bind.MethodArgumentNotValidException;
            import java.util.stream.Collectors;

            @RestControllerAdvice
            public class GlobalExceptionHandler {

                private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

                @ExceptionHandler(MethodArgumentNotValidException.class)
                public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
                    String msg = ex.getBindingResult().getFieldErrors().stream()
                            .map(e -> e.getField() + ": " + e.getDefaultMessage())
                            .collect(Collectors.joining(", "));
                    return ResponseEntity.badRequest().body(msg);
                }

                @ExceptionHandler(Exception.class)
                public ResponseEntity<String> handleException(Exception ex) {
                    log.error("Unhandled exception", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Ocorreu um erro interno. Tente novamente mais tarde.");
                }
            }
            """;

        Files.writeString(path.resolve("GlobalExceptionHandler.java"), code);
    }
}
