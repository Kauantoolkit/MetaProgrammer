package com.example.meta_backend.service.generator.entity;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FieldSpec;
import com.example.meta_backend.service.generator.model.AppSchema.ManyToManySpec;
import com.example.meta_backend.service.generator.model.AppSchema.RefSpec;
import com.example.meta_backend.service.generator.model.FieldType;
import com.example.meta_backend.service.generator.model.Names;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Request com as validações da tela (required, max_length, min) e Response montada a partir da entidade: o
 * controller nunca expõe a entidade, então tenant_id e deleted_at não saem na API.
 */
@Component
public class DtoGenerator {

    public void generate(EntitySpec e, ProjectWriter out) {
        generateRequest(e, out);
        generateResponse(e, out);
    }

    private void generateRequest(EntitySpec e, ProjectWriter out) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(ProjectWriter.pkg("dto")).append(";\n\n")
          .append("import ").append(ProjectWriter.pkg("entity")).append(".*;\n")
          .append("import jakarta.validation.constraints.*;\n")
          .append("import lombok.Data;\n\n")
          .append("import java.math.BigDecimal;\n")
          .append("import java.time.*;\n")
          .append("import java.util.*;\n\n")
          .append("@Data\n")
          .append("public class ").append(e.className()).append("Request {\n\n");

        for (FieldSpec f : e.fields()) {
            if (f.required()) sb.append(f.type().isText() ? "    @NotBlank\n" : "    @NotNull\n");
            // Em texto, "min" da tela é tamanho mínimo; em número, valor mínimo
            boolean minLength = f.type().isText() && f.min() != null && f.min().matches("\\d+");
            if (f.type() == FieldType.STRING) {
                sb.append("    @Size(").append(minLength ? "min = " + f.min() + ", " : "")
                  .append("max = ").append(f.length()).append(")\n");
            } else if (minLength) {
                sb.append("    @Size(min = ").append(f.min()).append(")\n");
            }
            if (f.type().isNumeric() && f.min() != null) {
                sb.append("    @DecimalMin(\"").append(f.min()).append("\")\n");
            }
            sb.append("    private ").append(f.javaType()).append(" ").append(f.name()).append(";\n\n");
        }
        for (RefSpec r : e.refs()) {
            if (r.required()) sb.append("    @NotNull\n");
            sb.append("    private UUID ").append(r.name()).append(";\n\n");
        }
        for (ManyToManySpec m : e.manyToMany()) {
            sb.append("    private Set<UUID> ").append(m.name()).append(" = new HashSet<>();\n\n");
        }
        sb.setLength(sb.length() - 1);
        sb.append("}\n");
        out.java("dto", e.className() + "Request", sb.toString());
    }

    private void generateResponse(EntitySpec e, ProjectWriter out) {
        List<String> components = new ArrayList<>();
        List<String> getters = new ArrayList<>();
        components.add("UUID id");
        getters.add("e.getId()");
        for (FieldSpec f : e.fields()) {
            components.add(f.javaType() + " " + f.name());
            getters.add("e.get" + Names.pascal(f.name()) + "()");
        }
        for (RefSpec r : e.refs()) {
            components.add("UUID " + r.name());
            getters.add("e.get" + Names.pascal(r.name()) + "()");
        }
        for (ManyToManySpec m : e.manyToMany()) {
            components.add("Set<UUID> " + m.name());
            getters.add("Set.copyOf(e.get" + Names.pascal(m.name()) + "())");
        }
        if (e.has("timestamps")) {
            components.add("Instant createdAt");
            getters.add("e.getCreatedAt()");
            components.add("Instant updatedAt");
            getters.add("e.getUpdatedAt()");
        }
        if (e.has("versioning")) {
            components.add("Long version");
            getters.add("e.getVersion()");
        }

        String code = "package " + ProjectWriter.pkg("dto") + ";\n\n"
                + "import " + ProjectWriter.pkg("entity") + ".*;\n\n"
                + "import java.math.BigDecimal;\n"
                + "import java.time.*;\n"
                + "import java.util.*;\n\n"
                + "public record " + e.className() + "Response(\n        "
                + String.join(",\n        ", components) + ") {\n\n"
                + "    public static " + e.className() + "Response from(" + e.className() + " e) {\n"
                + "        return new " + e.className() + "Response(\n                "
                + String.join(",\n                ", getters) + ");\n"
                + "    }\n"
                + "}\n";
        out.java("dto", e.className() + "Response", code);
    }
}
