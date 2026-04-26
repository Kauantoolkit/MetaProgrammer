package com.example.meta_backend.service.generator.entity;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.springframework.stereotype.Component;

@Component
public class DtoGenerator {

    public void generateDTOs(String baseDir, String name, List<Map<String, Object>> attrs) throws IOException {
        String className = capitalizeFirstLetter(name);

        StringBuilder dto = new StringBuilder();
        dto.append("package com.metagen.backend.generated.dto;\n\n")
           .append("import jakarta.validation.constraints.*;\n\n")
           .append("public class ").append(className).append("Dto {\n\n");

        for (Map<String, Object> attr : attrs) {
            if (attr == null) continue;
            String type = (String) attr.get("type");
            String field = (String) attr.get("name");
            if (field == null || field.isBlank() || field.equalsIgnoreCase("id")) continue;

            @SuppressWarnings("unchecked")
            List<String> constraints = (List<String>) attr.getOrDefault("constraints", List.of());

            appendValidationAnnotations(dto, type, constraints);
            dto.append("    private ").append(mapType(type)).append(" ").append(field).append(";\n\n");
        }

        dto.append("}\n");

        Files.createDirectories(Paths.get(baseDir + "dto/"));
        Files.writeString(Paths.get(baseDir + "dto/" + className + "Dto.java"), dto.toString());
    }

    private void appendValidationAnnotations(StringBuilder sb, String type, List<String> constraints) {
        if (constraints == null) constraints = List.of();

        boolean notNull = constraints.contains("not_null") || constraints.contains("required");
        boolean unique  = constraints.contains("unique");
        boolean email   = constraints.contains("email") || "email".equalsIgnoreCase(type);

        if (notNull) sb.append("    @NotNull\n");
        if (unique)  sb.append("    // unique constraint enforced at the database level\n");
        if (email)   sb.append("    @Email\n");

        if (constraints.contains("positive") && isNumeric(type)) {
            sb.append("    @Positive\n");
        }
        if ("string".equalsIgnoreCase(type) || type == null) {
            if (constraints.contains("not_blank")) sb.append("    @NotBlank\n");
            if (constraints.contains("size_max")) sb.append("    @Size(max = 255)\n");
        }
    }

    private boolean isNumeric(String type) {
        if (type == null) return false;
        return switch (type.toLowerCase()) {
            case "int", "integer", "long", "double", "bigdecimal" -> true;
            default -> false;
        };
    }

    private String capitalizeFirstLetter(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String mapType(String type) {
        if (type == null) return "String";
        return switch (type.toLowerCase()) {
            case "string"        -> "String";
            case "int", "integer"-> "Integer";
            case "long"          -> "Long";
            case "boolean"       -> "Boolean";
            case "double"        -> "Double";
            case "bigdecimal"    -> "java.math.BigDecimal";
            default              -> "String";
        };
    }
}
