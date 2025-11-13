package com.example.meta_backend.service.generator.entity;

import java.io.IOException;
import java.nio.file.*;

public class RepositoryGenerator {

    public void generateRepository(String baseDir, String entityName) throws IOException {
        String code = """
            package com.metagen.backend.generated.repository;

            import com.metagen.backend.generated.entity.%s;
            import org.springframework.data.jpa.repository.JpaRepository;

            public interface %sRepository extends JpaRepository<%s, Long> {
            }
            """.formatted(entityName, entityName, entityName);

        Files.createDirectories(Paths.get(baseDir + "repository/"));
        Files.writeString(Paths.get(baseDir + "repository/" + entityName + "Repository.java"), code);
    }
}
