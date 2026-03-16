package com.example.meta_backend.service.llm;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ContextBuilder {

    public String buildSystemPrompt() {
        return """
                Você é um desenvolvedor Java especialista em Spring Boot.
                Responda SEMPRE neste formato exato. NÃO escreva nada fora dos blocos.

                === IMPL ===
                // APENAS o código dentro das chaves {} do método. NÃO inclua a assinatura do método.
                === END IMPL ===

                === REPO NomeDoRepository ===
                // apenas assinaturas de métodos customizados, uma por linha, terminadas com ;
                === END REPO ===

                REGRAS OBRIGATÓRIAS:
                1. IMPL é obrigatório. Escreva SOMENTE o corpo do método — sem modificador de acesso, sem nome do método, sem chaves externas.
                2. NÃO use markdown. NÃO escreva ```, NÃO escreva ```java em nenhum lugar da resposta.
                3. Todo método customizado chamado no IMPL DEVE ser declarado em um bloco REPO correspondente. Sem exceções.
                4. Métodos JPA que NÃO precisam de REPO block (já existem): findAll, findById, save, saveAll, delete, deleteById, deleteAll, deleteAllById, count, existsById, flush, saveAndFlush, getReferenceById.
                5. Para retornos de findBy*: use Optional<Tipo> quando retorna zero ou um; List<Tipo> quando retorna vários.
                6. Você pode ter vários blocos REPO para repositórios diferentes.
                7. Nas assinaturas do REPO: NÃO coloque @Query, NÃO coloque implementação — apenas a assinatura terminada com ;

                CHECKLIST antes de responder:
                - O bloco IMPL contém apenas o corpo do método (sem public, sem nome, sem chaves externas)?
                - Não há ``` em nenhum lugar da resposta?
                - Cada método customizado chamado no IMPL está declarado em um bloco REPO?
                """;
    }

    public String buildUserPrompt(Map<String, Object> functionality, List<Map<String, Object>> entities) {
        String funcName = (String) functionality.get("name");
        String linkedEntity = (String) functionality.getOrDefault("entity", "");
        String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> inputs = (List<Map<String, Object>>) functionality.getOrDefault("input", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> outputs = (List<Map<String, Object>>) functionality.getOrDefault("output", List.of());

        StringBuilder sb = new StringBuilder();

        sb.append("=== ENTIDADES DO PROJETO ===\n");
        for (Map<String, Object> entity : entities) {
            String name = (String) entity.get("name");
            if ("Users".equalsIgnoreCase(name)) continue;

            sb.append("Entidade: ").append(name).append("\n");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> attrs = (List<Map<String, Object>>) entity.getOrDefault("attributes", List.of());
            for (Map<String, Object> attr : attrs) {
                sb.append("  - ").append(attr.get("name")).append(": ").append(mapJavaType((String) attr.get("type")));
                @SuppressWarnings("unchecked")
                List<String> values = (List<String>) attr.get("values");
                if (values != null && !values.isEmpty()) {
                    sb.append(" (enum: ").append(String.join(", ", values)).append(")");
                }
                sb.append("\n");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rels = (List<Map<String, Object>>) entity.getOrDefault("relations", List.of());
            for (Map<String, Object> rel : rels) {
                sb.append("  - relacao ").append(rel.get("type")).append(" com ").append(rel.get("target")).append("\n");
            }
            sb.append("\n");
        }

        sb.append("=== REPOSITÓRIOS INJETADOS NA CLASSE (use diretamente pelo nome do campo) ===\n");
        for (Map<String, Object> entity : entities) {
            String name = (String) entity.get("name");
            if ("Users".equalsIgnoreCase(name)) continue;
            String varName = name.substring(0, 1).toLowerCase() + name.substring(1) + "Repository";
            sb.append("- ").append(varName).append(" (").append(name).append("Repository)\n");
        }
        sb.append("\n");

        sb.append("=== FUNCIONALIDADE: ").append(funcName).append(" ===\n");
        if (!linkedEntity.isEmpty()) {
            sb.append("Entidade vinculada: ").append(linkedEntity).append("\n");
        }
        sb.append("\n");

        sb.append("=== REQUEST DTO (").append(funcName).append("RequestDto) ===\n");
        for (Map<String, Object> field : inputs) {
            sb.append("- ").append(field.get("name")).append(": ").append(mapJavaType((String) field.get("type"))).append("\n");
        }
        sb.append("\n");

        sb.append("=== RESPONSE DTO (").append(funcName).append("ResponseDto) ===\n");
        for (Map<String, Object> field : outputs) {
            sb.append("- ").append(field.get("name")).append(": ").append(mapJavaType((String) field.get("type"))).append("\n");
        }
        sb.append("\n");

        sb.append("=== INTERFACE ===\n");
        sb.append(funcName).append("ResponseDto ").append(methodName)
          .append("(").append(funcName).append("RequestDto request);\n\n");

        sb.append("Implemente o corpo do método ").append(methodName).append("().\n");
        sb.append("Os repositórios listados acima já estão injetados como campos da classe — use-os diretamente.\n");
        sb.append("Retorne uma instância de ").append(funcName).append("ResponseDto com todos os campos preenchidos.\n\n");
        sb.append("IMPORTANTE: Se você chamar qualquer método customizado em um repositório (ex: findByClientIp, countBySuccess, findByInputFormatAndOutputFormat),\n");
        sb.append("DECLARE esse método no bloco === REPO NomeDoRepository === correspondente.\n");
        sb.append("Não declare métodos que já existem no JpaRepository (findAll, findById, save, delete, count, existsById, etc).\n\n");
        sb.append("Responda no formato estruturado conforme as instruções do sistema. Não use markdown nem ```.\n");

        return sb.toString();
    }

    private String mapJavaType(String type) {
        if (type == null) return "String";
        return switch (type.toLowerCase()) {
            case "string", "text", "enum" -> "String";
            case "number", "double"       -> "Double";
            case "int", "integer"         -> "Integer";
            case "long"                   -> "Long";
            case "boolean"                -> "Boolean";
            case "date"                   -> "java.time.LocalDate";
            case "datetime"               -> "java.time.LocalDateTime";
            default                       -> "String";
        };
    }
}
