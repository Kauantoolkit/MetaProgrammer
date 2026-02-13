package com.example.meta_backend.service.generator.entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class EntityGenerator {

    public void generateEntity(String baseDir,
                               String name,
                               List<Map<String, Object>> attrs,
                               List<Map<String, Object>> rels,
                               List<String> behaviors,
                               String appName) throws IOException {

        if (name == null || name.isBlank()) return;

        String className = sanitizeClassName(name);
        String tableName = sanitizeTableName(name);
        boolean hasTimestamps = behaviors != null && behaviors.contains("timestamps");
        boolean hasSoftDelete = behaviors != null && behaviors.contains("soft_delete");
        boolean hasVersioning = behaviors != null && behaviors.contains("versioning");

        Path entityPath = Paths.get(baseDir + "entity/" + className + ".java");
        Files.createDirectories(entityPath.getParent());

        StringBuilder sb = new StringBuilder();
        sb.append("package com.metagen.backend.generated.entity;\n\n")
          .append("import jakarta.persistence.*;\n")
          .append("import jakarta.validation.constraints.*;\n")
          .append("import lombok.*;\n");
        
        if (hasTimestamps || hasSoftDelete) {
            sb.append("import java.time.LocalDateTime;\n");
        }
        if (hasTimestamps) {
            sb.append("import org.springframework.data.annotation.CreatedDate;\n")
              .append("import org.springframework.data.annotation.LastModifiedDate;\n")
              .append("import org.springframework.data.jpa.domain.support.AuditingEntityListener;\n");
        }
        if (hasVersioning) {
            sb.append("import org.springframework.data.annotation.Version;\n");
        }
        
        sb.append("import java.util.*;\n\n")
          .append("@Data\n")
          .append("@Entity\n@Table(name = \"" + tableName + "\")\n");
        
        if (hasTimestamps) {
            sb.append("@EntityListeners(AuditingEntityListener.class)\n");
        }
        
        if (hasSoftDelete) {
            sb.append("@org.hibernate.annotations.SQLDelete(sql = \"UPDATE " + tableName + " SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?\")\n")
              .append("@org.hibernate.annotations.SQLRestriction(\"deleted_at IS NULL\")\n");
        }
        
        sb.append("public class " + className + " {\n\n");



        boolean hasId = false;

        if (attrs != null) {
            for (Map<String, Object> attr : attrs) {
                if (attr == null) continue;

                String originalFieldName = (String) attr.get("name");
                if (originalFieldName == null || originalFieldName.isBlank() || originalFieldName.equalsIgnoreCase("id")) continue;

                String field = sanitizeFieldName(originalFieldName);
                String type = mapType((String) attr.get("type"));
                String columnName = originalFieldName.toLowerCase().replaceAll("[^a-z0-9_]", "_");

                if (field.equalsIgnoreCase("id")) {
                    hasId = true;
                    sb.append("    @Id\n")
                      .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n")
                      .append("    private Long id;\n\n");
                    continue;
                }

                sb.append("    @NotNull\n")
                  .append("    @Column(name = \"").append(columnName).append("\")\n")
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

                // Convert relation type from "1:1", "1:N", "N:1", "N:N" to JPA format
                type = convertRelationType(type);

                target = sanitizeClassName(target);
                field = sanitizeFieldName(field);

                String targetTableName = target.toLowerCase();
                
                switch (type.toLowerCase()) {
                    case "onetoone" -> {
                        String fkColumnName = targetTableName + "_id";
                        sb.append("    @OneToOne\n")
                          .append("    @JoinColumn(name = \"").append(fkColumnName).append("\")\n")
                          .append("    private ").append(target).append(" ").append(field).append(";\n\n");
                    }

                    case "onetomany" ->
                        sb.append("    @OneToMany\n")
                          .append("    private List<").append(target).append("> ").append(field)
                          .append(" = new ArrayList<>();\n\n");

                    case "manytoone" -> {
                        String fkColumnName = targetTableName + "_id";
                        sb.append("    @ManyToOne\n")
                          .append("    @JoinColumn(name = \"").append(fkColumnName).append("\")\n")
                          .append("    private ").append(target).append(" ").append(field).append(";\n\n");
                    }

                    case "manytomany" ->
                        sb.append("    @ManyToMany\n")
                          .append("    private List<").append(target).append("> ").append(field)
                          .append(" = new ArrayList<>();\n\n");
                }

            }
        }


        if (hasTimestamps) {
            sb.append("    @CreatedDate\n")
              .append("    @Column(updatable = false)\n")
              .append("    private LocalDateTime createdAt;\n\n")
              .append("    @LastModifiedDate\n")
              .append("    private LocalDateTime updatedAt;\n\n");
        }

        if (hasSoftDelete) {
            sb.append("    @Column(name = \"deleted_at\")\n")
              .append("    private LocalDateTime deletedAt;\n\n");
        }

        if (hasVersioning) {
            sb.append("    @Version\n")
              .append("    @Column(name = \"version\")\n")
              .append("    private Long version;\n\n");
        }

        sb.append("}\n");



        Files.writeString(entityPath, sb.toString());
    }

    public void generateUsersEntity(String baseDir) throws IOException {
        Path usersPath = Paths.get(baseDir + "entity/Users.java");
        Files.createDirectories(usersPath.getParent());

        String userEntity = """
            package com.metagen.backend.generated.entity;

            import jakarta.persistence.*;
            import jakarta.validation.constraints.*;

            @Entity
            @Table(name = "users")
            public class Users {

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

        Files.writeString(usersPath, userEntity);
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

    /**
     * Converts relation type notation from "1:1", "1:N", "N:1", "N:N" to JPA format
     * "onetoone", "onetomany", "manytoone", "manytomany"
     */
    private String convertRelationType(String type) {
        if (type == null) return null;
        
        return switch (type.toUpperCase()) {
            case "1:1" -> "ONETOONE";
            case "1:N" -> "ONETOMANY";
            case "N:1" -> "MANYTOONE";
            case "N:N" -> "MANYTOMANY";
            default -> type.toUpperCase();
        };
    }
}
