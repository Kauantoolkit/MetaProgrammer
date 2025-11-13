package com.example.meta_backend.service.generator.security;

import java.io.IOException;
import java.nio.file.*;

public class SecurityGenerator {

    public void generateSecurityClasses(String baseDir) throws IOException {
        Path secPath = Paths.get(baseDir + "security");
        Files.createDirectories(secPath);

        String config = """
            package com.metagen.backend.generated.security;

            import org.springframework.context.annotation.*;
            import org.springframework.security.config.annotation.web.builders.HttpSecurity;
            import org.springframework.security.web.SecurityFilterChain;

            @Configuration
            public class SecurityConfig {
                @Bean
                public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                    http.csrf(csrf -> csrf.disable())
                        .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                        );
                    return http.build();
                }
            }
            """;

        Files.writeString(secPath.resolve("SecurityConfig.java"), config);
    }
}
