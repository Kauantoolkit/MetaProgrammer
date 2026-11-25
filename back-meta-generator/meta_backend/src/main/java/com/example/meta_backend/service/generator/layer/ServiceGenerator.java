package com.example.meta_backend.service.generator.layer;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FieldSpec;
import com.example.meta_backend.service.generator.model.AppSchema.ManyToManySpec;
import com.example.meta_backend.service.generator.model.AppSchema.RefSpec;
import com.example.meta_backend.service.generator.model.Names;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Service no padrão do estudio-app: tudo filtrado pelo tenant do TenantContext, erro de negócio como
 * BusinessException (o GlobalExceptionHandler do basilisk-core vira 400/404) e referência conferida antes de
 * gravar, para o id de outro tenant não passar.
 */
@Component
public class ServiceGenerator {

    public void generate(EntitySpec e, ProjectWriter out) {
        String c = e.className();
        String resp = c + "Response";
        String req = c + "Request";

        Set<String> targets = new LinkedHashSet<>();
        e.refs().stream().filter(r -> !r.toUser() && !r.targetClass().equals(c)).forEach(r -> targets.add(r.targetClass()));
        e.manyToMany().stream().filter(m -> !m.targetClass().equals(c)).forEach(m -> targets.add(m.targetClass()));

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(ProjectWriter.pkg("service")).append(";\n\n")
          .append("import com.basilisk.core.exception.BusinessException;\n")
          .append("import com.basilisk.tenant.context.TenantContext;\n")
          .append("import ").append(ProjectWriter.pkg("dto")).append(".*;\n")
          .append("import ").append(ProjectWriter.pkg("entity")).append(".*;\n")
          .append("import ").append(ProjectWriter.pkg("repository")).append(".*;\n")
          .append("import lombok.RequiredArgsConstructor;\n")
          .append("import org.springframework.data.domain.Page;\n")
          .append("import org.springframework.data.domain.Pageable;\n")
          .append("import org.springframework.http.HttpStatus;\n")
          .append("import org.springframework.stereotype.Service;\n")
          .append("import org.springframework.transaction.annotation.Transactional;\n")
          .append("import org.springframework.web.multipart.MultipartFile;\n\n")
          .append("import java.math.BigDecimal;\n")
          .append("import java.time.*;\n")
          .append("import java.util.*;\n\n")
          .append("@Service\n@RequiredArgsConstructor\n")
          .append("public class ").append(c).append("Service {\n\n")
          .append("    private final ").append(c).append("Repository repository;\n");
        for (String t : targets) {
            sb.append("    private final ").append(t).append("Repository ").append(Names.lowerFirst(t)).append("Repository;\n");
        }
        sb.append('\n');

        sb.append("    @Transactional(readOnly = true)\n")
          .append("    public Page<").append(resp).append("> list(Pageable pageable) {\n")
          .append("        return repository.findAllByTenantId(tenantId(), pageable).map(").append(resp).append("::from);\n")
          .append("    }\n\n")
          .append("    @Transactional(readOnly = true)\n")
          .append("    public ").append(resp).append(" get(UUID id) {\n")
          .append("        return ").append(resp).append(".from(find(id));\n")
          .append("    }\n\n")
          .append("    @Transactional\n")
          .append("    public ").append(resp).append(" create(").append(req).append(" request) {\n")
          .append("        ").append(c).append(" e = new ").append(c).append("();\n")
          .append("        apply(e, request);\n")
          .append("        // tenantId é injetado pelo TenantEntity.onPrePersist(). saveAndFlush: createdAt, updatedAt e\n")
          .append("        // version só ganham valor quando o Hibernate grava; com save a resposta saía com eles vazios\n")
          .append("        return ").append(resp).append(".from(repository.saveAndFlush(e));\n")
          .append("    }\n\n")
          .append("    @Transactional\n")
          .append("    public ").append(resp).append(" update(UUID id, ").append(req).append(" request) {\n")
          .append("        ").append(c).append(" e = find(id);\n")
          .append("        apply(e, request);\n")
          .append("        return ").append(resp).append(".from(repository.saveAndFlush(e));\n")
          .append("    }\n\n")
          .append("    @Transactional\n")
          .append("    public void delete(UUID id) {\n")
          .append("        repository.delete(find(id));\n")
          .append("    }\n");

        if (e.exposes("bulk_create")) {
            sb.append("\n    @Transactional\n")
              .append("    public List<").append(resp).append("> createAll(List<").append(req).append("> requests) {\n")
              .append("        return requests.stream().map(this::create).toList();\n")
              .append("    }\n");
        }
        if (e.exposes("bulk_delete")) {
            sb.append("\n    @Transactional\n")
              .append("    public int deleteAll(List<UUID> ids) {\n")
              .append("        List<").append(c).append("> found = repository.findAllByTenantIdAndIdIn(tenantId(), ids);\n")
              .append("        repository.deleteAll(found);\n")
              .append("        return found.size();\n")
              .append("    }\n");
        }
        if (e.exposes("search") && !e.textFields().isEmpty()) {
            sb.append("\n    @Transactional(readOnly = true)\n")
              .append("    public Page<").append(resp).append("> search(String q, Pageable pageable) {\n")
              .append("        return repository.search(tenantId(), q == null ? \"\" : q.strip(), pageable).map(")
              .append(resp).append("::from);\n")
              .append("    }\n");
        }
        FieldSpec date = e.dateField();
        if (e.exposes("filter_by_date_range") && (date != null || e.has("timestamps"))) {
            String field = date != null ? date.name() : "createdAt";
            String type = date != null ? date.javaType() : "Instant";
            sb.append("\n    @Transactional(readOnly = true)\n")
              .append("    public Page<").append(resp).append("> filterByDateRange(").append(type).append(" start, ")
              .append(type).append(" end, Pageable pageable) {\n")
              .append("        if (start.isAfter(end)) throw new BusinessException(\"A data inicial é depois da final\");\n")
              .append("        return repository.findAllByTenantIdAnd").append(Names.pascal(field))
              .append("Between(tenantId(), start, end, pageable).map(").append(resp).append("::from);\n")
              .append("    }\n");
        }
        if (e.exposes("export_csv")) {
            sb.append("\n    @Transactional(readOnly = true)\n")
              .append("    public String exportCsv() {\n")
              .append("        StringBuilder csv = new StringBuilder(\"").append(csvHeader(e)).append("\\n\");\n")
              .append("        for (").append(c).append(" e : repository.findAllByTenantId(tenantId())) {\n")
              .append("            csv.append(").append(csvRow(e)).append(").append('\\n');\n")
              .append("        }\n")
              .append("        return csv.toString();\n")
              .append("    }\n\n")
              .append("    private static String cell(Object value) {\n")
              .append("        if (value == null) return \"\";\n")
              .append("        String s = value.toString();\n")
              .append("        return s.matches(\"(?s).*[,\\\"\\n\\r].*\") ? '\"' + s.replace(\"\\\"\", \"\\\"\\\"\") + '\"' : s;\n")
              .append("    }\n");
        }
        if (e.exposes("import_csv")) {
            sb.append("\n    // TODO(claude): importar o CSV (mesmas colunas do export, se houver). Decidir com o usuário: linha\n")
              .append("    // inválida aborta tudo ou é pulada, e se id existente atualiza. Ver CLAUDE-TODO.md.\n")
              .append("    @Transactional\n")
              .append("    public List<").append(resp).append("> importCsv(MultipartFile file) {\n")
              .append("        throw new BusinessException(\"Importação de CSV ainda não implementada\", HttpStatus.NOT_IMPLEMENTED);\n")
              .append("    }\n");
        }

        sb.append("\n    private void apply(").append(c).append(" e, ").append(req).append(" request) {\n");
        for (FieldSpec f : e.fields()) {
            if (f.unique()) {
                String m = "existsByTenantIdAnd" + Names.pascal(f.name());
                sb.append("        if (request.get").append(Names.pascal(f.name())).append("() != null && (e.getId() == null\n")
                  .append("                ? repository.").append(m).append("(tenantId(), request.get").append(Names.pascal(f.name())).append("())\n")
                  .append("                : repository.").append(m).append("AndIdNot(tenantId(), request.get")
                  .append(Names.pascal(f.name())).append("(), e.getId()))) {\n")
                  .append("            throw new BusinessException(\"Já existe ").append(c).append(" com este ").append(f.name())
                  .append("\", HttpStatus.CONFLICT);\n")
                  .append("        }\n");
            }
            sb.append("        e.set").append(Names.pascal(f.name())).append("(request.get").append(Names.pascal(f.name())).append("());\n");
        }
        for (RefSpec r : e.refs()) {
            String getter = "request.get" + Names.pascal(r.name()) + "()";
            if (!r.toUser()) {
                String repo = r.targetClass().equals(c) ? "repository" : Names.lowerFirst(r.targetClass()) + "Repository";
                sb.append("        if (").append(getter).append(" != null && !").append(repo)
                  .append(".existsByIdAndTenantId(").append(getter).append(", tenantId())) {\n")
                  .append("            throw new BusinessException(\"").append(r.targetClass()).append(" não encontrado: \" + ")
                  .append(getter).append(");\n")
                  .append("        }\n");
            }
            sb.append("        e.set").append(Names.pascal(r.name())).append("(").append(getter).append(");\n");
        }
        for (ManyToManySpec m : e.manyToMany()) {
            String repo = m.targetClass().equals(c) ? "repository" : Names.lowerFirst(m.targetClass()) + "Repository";
            String var = m.name();
            sb.append("        Set<UUID> ").append(var).append(" = request.get").append(Names.pascal(var))
              .append("() == null ? Set.of() : request.get").append(Names.pascal(var)).append("();\n")
              .append("        if (").append(repo).append(".findAllByTenantIdAndIdIn(tenantId(), ").append(var)
              .append(").size() != ").append(var).append(".size()) {\n")
              .append("            throw new BusinessException(\"Algum ").append(m.targetClass()).append(" não foi encontrado\");\n")
              .append("        }\n")
              .append("        e.get").append(Names.pascal(var)).append("().clear();\n")
              .append("        e.get").append(Names.pascal(var)).append("().addAll(").append(var).append(");\n");
        }
        sb.append("    }\n\n")
          .append("    private ").append(c).append(" find(UUID id) {\n")
          .append("        return repository.findByIdAndTenantId(id, tenantId())\n")
          .append("                .orElseThrow(() -> new BusinessException(\"").append(c)
          .append(" não encontrado\", HttpStatus.NOT_FOUND));\n")
          .append("    }\n\n")
          .append("    private static UUID tenantId() {\n")
          .append("        return TenantContext.getTenantId();\n")
          .append("    }\n")
          .append("}\n");

        out.java("service", c + "Service", sb.toString());
    }

    private String csvHeader(EntitySpec e) {
        StringBuilder h = new StringBuilder("id");
        e.fields().forEach(f -> h.append(',').append(f.name()));
        e.refs().forEach(r -> h.append(',').append(r.name()));
        if (e.has("timestamps")) h.append(",createdAt,updatedAt");
        return h.toString();
    }

    private String csvRow(EntitySpec e) {
        StringBuilder row = new StringBuilder("cell(e.getId())");
        e.fields().forEach(f -> row.append(").append(',').append(cell(e.get").append(Names.pascal(f.name())).append("())"));
        e.refs().forEach(r -> row.append(").append(',').append(cell(e.get").append(Names.pascal(r.name())).append("())"));
        if (e.has("timestamps")) {
            row.append(").append(',').append(cell(e.getCreatedAt())").append(").append(',').append(cell(e.getUpdatedAt())");
        }
        return row.toString();
    }
}
