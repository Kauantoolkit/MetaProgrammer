package com.example.meta_backend.service.generator.entity;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FieldSpec;
import com.example.meta_backend.service.generator.model.Names;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/** Toda consulta leva o tenantId, como no estudio-app: um tenant nunca enxerga linha de outro. */
@Component
public class RepositoryGenerator {

    public void generate(EntitySpec e, ProjectWriter out) {
        String c = e.className();
        StringBuilder methods = new StringBuilder();
        methods.append("    Page<").append(c).append("> findAllByTenantId(UUID tenantId, Pageable pageable);\n\n")
               .append("    List<").append(c).append("> findAllByTenantId(UUID tenantId);\n\n")
               .append("    Optional<").append(c).append("> findByIdAndTenantId(UUID id, UUID tenantId);\n\n")
               .append("    List<").append(c).append("> findAllByTenantIdAndIdIn(UUID tenantId, Collection<UUID> ids);\n\n")
               .append("    boolean existsByIdAndTenantId(UUID id, UUID tenantId);\n");

        // "unique" da tela vale dentro do tenant (a migration cria UNIQUE (tenant_id, coluna)); o service confere
        // antes de gravar para responder 409 em vez de estourar a constraint
        for (FieldSpec f : e.fields()) {
            if (!f.unique()) continue;
            String field = Names.pascal(f.name());
            methods.append("\n    boolean existsByTenantIdAnd").append(field).append("(UUID tenantId, ")
                   .append(f.javaType()).append(" value);\n\n")
                   .append("    boolean existsByTenantIdAnd").append(field).append("AndIdNot(UUID tenantId, ")
                   .append(f.javaType()).append(" value, UUID id);\n");
        }

        if (e.exposes("search") && !e.textFields().isEmpty()) {
            String where = e.textFields().stream()
                    .map(f -> "lower(e." + f.name() + ") like lower(concat('%', :q, '%'))")
                    .collect(Collectors.joining(" or "));
            methods.append("\n    @Query(\"select e from ").append(c).append(" e where e.tenantId = :tenantId and (")
                   .append(where).append(")\")\n")
                   .append("    Page<").append(c)
                   .append("> search(@Param(\"tenantId\") UUID tenantId, @Param(\"q\") String q, Pageable pageable);\n");
        }

        FieldSpec date = e.dateField();
        if (e.exposes("filter_by_date_range") && (date != null || e.has("timestamps"))) {
            String field = date != null ? date.name() : "createdAt";
            String type = date != null ? date.javaType() : "Instant";
            methods.append("\n    Page<").append(c).append("> findAllByTenantIdAnd").append(Names.pascal(field))
                   .append("Between(UUID tenantId, ").append(type).append(" start, ").append(type)
                   .append(" end, Pageable pageable);\n");
        }

        String code = "package " + ProjectWriter.pkg("repository") + ";\n\n"
                + "import " + ProjectWriter.pkg("entity") + "." + c + ";\n"
                + "import org.springframework.data.domain.Page;\n"
                + "import org.springframework.data.domain.Pageable;\n"
                + "import org.springframework.data.jpa.repository.JpaRepository;\n"
                + "import org.springframework.data.jpa.repository.Query;\n"
                + "import org.springframework.data.repository.query.Param;\n\n"
                + "import java.time.*;\n"
                + "import java.util.*;\n\n"
                + "public interface " + c + "Repository extends JpaRepository<" + c + ", UUID> {\n\n"
                + methods
                + "}\n";
        out.java("repository", c + "Repository", code);
    }
}
