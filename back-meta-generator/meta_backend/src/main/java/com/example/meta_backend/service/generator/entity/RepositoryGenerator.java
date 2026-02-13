package com.example.meta_backend.service.generator.entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

@Component
public class RepositoryGenerator {

    public void generateRepository(String baseDir, String entityName) throws IOException {
        String className = capitalizeFirstLetter(entityName);
        
        boolean hasNameField = hasNameField(className);

        String searchMethod = "";
        if (hasNameField) {
            searchMethod = """
                    
                    Page<%s> findByNameContainingIgnoreCase(String name, Pageable pageable);
                    """.formatted(className);
        }

        String code = """
            package com.metagen.backend.generated.repository;

            import com.metagen.backend.generated.entity.%s;
            import org.springframework.data.jpa.repository.JpaRepository;
            import org.springframework.data.domain.Page;
            import org.springframework.data.domain.Pageable;

            public interface %sRepository extends JpaRepository<%s, Long> {
            %s
            }
            """.formatted(className, className, className, searchMethod);

        Files.createDirectories(Paths.get(baseDir + "repository/"));
        Files.writeString(
                Paths.get(baseDir + "repository/" + className + "Repository.java"),
                code
        );
    }
    
    private String capitalizeFirstLetter(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private boolean hasNameField(String entityName) {
        try {
            Class<?> clazz = Class.forName(
                    "com.metagen.backend.generated.entity." + entityName
            );
            clazz.getDeclaredField("name");
            return true;
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            return false;
        }
    }

    public void generateUsersRepository(String baseDir) throws IOException {
        String code = """
            package com.metagen.backend.generated.repository;

            import com.metagen.backend.generated.entity.Users;
            import org.springframework.data.jpa.repository.JpaRepository;
            import java.util.Optional;

            public interface UsersRepository extends JpaRepository<Users, Long> {
                Optional<Users> findByUsername(String username);
            }
            """;

        Files.createDirectories(Paths.get(baseDir + "repository/"));
        Files.writeString(
                Paths.get(baseDir + "repository/UsersRepository.java"),
                code
        );
    }
}
