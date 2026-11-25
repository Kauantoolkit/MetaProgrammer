package com.example.meta_backend.service.generator.model;

import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FieldSpec;
import com.example.meta_backend.service.generator.model.AppSchema.FunctionalitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.ManyToManySpec;
import com.example.meta_backend.service.generator.model.AppSchema.ParamSpec;
import com.example.meta_backend.service.generator.model.AppSchema.RefSpec;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Converte o payload da tela em AppSchema. Tudo o que o gerador não sabe fazer sozinho vira nota, e as notas vão
 * para o CLAUDE-TODO.md do projeto gerado em vez de sumirem.
 */
@Component
public class SchemaParser {

    // O usuário, o login e o tenant vêm do basilisk-user: uma entidade com estes nomes no payload não é gerada
    private static final Set<String> USER_ENTITIES = Set.of("User", "Users", "Usuario", "Usuarios");

    @SuppressWarnings("unchecked")
    public AppSchema parse(Map<String, Object> payload) {
        String appName = str(payload.get("appName"));
        if (appName.isBlank()) throw new IllegalArgumentException("appName é obrigatório");

        List<Map<String, Object>> rawEntities = list(payload.get("entities"));
        List<Map<String, Object>> rawFunctionalities = list(payload.get("functionalities"));
        List<String> notes = new ArrayList<>();

        Map<String, Map<String, Object>> byClass = new LinkedHashMap<>();
        for (Map<String, Object> raw : rawEntities) {
            String className = Names.pascal(str(raw.get("name")));
            if (className.isEmpty()) continue;
            if (USER_ENTITIES.contains(className)) {
                notes.add("A entidade `" + className + "` do payload não foi gerada: usuário, login e papéis vêm do "
                        + "basilisk-user (`/api/v1/auth`, `/api/v1/users`). Relações apontando para ela viraram "
                        + "`userId` (UUID do usuário, sem FK). Se ela tinha campos próprios (ex.: telefone), "
                        + "decidir onde guardá-los.");
                continue;
            }
            byClass.put(className, raw);
        }

        Map<String, List<RefSpec>> refs = new LinkedHashMap<>();
        Map<String, List<ManyToManySpec>> manyToMany = new LinkedHashMap<>();
        byClass.keySet().forEach(c -> {
            refs.put(c, new ArrayList<>());
            manyToMany.put(c, new ArrayList<>());
        });
        resolveRelations(byClass, refs, manyToMany, notes);

        List<EntitySpec> entities = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : byClass.entrySet()) {
            entities.add(entity(entry.getKey(), entry.getValue(), refs.get(entry.getKey()),
                    manyToMany.get(entry.getKey()), notes));
        }

        List<FunctionalitySpec> functionalities = new ArrayList<>();
        for (Map<String, Object> raw : rawFunctionalities) {
            FunctionalitySpec f = functionality(raw, byClass.keySet(), notes);
            if (f != null) functionalities.add(f);
        }

