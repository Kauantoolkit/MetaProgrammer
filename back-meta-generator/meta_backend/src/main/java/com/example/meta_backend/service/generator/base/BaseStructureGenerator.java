package com.example.meta_backend.service.generator.base;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Component
public class BaseStructureGenerator {

    private static final String BASE_PACKAGE = "com/metagen/backend/generated/";

    public void createBaseStructure(String appName) throws IOException {
        String baseDir = appName + "/src/main/java/" + BASE_PACKAGE;
        String resourcesDir = appName + "/src/main/resources/";
        String dbMigrationDir = appName + "/src/main/resources/db/migration/";

        for (String sub : List.of(
                "entity", "dto", "repository", "service",
                "controller", "security", "exception", "mapper"
        )) {
            Files.createDirectories(Paths.get(baseDir + sub));
        }

        Files.createDirectories(Paths.get(resourcesDir));
        Files.createDirectories(Paths.get(dbMigrationDir));
    }

    public void generateFlywayMigration(String appName) throws IOException {
        String dbMigrationDir = appName + "/src/main/resources/db/migration/";
        String migrationFile = "V1__Create_admin_user.sql";

        String migration = """
            -- Create admin user
            INSERT INTO users (username, password, roles) VALUES
            ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN')
            ON CONFLICT (username) DO NOTHING;
            """;

        Files.writeString(Paths.get(dbMigrationDir + migrationFile), migration);
    }
}
