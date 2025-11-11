package com.example.meta_backend.service;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class CodeGeneratorService {

    private static final String BASE_DIR = "src/main/java/com/metagen/backend/generated/";

    public void generateEntities(List<Map<String, Object>> entities) {
        try {
            for (String sub : List.of("entity", "dto", "repository", "service", "controller")) {
                Files.createDirectories(Paths.get(BASE_DIR + sub));
            }

            for (Map<String, Object> entity : entities) {
                String name = (String) entity.get("name");
                List<Map<String, Object>> attributes = (List<Map<String, Object>>) entity.get("attributes");
                List<Map<String, Object>> relations = (List<Map<String, Object>>) entity.get("relations");

                generateEntity(name, attributes, relations);
                generateRepository(name);
                generateDTOs(name, attributes);
                generateService(name);
                generateController(name, attributes);
            }

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar arquivos: " + e.getMessage(), e);
        }
    }

    // ================= ENTITY =================
    private void generateEntity(String name, List<Map<String, Object>> attrs, List<Map<String, Object>> rels) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.metagen.backend.generated.entity;\n\n")
          .append("import jakarta.persistence.*;\n")
          .append("import java.util.*;\n\n")
          .append("@Entity\n@Table(name = \"" + name.toLowerCase() + "\")\n")
          .append("public class " + name + " {\n\n");

        for (Map<String, Object> attr : attrs) {
            String type = mapType((String) attr.get("type"));
            String attrName = (String) attr.get("name");

            if (attrName.equalsIgnoreCase("id")) {
                sb.append("    @Id\n    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
            }
            sb.append("    private " + type + " " + attrName + ";\n\n");
        }

        for (Map<String, Object> rel : rels) {
            String target = (String) rel.get("target");
            String type = (String) rel.get("type");
            switch (type) {
                case "1:N" -> {
                    sb.append("    @OneToMany(mappedBy = \"" + name.toLowerCase() + "\")\n");
                    sb.append("    private List<" + target + "> " + target.toLowerCase() + "s;\n\n");
                }
                case "N:1" -> {
                    sb.append("    @ManyToOne\n");
                    sb.append("    @JoinColumn(name = \"" + target.toLowerCase() + "_id\")\n");
                    sb.append("    private " + target + " " + target.toLowerCase() + ";\n\n");
                }
                case "1:1" -> {
                    sb.append("    @OneToOne\n");
                    sb.append("    @JoinColumn(name = \"" + target.toLowerCase() + "_id\")\n");
                    sb.append("    private " + target + " " + target.toLowerCase() + ";\n\n");
                }
                case "N:N" -> {
                    sb.append("    @ManyToMany\n");
                    sb.append("    @JoinTable(name = \"" + name.toLowerCase() + "_" + target.toLowerCase() + "\", " +
                              "joinColumns = @JoinColumn(name = \"" + name.toLowerCase() + "_id\"), " +
                              "inverseJoinColumns = @JoinColumn(name = \"" + target.toLowerCase() + "_id\"))\n");
                    sb.append("    private List<" + target + "> " + target.toLowerCase() + "s;\n\n");
                }
            }
        }

        sb.append("    // Getters e Setters\n");
        for (Map<String, Object> attr : attrs) {
            String type = mapType((String) attr.get("type"));
            String attrName = (String) attr.get("name");
            String cap = capitalize(attrName);
            sb.append("    public " + type + " get" + cap + "() { return " + attrName + "; }\n");
            sb.append("    public void set" + cap + "(" + type + " " + attrName + ") { this." + attrName + " = " + attrName + "; }\n\n");
        }

        sb.append("}\n");
        Files.writeString(Paths.get(BASE_DIR + "entity/" + name + ".java"), sb.toString());
    }

    // ================= REPOSITORY =================
    private void generateRepository(String name) throws IOException {
        String code = """
            package com.metagen.backend.generated.repository;

            import org.springframework.data.jpa.repository.JpaRepository;
            import com.metagen.backend.generated.entity.%s;

            public interface %sRepository extends JpaRepository<%s, Long> {}
            """.formatted(name, name, name);

        Files.writeString(Paths.get(BASE_DIR + "repository/" + name + "Repository.java"), code);
    }

    // ================= DTOs =================
    private void generateDTOs(String name, List<Map<String, Object>> attrs) throws IOException {
        StringBuilder req = new StringBuilder("package com.metagen.backend.generated.dto;\n\npublic class " + name + "Request {\n");
        StringBuilder res = new StringBuilder("package com.metagen.backend.generated.dto;\n\npublic class " + name + "Response {\n");

        for (Map<String, Object> attr : attrs) {
            String field = (String) attr.get("name");
            String type = mapType((String) attr.get("type"));

            if (!field.equalsIgnoreCase("id"))
                req.append("    private " + type + " " + field + ";\n");
            res.append("    private " + type + " " + field + ";\n");
        }

        req.append("\n    // Getters e Setters\n");
        res.append("\n    // Getters e Setters\n");

        for (Map<String, Object> attr : attrs) {
            String field = (String) attr.get("name");
            String type = mapType((String) attr.get("type"));
            String cap = capitalize(field);

            if (!field.equalsIgnoreCase("id")) {
                req.append("    public " + type + " get" + cap + "() { return " + field + "; }\n");
                req.append("    public void set" + cap + "(" + type + " " + field + ") { this." + field + " = " + field + "; }\n\n");
            }

            res.append("    public " + type + " get" + cap + "() { return " + field + "; }\n");
            res.append("    public void set" + cap + "(" + type + " " + field + ") { this." + field + " = " + field + "; }\n\n");
        }

        req.append("}\n");
        res.append("}\n");

        Files.writeString(Paths.get(BASE_DIR + "dto/" + name + "Request.java"), req.toString());
        Files.writeString(Paths.get(BASE_DIR + "dto/" + name + "Response.java"), res.toString());
    }

    // ================= SERVICE =================
    private void generateService(String name) throws IOException {
        String code = """
            package com.metagen.backend.generated.service;

            import org.springframework.stereotype.Service;
            import java.util.*;
            import com.metagen.backend.generated.entity.%s;
            import com.metagen.backend.generated.repository.%sRepository;

            @Service
            public class %sService {

                private final %sRepository repository;

                public %sService(%sRepository repository) {
                    this.repository = repository;
                }

                public List<%s> findAll() { return repository.findAll(); }
                public Optional<%s> findById(Long id) { return repository.findById(id); }
                public %s save(%s entity) { return repository.save(entity); }
                public void delete(Long id) { repository.deleteById(id); }
            }
            """.formatted(name, name, name, name, name, name, name, name, name, name);

        Files.writeString(Paths.get(BASE_DIR + "service/" + name + "Service.java"), code);
    }

    // ================= CONTROLLER =================
    private void generateController(String name, List<Map<String, Object>> attrs) throws IOException {
        String lname = name.toLowerCase();

        // Gera mapeamento automático DTO <-> Entity
        StringBuilder mapToEntity = new StringBuilder();
        StringBuilder mapToResponse = new StringBuilder();

        for (Map<String, Object> attr : attrs) {
            String field = (String) attr.get("name");
            String cap = capitalize(field);

            if (!field.equalsIgnoreCase("id")) {
                mapToEntity.append("        entity.set" + cap + "(dto.get" + cap + "());\n");
            }
            mapToResponse.append("        dto.set" + cap + "(entity.get" + cap + "());\n");
        }

        String code = """
            package com.metagen.backend.generated.controller;

            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.*;
            import java.util.*;
            import com.metagen.backend.generated.dto.%sRequest;
            import com.metagen.backend.generated.dto.%sResponse;
            import com.metagen.backend.generated.entity.%s;
            import com.metagen.backend.generated.service.%sService;

            @RestController
            @RequestMapping("/api/%s")
            public class %sController {

                private final %sService service;

                public %sController(%sService service) {
                    this.service = service;
                }

                @GetMapping
                public ResponseEntity<List<%sResponse>> getAll() {
                    List<%sResponse> list = service.findAll().stream().map(this::toResponse).toList();
                    return ResponseEntity.ok(list);
                }

                @GetMapping("/{id}")
                public ResponseEntity<%sResponse> getById(@PathVariable Long id) {
                    return service.findById(id)
                            .map(this::toResponse)
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
                }

                @PostMapping
                public ResponseEntity<%sResponse> create(@RequestBody %sRequest dto) {
                    %s entity = toEntity(dto);
                    return ResponseEntity.ok(toResponse(service.save(entity)));
                }

                @PutMapping("/{id}")
                public ResponseEntity<%sResponse> update(@PathVariable Long id, @RequestBody %sRequest dto) {
                    %s entity = toEntity(dto);
                    entity.setId(id);
                    return ResponseEntity.ok(toResponse(service.save(entity)));
                }

                @DeleteMapping("/{id}")
                public ResponseEntity<Void> delete(@PathVariable Long id) {
                    service.delete(id);
                    return ResponseEntity.noContent().build();
                }

                private %sResponse toResponse(%s entity) {
                    %sResponse dto = new %sResponse();
            %s
                    return dto;
                }

                private %s toEntity(%sRequest dto) {
                    %s entity = new %s();
            %s
                    return entity;
                }
            }
            """.formatted(
                name, name, name, name, lname,
                name, name, name, name,
                name, name, name, name, name, name, name,
                name, name, name, name, name, name, name, name,
                mapToResponse, name, name, name, name, mapToEntity
            );

        Files.writeString(Paths.get(BASE_DIR + "controller/" + name + "Controller.java"), code);
    }

    // ================= HELPERS =================
    private String mapType(String t) {
        return switch (t) {
            case "string" -> "String";
            case "number" -> "Long";
            case "boolean" -> "Boolean";
            default -> "String";
        };
    }

    private String capitalize(String str) {
        return str == null || str.isEmpty() ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
