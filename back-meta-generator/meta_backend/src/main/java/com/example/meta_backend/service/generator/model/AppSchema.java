package com.example.meta_backend.service.generator.model;

import java.util.List;
import java.util.Set;

/**
 * O payload da tela já normalizado (SchemaParser): nomes resolvidos, tipos únicos e relações reduzidas ao lado
 * dono. Os geradores leem só isto, nunca o Map cru.
 */
public record AppSchema(
        String appName,
        String className,
        List<EntitySpec> entities,
        List<FunctionalitySpec> functionalities,
        List<String> notes) {

    public EntitySpec entity(String className) {
        return entities.stream().filter(e -> e.className().equals(className)).findFirst().orElse(null);
    }

    public record EntitySpec(
            String className,
            String varName,
            String table,
            String resource,
            List<FieldSpec> fields,
            List<RefSpec> refs,
            List<ManyToManySpec> manyToMany,
            Set<String> behaviors,
            Set<String> endpoints) {

        public boolean has(String behavior) {
            return behaviors.contains(behavior);
        }

        public boolean exposes(String endpoint) {
            return endpoints.contains(endpoint);
        }

        /** Sem "crud", "read_only" e nenhum outro, a entidade não ganha controller. */
        public boolean hasApi() {
            return !endpoints.isEmpty();
        }

        public boolean writable() {
            return endpoints.contains("crud");
        }

        public List<FieldSpec> textFields() {
            return fields.stream().filter(f -> f.type().isText()).toList();
        }

        /** Campo usado pelo filtro por período: o primeiro de data; sem nenhum, o createdAt dos timestamps. */
        public FieldSpec dateField() {
            return fields.stream().filter(f -> f.type().isTemporal()).findFirst().orElse(null);
        }
    }

    public record FieldSpec(
            String name,
            String column,
            FieldType type,
            String enumClass,
            List<String> enumValues,
            boolean required,
            boolean unique,
            boolean indexed,
            Integer maxLength,
            String min) {

        public String javaType() {
            return type == FieldType.ENUM ? enumClass : type.javaType();
        }

        public String sqlType() {
            if (type == FieldType.STRING) return "VARCHAR(" + length() + ")";
            return type.sqlType();
        }

        public int length() {
            return maxLength != null ? maxLength : 255;
        }
    }

    /**
     * Coluna UUID que aponta para outra entidade (N:1, 1:1, ou o lado "muitos" de um 1:N). Como no estudio-app
     * (Comment.videoId), é um UUID solto na entidade, não @ManyToOne.
     */
    public record RefSpec(
            String name,
            String column,
            String targetClass,
            String targetTable,
            boolean required,
            boolean unique,
            boolean cascadeDelete,
            boolean toUser) {
    }

    /** N:N: tabela de junção do lado de nome menor; a entidade guarda o conjunto de ids. */
    public record ManyToManySpec(
            String name,
            String joinTable,
            String ownerColumn,
            String targetColumn,
            String targetClass,
            String targetTable) {
    }

    public record FunctionalitySpec(
            String className,
            String methodName,
            String path,
            String entityClass,
            List<ParamSpec> input,
            List<ParamSpec> output) {
    }

    public record ParamSpec(String name, String javaType) {
    }
}
