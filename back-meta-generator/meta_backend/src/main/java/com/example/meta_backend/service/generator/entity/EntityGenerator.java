package com.example.meta_backend.service.generator.entity;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.springframework.stereotype.Component;

@Component
public class EntityGenerator {

    public void generateEntity(String baseDir,
                               String name,
                               List<Map<String, Object>> attrs,
                               List<Map<String, Object>> rels,
                               String appName) throws IOException {

        if (name == null || name.isBlank()) return;

        String className = sanitizeClassName(name);
        String tableName = sanitizeTableName(name);

        Path entityPath = Paths.get(baseDir + "entity/" + className + ".java");
        Files.createDirectories(entityPath.getParent());

        StringBuilder sb = new StringBuilder();
        sb.append("package com.metagen.backend.generated.entity;\n\n")
          .append("import jakarta.persistence.*;\n")
          .append("import jakarta.validation.constraints.*;\n")
          .append("import org.springframework.data.annotation.CreatedDate;\n")
          .append("import org.springframework.data.annotation.LastModifiedDate;\n")
          .append("import org.springframework.data.jpa.domain.support.AuditingEntityListener;\n")
          .append("import java.time.LocalDateTime;\n")
          .append("import java.util.*;\n\n")
          .append("@Entity\n@Table(name = \"" + tableName + "\")\n")
          .append("@EntityListeners(AuditingEntityListener.class)\n")
          .append("public class " + className + " {\n\n");

        boolean hasId = false;

        if (attrs != null) {
            for (Map<String, Object> attr : attrs) {
                if (attr == null) continue;

                String field = (String) attr.get("name");
                if (field == null || field.isBlank()) continue;

                field = sanitizeFieldName(field);
                String type = mapType((String) attr.get("type"));

                if (field.equalsIgnoreCase("id")) {
                    hasId = true;
                    sb.append("    @Id\n")
                      .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n")
                      .append("    private Long id;\n\n");
                    continue;
                }

                sb.append("    @NotNull\n")
                  .append("    private ").append(type).append(" ").append(field).append(";\n\n");
            }
        }

        if (!hasId) {
            sb.append("    @Id\n")
              .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n")
              .append("    private Long id;\n\n");
        }

        if (rels != null) {
            for (Map<String, Object> rel : rels) {
                if (rel == null) continue;

                String type = (String) rel.get("type");
                String target = (String) rel.get("target");
                String field = (String) rel.get("name");

                if (type == null || target == null || field == null) continue;

                target = sanitizeClassName(target);
                field = sanitizeFieldName(field);

                switch (type.toLowerCase()) {
                    case "onetoone" ->
                        sb.append("    @OneToOne\n")
                          .append("    private ").append(target).append(" ").append(field).append(";\n\n");

                    case "onetomany" ->
                        sb.append("    @OneToMany\n")
                          .append("    private List<").append(target).append("> ").append(field)
                          .append(" = new ArrayList<>();\n\n");

                    case "manytoone" ->
                        sb.append("    @ManyToOne\n")
                          .append("    private ").append(target).append(" ").append(field).append(";\n\n");

                    case "manytomany" ->
                        sb.append("    @ManyToMany\n")
                          .append("    private List<").append(target).append("> ").append(field)
                          .append(" = new ArrayList<>();\n\n");
                }
            }
        }

        sb.append("    @CreatedDate\n")
          .append("    @Column(updatable = false)\n")
          .append("    private LocalDateTime createdAt;\n\n")
          .append("    @LastModifiedDate\n")
          .append("    private LocalDateTime updatedAt;\n\n");

        sb.append("}\n");

        Files.writeString(entityPath, sb.toString());
    }

    public void generateUserEntity(String baseDir) throws IOException {
        Path userPath = Paths.get(baseDir + "entity/User.java");
        Files.createDirectories(userPath.getParent());

        String userEntity = """
            package com.metagen.backend.generated.entity;

            import jakarta.persistence.*;
            import jakarta.validation.constraints.*;

            @Entity
            @Table(name = "users")
            public class User {

                @Id
                @GeneratedValue(strategy = GenerationType.IDENTITY)
                private Long id;

                @NotNull
                @Column(unique = true)
                private String username;

                @NotNull
                private String password;

                @NotNull
                private String roles;

                public Long getId() { return id; }
                public void setId(Long id) { this.id = id; }

                public String getUsername() { return username; }
                public void setUsername(String username) { this.username = username; }

                public String getPassword() { return password; }
                public void setPassword(String password) { this.password = password; }

                public String getRoles() { return roles; }
                public void setRoles(String roles) { this.roles = roles; }
            }
            """;

        Files.writeString(userPath, userEntity);
    }

    private String mapType(String type) {
        if (type == null) return "String";
        return switch (type.toLowerCase()) {
            case "string" -> "String";
            case "int", "integer" -> "Integer";
            case "long" -> "Long";
            case "boolean" -> "Boolean";
            case "double" -> "Double";
            case "date" -> "LocalDateTime";
            default -> "String";
        };
    }

    private String sanitizeClassName(String name) {
        String cleaned = name.replaceAll("[^a-zA-Z0-9]", "");
        if (cleaned.isBlank()) return "Entity";
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private String sanitizeFieldName(String name) {
        String cleaned = name.replaceAll("[^a-zA-Z0-9]", "");
        if (cleaned.isBlank()) return "field";
        return Character.toLowerCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private String sanitizeTableName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }
}
