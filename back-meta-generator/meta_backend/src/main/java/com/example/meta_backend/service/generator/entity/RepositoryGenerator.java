package com.example.meta_backend.service.generator.entity;

import java.io.IOException;
import java.nio.file.*;

public class RepositoryGenerator {

    public void generateRepository(String baseDir, String entityName) throws IOException {
        String code = """
            package com.metagen.backend.generated.repository;

            import com.metagen.backend.generated.entity.%s;
            import org.springframework.data.jpa.repository.JpaRepository;
            import org.springframework.data.domain.Page;
            import org.springframework.data.domain.Pageable;

            public interface %sRepository extends JpaRepository<%s, Long> {
                Page<%s> findAll(Pageable pageable);
                Page<%s> findByNameContainingIgnoreCase(String name, Pageable pageable);
            }
            """.formatted(entityName, entityName, entityName, entityName, entityName);

        Files.createDirectories(Paths.get(baseDir + "repository/"));
        Files.writeString(Paths.get(baseDir + "repository/" + entityName + "Repository.java"), code);
    }

    public void generateUserRepository(String baseDir) throws IOException {
        String code = """
            package com.metagen.backend.generated.repository;

            import com.metagen.backend.generated.entity.User;
            import org.springframework.data.jpa.repository.JpaRepository;
            import java.util.Optional;

            public interface UserRepository extends JpaRepository<User, Long> {
                Optional<User> findByUsername(String username);
            }
            """;

        Files.createDirectories(Paths.get(baseDir + "repository/"));
        Files.writeString(Paths.get(baseDir + "repository/UserRepository.java"), code);
    }
}