        return new AppSchema(appName, Names.pascal(appName), entities, functionalities, notes);
    }

    // --- entidades e campos ---

    private EntitySpec entity(String className, Map<String, Object> raw, List<RefSpec> refs,
                              List<ManyToManySpec> m2m, List<String> notes) {
        List<FieldSpec> fields = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (Map<String, Object> attr : list(raw.get("attributes"))) {
            String name = Names.camel(str(attr.get("name")));
            if (name.isEmpty() || name.equalsIgnoreCase("id") || name.equalsIgnoreCase("tenantId")) continue;
            if (!used.add(name)) continue;
            fields.add(field(className, name, attr));
        }

        Set<String> behaviors = new LinkedHashSet<>(strings(raw.get("behaviors")));
        Map<String, Object> api = map(raw.get("api"));
        Set<String> endpoints = new LinkedHashSet<>(strings(api.get("endpoints")));

        if (behaviors.contains("state_machine")) {
            notes.add("`" + className + "` pediu máquina de estados (`state_machine`): definir os estados e as "
                    + "transições permitidas e travar no `" + className + "Service` (hoje qualquer status é aceito).");
        }
        if (behaviors.contains("audit_log")) {
            notes.add("`" + className + "` pediu `audit_log`: o basilisk-audit já grava toda escrita feita pela API "
                    + "(quem, quando, rota). Se precisar do antes/depois dos campos, implementar no service.");
        }
        boolean hasDate = fields.stream().anyMatch(f -> f.type().isTemporal()) || behaviors.contains("timestamps");
        if (endpoints.contains("filter_by_date_range") && !hasDate) {
            notes.add("`" + className + "` pediu filtro por período (`filter_by_date_range`), mas não tem campo de "
                    + "data nem `timestamps`: o endpoint não foi gerado. Dizer qual data filtrar (ou ligar "
                    + "`timestamps` na tela e gerar de novo).");
        }
        if (endpoints.contains("search") && fields.stream().noneMatch(f -> f.type().isText())) {
            notes.add("`" + className + "` pediu busca (`search`), mas não tem campo de texto: o endpoint não foi "
                    + "gerado. Dizer por quais campos buscar.");
        }
        String auth = str(api.get("auth"));
        if (auth.equals("none") || auth.equals("api_key")) {
            notes.add("`" + className + "` foi marcada como `" + auth + "` na tela, mas foi gerada exigindo login: "
                    + "num sistema multiempresa, uma chamada " + (auth.equals("none") ? "sem login" : "por chave")
                    + " não diz de qual empresa é o dado. Decidir a regra antes de abrir.");
        }

        return new EntitySpec(className, Names.lowerFirst(className), Names.table(className),
                Names.resource(className), fields, refs, m2m, behaviors, endpoints);
    }

    private FieldSpec field(String className, String name, Map<String, Object> attr) {
        FieldType type = FieldType.parse(str(attr.get("type")));
        boolean required = false, unique = false, indexed = false;
        Integer maxLength = null;
        String min = null;
        for (String c : strings(attr.get("constraints"))) {
            String key = c.contains(":") ? c.substring(0, c.indexOf(':')) : c;
            String value = c.contains(":") ? c.substring(c.indexOf(':') + 1).trim() : "";
            switch (key) {
                case "required" -> required = true;
                case "unique" -> unique = true;
                case "indexed" -> indexed = true;
                case "max_length", "max" -> maxLength = parseInt(value);
                case "min" -> min = value.matches("-?\\d+(\\.\\d+)?") ? value : null;
                default -> { /* primary_key / auto_increment: o id é sempre UUID gerado */ }
            }
        }
        List<String> values = new ArrayList<>();
        String enumClass = null;
        if (type == FieldType.ENUM) {
            for (String v : strings(attr.get("values"))) values.add(Names.enumConstant(v));
            if (values.isEmpty()) {
                // Enum sem valores não compila; sem lista, o campo vira texto
                type = FieldType.STRING;
            } else {
                enumClass = className + Names.pascal(name);
            }
        }
        if (type == FieldType.TEXT || type == FieldType.ENUM) maxLength = null;
        return new FieldSpec(name, Names.snake(name), type, enumClass, values, required, unique, indexed,
                type == FieldType.STRING ? maxLength : null, min);
    }

    // --- relações ---

    /**
     * Cada relação da tela vira a coluna do lado dono: N:1 e 1:1 ficam na própria entidade, 1:N na entidade alvo,
     * N:N numa tabela de junção. O par declarado dos dois lados (Pedido 1:N Pagamento e Pagamento N:1 Pedido) gera
     * uma coluna só.
     */
    private void resolveRelations(Map<String, Map<String, Object>> byClass, Map<String, List<RefSpec>> refs,
                                  Map<String, List<ManyToManySpec>> manyToMany, List<String> notes) {
        Map<String, RefSpec> seen = new LinkedHashMap<>();
        Set<String> seenJoin = new HashSet<>();

        for (Map.Entry<String, Map<String, Object>> entry : byClass.entrySet()) {
            String source = entry.getKey();
            for (Map<String, Object> rel : list(entry.getValue().get("relations"))) {
                String target = Names.pascal(str(rel.get("target")));
                String type = str(rel.get("type")).toUpperCase();
                boolean required = Boolean.TRUE.equals(rel.get("required"));
                boolean cascade = "cascade".equalsIgnoreCase(str(rel.get("cascade")));
                boolean toUser = USER_ENTITIES.contains(target);

                if (!toUser && !byClass.containsKey(target)) {
                    notes.add("Relação de `" + source + "` para `" + target + "` ignorada: `" + target
                            + "` não existe no payload.");
                    continue;
                }
                if (toUser && (type.equals("1:N") || type.equals("N:N"))) {
                    notes.add("Relação `" + source + "` " + type + " usuário ignorada: o usuário é do basilisk-user. "
                            + "Se cada usuário precisa de vários `" + source + "`, guardar o `userId` do lado de `"
                            + source + "`.");
                    continue;
                }

                switch (type) {
                    case "N:1", "MANYTOONE" -> addRef(seen, refs, source, target, required, false, cascade, toUser);
                    case "1:1", "ONETOONE" -> {
                        // 1:1 declarado dos dois lados: a coluna fica no lado de nome menor
                        String owner = source, other = target;
                        if (!toUser && declares(byClass.get(target), source, "1:1") && target.compareTo(source) < 0) {
                            owner = target;
                            other = source;
                        }
                        addRef(seen, refs, owner, other, required, true, cascade, toUser);
                    }
                    case "1:N", "ONETOMANY" -> addRef(seen, refs, target, source, false, false, cascade, false);
                    case "N:N", "MANYTOMANY" -> {
                        String owner = source.compareTo(target) <= 0 ? source : target;
                        String other = owner.equals(source) ? target : source;
                        String joinTable = Names.table(owner) + "_" + Names.table(other);
                        if (!seenJoin.add(joinTable)) continue;
                        String ownerCol = Names.snake(owner) + "_id";
                        String targetCol = owner.equals(other) ? "related_id" : Names.snake(other) + "_id";
                        manyToMany.get(owner).add(new ManyToManySpec(Names.lowerFirst(other) + "Ids", joinTable,
                                ownerCol, targetCol, other, Names.table(other)));
                    }
                    default -> notes.add("Relação `" + source + "` → `" + target + "` com tipo desconhecido `"
                            + type + "` ignorada.");
                }
            }
        }
    }

    private void addRef(Map<String, RefSpec> seen, Map<String, List<RefSpec>> refs, String owner, String target,
                        boolean required, boolean unique, boolean cascade, boolean toUser) {
        String name = toUser ? "userId" : Names.lowerFirst(target) + "Id";
        String key = owner + "." + name;
        RefSpec previous = seen.get(key);
        if (previous != null) {
            // Mesmo vínculo vindo dos dois lados: vale o mais restritivo
            if ((required && !previous.required()) || (cascade && !previous.cascadeDelete())
                    || (unique && !previous.unique())) {
                RefSpec merged = new RefSpec(name, previous.column(), previous.targetClass(), previous.targetTable(),
                        previous.required() || required, previous.unique() || unique,
                        previous.cascadeDelete() || cascade, toUser);
                refs.get(owner).set(refs.get(owner).indexOf(previous), merged);
                seen.put(key, merged);
            }
            return;
        }
        RefSpec ref = new RefSpec(name, Names.snake(name), toUser ? null : target,
                toUser ? null : Names.table(target), required, unique, cascade, toUser);
        refs.get(owner).add(ref);
        seen.put(key, ref);
    }

    private boolean declares(Map<String, Object> entity, String target, String type) {
        for (Map<String, Object> rel : list(entity.get("relations"))) {
            if (Names.pascal(str(rel.get("target"))).equals(target) && str(rel.get("type")).equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    // --- funcionalidades ---

    private FunctionalitySpec functionality(Map<String, Object> raw, Set<String> entities, List<String> notes) {
        String className = Names.pascal(str(raw.get("name")));
        if (className.isEmpty()) return null;
        String entity = Names.pascal(str(raw.get("entity")));
        if (!entities.contains(entity)) {
            notes.add("Funcionalidade `" + className + "` não foi gerada: a entidade `" + entity
                    + "` não existe no payload.");
            return null;
        }
        return new FunctionalitySpec(className, Names.lowerFirst(className), Names.kebab(className), entity,
                params(raw.get("input")), params(raw.get("output")));
    }

    private List<ParamSpec> params(Object raw) {
        List<ParamSpec> params = new ArrayList<>();
        Set<String> used = new HashSet<>();
        int i = 1;
        for (Map<String, Object> p : list(raw)) {
            String name = Names.camel(str(p.get("name")));
            if (name.isEmpty()) name = "field" + i;
            i++;
            if (!used.add(name)) continue;
            FieldType type = FieldType.parse(str(p.get("type")));
            // Os ids do sistema gerado são UUID: "orderId: Long" do payload antigo aponta para um deles
            if (name.endsWith("Id") && (type.isNumeric() || type == FieldType.STRING)) type = FieldType.UUID;
            String javaType = type == FieldType.ENUM ? "String" : type.javaType();
            params.add(new ParamSpec(name, javaType));
        }
        return params;
    }

    // --- leitura tolerante do Map ---

    private static String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    private static Integer parseInt(String s) {
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> list(Object o) {
        if (!(o instanceof List<?> l)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : l) if (item instanceof Map<?, ?> m) out.add((Map<String, Object>) m);
        return out;
    }

    private static List<String> strings(Object o) {
        if (!(o instanceof List<?> l)) return List.of();
        List<String> out = new ArrayList<>();
        for (Object item : l) if (item != null && !item.toString().isBlank()) out.add(item.toString().trim());
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object o) {
        return o instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }
}
