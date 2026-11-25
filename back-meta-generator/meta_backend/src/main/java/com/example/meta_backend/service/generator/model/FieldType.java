package com.example.meta_backend.service.generator.model;

/**
 * Tipos que a tela do gerador oferece (AttributesEditor.jsx), com o tipo Java e o SQL juntos: antes cada gerador
 * tinha a própria tabela e elas discordavam ("number" virava String no Java e VARCHAR no banco).
 */
public enum FieldType {
    STRING("String", "VARCHAR"),
    TEXT("String", "TEXT"),
    NUMBER("BigDecimal", "NUMERIC(19,4)"),
    INTEGER("Integer", "INTEGER"),
    LONG("Long", "BIGINT"),
    BOOLEAN("Boolean", "BOOLEAN"),
    DATE("LocalDate", "DATE"),
    TIME("LocalTime", "TIME"),
    DATETIME("LocalDateTime", "TIMESTAMP"),
    ENUM(null, "VARCHAR(64)"),
    UUID("UUID", "UUID");

    private final String javaType;
    private final String sqlType;

    FieldType(String javaType, String sqlType) {
        this.javaType = javaType;
        this.sqlType = sqlType;
    }

    /** Tipo Java; enum usa a classe gerada para o campo, que só o FieldSpec conhece. */
    public String javaType() {
        return javaType;
    }

    public String sqlType() {
        return sqlType;
    }

    public boolean isTemporal() {
        return this == DATE || this == DATETIME;
    }

    public boolean isNumeric() {
        return this == NUMBER || this == INTEGER || this == LONG;
    }

    public boolean isText() {
        return this == STRING || this == TEXT;
    }

    /** Aceita os nomes da tela e os antigos dos payloads de exemplo (Long, Integer, Double, DateTime...). */
    public static FieldType parse(String raw) {
        if (raw == null || raw.isBlank()) return STRING;
        return switch (raw.trim().toLowerCase()) {
            case "text", "longtext" -> TEXT;
            case "number", "double", "float", "decimal", "bigdecimal" -> NUMBER;
            case "int", "integer" -> INTEGER;
            case "long" -> LONG;
            case "boolean", "bool" -> BOOLEAN;
            case "date", "localdate" -> DATE;
            case "time", "localtime" -> TIME;
            case "datetime", "localdatetime", "timestamp", "instant" -> DATETIME;
            case "enum" -> ENUM;
            case "uuid" -> UUID;
            default -> STRING;
        };
    }
}
