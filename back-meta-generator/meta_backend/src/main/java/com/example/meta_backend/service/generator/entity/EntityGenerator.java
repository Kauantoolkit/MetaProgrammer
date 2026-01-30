package com.example.meta_backend.service.generator.entity;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class EntityGenerator {

    public void generateEntity(String baseDir, String name, List<Map<String, Object>> attrs, List<Map<String, Object>> rels, String appName) throws IOException {
        Path entityPath = Paths.get(baseDir + "entity/" + name + ".java");
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
          .append("@Entity\n@Table(name = \"" + name.toLowerCase() + "\")\n")
          .append("@EntityListeners(AuditingEntityListener.class)\n")
          .append("public class " + name + " {\n\n");

        for (Map<String, Object> attr : attrs) {
            String type = mapType((String) attr.get("type"));
            String field = (String) attr.get("name");

            if (field.equalsIgnoreCase("id")) {
                sb.append("    @Id\n")
                  .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
                type = "Long"; // Force Long for ID with IDENTITY strategy
            } else {
                sb.append("    @NotNull\n");
            }

            sb.append("    private " + type + " " + field + ";\n\n");
        }

        // relações (se houver)
        for (Map<String, Object> rel : rels) {
            String type = (String) rel.get("type");
            String target = (String) rel.get("target");
            String field = (String) rel.get("name");

            switch (type.toLowerCase()) {
                case "onetoone" ->
                    sb.append("    @OneToOne\n    private " + target + " " + field + ";\n\n");
                case "onetomany" ->
                    sb.append("    @OneToMany(mappedBy = \"" + name.toLowerCase() + "\")\n    private List<" + target + "> " + field + " = new ArrayList<>();\n\n");
                case "manytoone" ->
                    sb.append("    @ManyToOne\n    private " + target + " " + field + ";\n\n");
                case "manytomany" ->
                    sb.append("    @ManyToMany\n    private List<" + target + "> " + field + " = new ArrayList<>();\n\n");
            }
        }

        // Campos de auditoria
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

                // Getters and Setters
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
            case "date" -> "Date";
            default -> "String";
        };
    }
}
