package com.example.meta_backend.service.generator.functionality;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FunctionalitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.ParamSpec;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Checklist do que o gerador não decide sozinho, para um agente (Claude) pegar depois: cada item diz o arquivo,
 * o endpoint, a entrada e a saída. No código, o ponto exato leva a marca TODO(claude).
 */
@Component
public class ClaudeTodoGenerator {

    public void generate(AppSchema schema, ProjectWriter out) {
        StringBuilder md = new StringBuilder();
        md.append("# CLAUDE-TODO — ").append(schema.appName()).append("\n\n")
          .append("Gerado pelo MetaProgrammer. O código já compila e sobe; o que está abaixo é o que ficou para\n")
          .append("um agente (ou uma pessoa) completar. No código, cada ponto tem a marca `")
          .append(FunctionalityGenerator.MARKER).append("`.\n\n")
          .append("Como trabalhar aqui:\n")
          .append("- Toda consulta vai pelo tenant: `repository.findByIdAndTenantId(id, TenantContext.getTenantId())`.\n")
          .append("- Erro de regra: `throw new BusinessException(\"mensagem\")` (400) ou com `HttpStatus` (404, 409...).\n")
          .append("- Os ids são UUID. Referência a outra entidade é um campo `UUID xxxId` (não há `@ManyToOne`).\n")
          .append("- Ao terminar um item, marcar `[x]` e apagar a marca do código.\n\n");

        md.append("## Funcionalidades\n\n");
        if (schema.functionalities().isEmpty()) md.append("Nenhuma.\n\n");
        for (FunctionalitySpec f : schema.functionalities()) {
            EntitySpec e = schema.entity(f.entityClass());
            md.append("- [ ] **").append(f.className()).append("** (entidade `").append(e.className()).append("`)\n")
              .append("  - Endpoint: `").append(FunctionalityGenerator.endpoint(schema, f)).append("` — permissão `")
              .append(e.resource()).append(":write`\n")
              .append("  - Entrada: ").append(params(f.input())).append('\n')
              .append("  - Saída: ").append(params(f.output())).append('\n')
              .append("  - Implementar `execute()` em `").append(FunctionalityGenerator.serviceFile(f)).append("`\n")
              .append("  - Regra de negócio: **não veio da tela** — confirmar com o usuário antes de escrever.\n");
        }

        boolean importCsv = schema.entities().stream().anyMatch(e -> e.exposes("import_csv"));
        if (importCsv || !schema.notes().isEmpty()) {
            md.append("\n## Pendências da geração\n\n");
            for (EntitySpec e : schema.entities()) {
                if (e.exposes("import_csv")) {
                    md.append("- [ ] `").append(e.className()).append("Service.importCsv`: importar CSV (`POST /api/v1/")
                      .append(e.resource()).append("/import`). Decidir: linha inválida aborta tudo ou é pulada; id existente atualiza?\n");
                }
            }
            for (String note : schema.notes()) md.append("- [ ] ").append(note).append('\n');
        }
        out.write("CLAUDE-TODO.md", md.toString());
    }

    private String params(List<ParamSpec> params) {
        if (params.isEmpty()) return "(nada)";
        return params.stream().map(p -> "`" + p.name() + ": " + p.javaType() + "`").collect(Collectors.joining(", "));
    }
}
