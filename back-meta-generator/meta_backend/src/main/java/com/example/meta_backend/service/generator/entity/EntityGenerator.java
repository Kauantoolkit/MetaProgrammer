package com.example.meta_backend.service.generator.entity;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class EntityGenerator {

    public void generateEntity(String baseDir, String name, List<Map<String, Object>> attrs, List<Map<String, Object>> rels) throws IOException {
        Path entityPath = Paths.get(baseDir + "entity/" + name + ".java");
        Files.createDirectories(entityPath.getParent());

        StringBuilder sb = new StringBuilder();
        sb.append("package com.metagen.backend.generated.entity;\n\n")
          .append("import jakarta.persistence.*;\n")
          .append("import jakarta.validation.constraints.*;\n")
          .append("import java.util.*;\n\n")
          .append("@Entity\n@Table(name = \"" + name.toLowerCase() + "\")\n")
          .append("public class " + name + " {\n\n");

        for (Map<String, Object> attr : attrs) {
            String type = mapType((String) attr.get("type"));
            String field = (String) attr.get("name");

            if (field.equalsIgnoreCase("id")) {
                sb.append("    @Id\n")
                  .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
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

        sb.append("}\n");

        Files.writeString(entityPath, sb.toString());
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
