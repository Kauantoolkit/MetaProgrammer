package com.example.meta_backend.service.generator.base;

import java.io.IOException;
import java.nio.file.*;

public class PomGenerator {

    private static final String BASE_DIR = "generated_app/";

    public void generatePom(String projectName) throws IOException {
        String baseDir = projectName + "/";
        String pom = """
            <?xml version="1.0" encoding="UTF-8"?>

            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

                <modelVersion>4.0.0</modelVersion>

                <parent>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-parent</artifactId>
                    <version>3.2.5</version>
                    <relativePath/>
                </parent>

                <groupId>com.metagen.backend</groupId>
                <artifactId>%s</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <name>%s</name>
                <description>Projeto backend gerado automaticamente</description>

                <properties>
                    <java.version>17</java.version>
                </properties>

                <dependencies>
                    <!-- Spring Boot Starter principal -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter</artifactId>
                    </dependency>

                    <!-- Web + JSON -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-web</artifactId>
                    </dependency>

                    <!-- JPA / Hibernate -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-data-jpa</artifactId>
                    </dependency>

                    <!-- Bean Validation -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-validation</artifactId>
                    </dependency>

                    <!-- Thymeleaf -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-thymeleaf</artifactId>
                    </dependency>

                    <!-- Spring Security -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-security</artifactId>
                    </dependency>

                    <!-- PostgreSQL Database -->
                    <dependency>
                        <groupId>org.postgresql</groupId>
                        <artifactId>postgresql</artifactId>
                        <scope>runtime</scope>
                    </dependency>

                    <!-- Flyway for database migrations -->
                    <dependency>
                        <groupId>org.flywaydb</groupId>
                        <artifactId>flyway-core</artifactId>
                    </dependency>

                    <!-- Testes -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-test</artifactId>
                        <scope>test</scope>
                    </dependency>
                </dependencies>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-maven-plugin</artifactId>
                        </plugin>
                    </plugins>
                </build>

            </project>
            """.formatted(projectName, projectName);

        Files.createDirectories(Paths.get(baseDir));
        Files.writeString(Paths.get(baseDir + "pom.xml"), pom);
    }
}
