package com.example.meta_backend.service.generator.functionality;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FunctionalitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.ParamSpec;
import com.example.meta_backend.service.generator.model.Names;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Funcionalidade de negócio (ApproveOrder, RegisterPayment...): o gerador escreve tudo menos a regra — DTOs,
 * endpoint com permissão e o service com o corpo marcado `TODO(claude)`, que responde 501 até alguém implementar.
 * A lista do que falta vai para o CLAUDE-TODO.md (ClaudeTodoGenerator). Substitui a geração por LLM via API.
 */
@Component
public class FunctionalityGenerator {

    public static final String MARKER = "TODO(claude)";

    public void generate(AppSchema schema, FunctionalitySpec f, ProjectWriter out) {
        EntitySpec entity = schema.entity(f.entityClass());
        String c = f.className();

        out.java("dto.functionality", c + "Request", dto(c + "Request", f.input()));
        out.java("dto.functionality", c + "Response", dto(c + "Response", f.output()));

        String service = "package " + ProjectWriter.pkg("service.functionality") + ";\n\n"
                + "import com.basilisk.core.exception.BusinessException;\n"
                + "import " + ProjectWriter.pkg("dto.functionality") + "." + c + "Request;\n"
                + "import " + ProjectWriter.pkg("dto.functionality") + "." + c + "Response;\n"
                + "import " + ProjectWriter.pkg("repository") + "." + entity.className() + "Repository;\n"
                + "import lombok.RequiredArgsConstructor;\n"
                + "import org.springframework.http.HttpStatus;\n"
                + "import org.springframework.stereotype.Service;\n"
                + "import org.springframework.transaction.annotation.Transactional;\n\n"
                + "@Service\n@RequiredArgsConstructor\n"
                + "public class " + c + "Service {\n\n"
                + "    private final " + entity.className() + "Repository " + entity.varName() + "Repository;\n\n"
                + "    // " + MARKER + ": implementar " + c + " (ver CLAUDE-TODO.md). Buscar sempre pelo tenant\n"
                + "    // (findByIdAndTenantId com TenantContext.getTenantId()) e erro de regra como BusinessException.\n"
                + "    @Transactional\n"
                + "    public " + c + "Response execute(" + c + "Request request) {\n"
                + "        throw new BusinessException(\"" + c + " ainda não implementada\", HttpStatus.NOT_IMPLEMENTED);\n"
                + "    }\n"
                + "}\n";
        out.java("service.functionality", c + "Service", service);

        String controller = "package " + ProjectWriter.pkg("controller") + ";\n\n"
                + "import com.basilisk.core.dto.ApiResponse;\n"
                + "import com.basilisk.permission.annotation.RequiresPermission;\n"
                + "import " + ProjectWriter.pkg("dto.functionality") + "." + c + "Request;\n"
                + "import " + ProjectWriter.pkg("dto.functionality") + "." + c + "Response;\n"
                + "import " + ProjectWriter.pkg("service.functionality") + "." + c + "Service;\n"
                + "import jakarta.validation.Valid;\n"
                + "import lombok.RequiredArgsConstructor;\n"
                + "import org.springframework.http.ResponseEntity;\n"
                + "import org.springframework.web.bind.annotation.*;\n\n"
                + "@RestController\n"
                + "@RequestMapping(\"/api/v1/" + entity.resource() + "/" + f.path() + "\")\n"
                + "@RequiredArgsConstructor\n"
                + "public class " + c + "Controller {\n\n"
                + "    private final " + c + "Service service;\n\n"
                + "    @PostMapping\n"
                + "    @RequiresPermission(\"" + entity.resource() + ":write\")\n"
                + "    public ResponseEntity<ApiResponse<" + c + "Response>> execute(@Valid @RequestBody " + c + "Request request) {\n"
                + "        return ResponseEntity.ok(ApiResponse.ok(service.execute(request)));\n"
                + "    }\n"
                + "}\n";
        out.java("controller", c + "Controller", controller);
    }

    private String dto(String className, List<ParamSpec> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(ProjectWriter.pkg("dto.functionality")).append(";\n\n")
          .append("import lombok.AllArgsConstructor;\n")
          .append("import lombok.Data;\n")
          .append("import lombok.NoArgsConstructor;\n\n")
          .append("import java.math.BigDecimal;\n")
          .append("import java.time.*;\n")
          .append("import java.util.*;\n\n")
          .append("@Data\n@NoArgsConstructor\n");
        if (!params.isEmpty()) sb.append("@AllArgsConstructor\n");
        sb.append("public class ").append(className).append(" {\n");
        for (ParamSpec p : params) {
            sb.append("\n    private ").append(p.javaType()).append(' ').append(p.name()).append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    /** Caminho completo do endpoint, para o checklist. */
    public static String endpoint(AppSchema schema, FunctionalitySpec f) {
        return "POST /api/v1/" + schema.entity(f.entityClass()).resource() + "/" + f.path();
    }

    public static String serviceFile(FunctionalitySpec f) {
        return "backend/src/main/java/" + ProjectWriter.pkg("service.functionality").replace('.', '/')
                + "/" + Names.pascal(f.className()) + "Service.java";
    }
}
