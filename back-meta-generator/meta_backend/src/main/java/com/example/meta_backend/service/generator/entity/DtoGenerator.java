package com.example.meta_backend.service.generator.entity;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class DtoGenerator {
    private static final String BASE_DIR = "generated/";

    public void generateDTOs(String baseDir, String name, List<Map<String, Object>> attrs) throws IOException {
    StringBuilder dto = new StringBuilder();
    dto.append("package com.metagen.backend.generated.dto;\n\n")
       .append("public class ").append(name).append("Dto {\n\n");

    for (Map<String, Object> attr : attrs) {
        String type = (String) attr.get("type");
        String field = (String) attr.get("name");
        dto.append("    private ").append(mapType(type)).append(" ").append(field).append(";\n");
    }

    dto.append("}\n");

    // Cria pasta se não existir
    Files.createDirectories(Paths.get(baseDir + "dto/"));
    Files.writeString(Paths.get(baseDir + "dto/" + name + "Dto.java"), dto.toString());
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
