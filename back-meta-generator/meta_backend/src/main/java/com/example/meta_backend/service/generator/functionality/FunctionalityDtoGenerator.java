package com.example.meta_backend.service.generator.functionality;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.springframework.stereotype.Component;

@Component
public class FunctionalityDtoGenerator {

    public void generateFunctionalityDTOs(String baseDir, List<Map<String, Object>> functionalities) throws IOException {
        for (Map<String, Object> functionality : functionalities) {
            String funcName = (String) functionality.get("name");

            // Generate Request DTO
            generateRequestDto(baseDir, funcName, (List<Map<String, Object>>) functionality.getOrDefault("input", List.of()));

            // Generate Response DTO
            generateResponseDto(baseDir, funcName, (List<Map<String, Object>>) functionality.getOrDefault("output", List.of()));
        }
    }

    private void generateRequestDto(String baseDir, String funcName, List<Map<String, Object>> inputFields) throws IOException {
        String className = funcName + "RequestDto";
        StringBuilder dto = new StringBuilder();
        dto.append("package com.metagen.backend.generated.dto.functionality;\n\n")
           .append("public class ").append(className).append(" {\n\n");

        int fieldIndex = 1;
        for (Map<String, Object> field : inputFields) {
            String type = (String) field.get("type");
            String name = (String) field.get("name");
            if (name == null || name.trim().isEmpty()) {
                name = "inputField" + fieldIndex;
            }
            dto.append("    private ").append(mapType(type)).append(" ").append(name).append(";\n");
            fieldIndex++;
        }

        // Add getters and setters
        fieldIndex = 1;
        for (Map<String, Object> field : inputFields) {
            String type = (String) field.get("type");
            String name = (String) field.get("name");
            if (name == null || name.trim().isEmpty()) {
                name = "inputField" + fieldIndex;
            }
            String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);

            dto.append("\n    public ").append(mapType(type)).append(" get").append(capitalizedName).append("() {\n")
               .append("        return ").append(name).append(";\n")
               .append("    }\n\n")
               .append("    public void set").append(capitalizedName).append("(").append(mapType(type)).append(" ").append(name).append(") {\n")
               .append("        this.").append(name).append(" = ").append(name).append(";\n")
               .append("    }\n");
            fieldIndex++;
        }

        dto.append("}\n");

        // Create directory if not exists
        Files.createDirectories(Paths.get(baseDir + "dto/functionality/"));
        Files.writeString(Paths.get(baseDir + "dto/functionality/" + className + ".java"), dto.toString());
    }

    private void generateResponseDto(String baseDir, String funcName, List<Map<String, Object>> outputFields) throws IOException {
        String className = funcName + "ResponseDto";
        StringBuilder dto = new StringBuilder();
        dto.append("package com.metagen.backend.generated.dto.functionality;\n\n")
           .append("public class ").append(className).append(" {\n\n");

        for (Map<String, Object> field : outputFields) {
            String type = (String) field.get("type");
            String name = (String) field.get("name");
            dto.append("    private ").append(mapType(type)).append(" ").append(name).append(";\n");
        }

        // Add getters and setters
        for (Map<String, Object> field : outputFields) {
            String type = (String) field.get("type");
            String name = (String) field.get("name");
            String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);

            dto.append("\n    public ").append(mapType(type)).append(" get").append(capitalizedName).append("() {\n")
               .append("        return ").append(name).append(";\n")
               .append("    }\n\n")
               .append("    public void set").append(capitalizedName).append("(").append(mapType(type)).append(" ").append(name).append(") {\n")
               .append("        this.").append(name).append(" = ").append(name).append(";\n")
               .append("    }\n");
        }

        dto.append("}\n");

        // Create directory if not exists
        Files.createDirectories(Paths.get(baseDir + "dto/functionality/"));
        Files.writeString(Paths.get(baseDir + "dto/functionality/" + className + ".java"), dto.toString());
    }

    private String mapType(String type) {
        return switch (type.toLowerCase()) {
            case "string" -> "String";
            case "int", "integer" -> "Integer";
            case "long" -> "Long";
            case "boolean" -> "Boolean";
            case "double" -> "Double";
            default -> "String";
        };
    }
}
