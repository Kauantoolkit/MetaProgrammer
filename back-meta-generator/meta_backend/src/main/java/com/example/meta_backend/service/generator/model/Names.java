package com.example.meta_backend.service.generator.model;

import java.util.Set;

/** Conversões de nome usadas por todos os geradores, para que Java, SQL, URL e front falem do mesmo jeito. */
public final class Names {

    // Palavras reservadas do Postgres que a tela aceita como nome de entidade sem reclamar
    private static final Set<String> RESERVED = Set.of(
            "user", "order", "group", "table", "select", "from", "where", "case", "check", "column", "default",
            "grant", "limit", "offset", "references", "role", "session", "transaction", "end", "desc", "asc");

    private Names() {
    }

    /** "pedido_item", "pedido item", "pedidoItem" → "PedidoItem". */
    public static String pascal(String raw) {
        String camel = camel(raw);
        return camel.isEmpty() ? camel : Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    /** "company_name", "Company Name", "companyName" → "companyName". */
    public static String camel(String raw) {
        if (raw == null) return "";
        String[] parts = raw.trim().split("[^A-Za-z0-9]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (sb.isEmpty()) {
                sb.append(Character.toLowerCase(part.charAt(0))).append(part.substring(1));
            } else {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        String result = sb.toString();
        if (!result.isEmpty() && Character.isDigit(result.charAt(0))) result = "f" + result;
        return result;
    }

    /** "PedidoItem" / "companyName" → "pedido_item" / "company_name". */
    public static String snake(String raw) {
        return camel(raw).replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    /** "PedidoItem" → "pedido-item". */
    public static String kebab(String raw) {
        return snake(raw).replace('_', '-');
    }

    public static String table(String className) {
        String table = snake(className);
        return RESERVED.contains(table) ? table + "s" : table;
    }

    /** Recurso da permissão e caminho da URL: "PedidoItem" → "pedido-items". */
    public static String resource(String className) {
        String kebab = kebab(className);
        return kebab.endsWith("s") ? kebab : kebab + "s";
    }

    public static String lowerFirst(String s) {
        return s.isEmpty() ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    /** Valor de enum vindo da tela ("credit card", "pix") vira identificador Java; o texto do JSON é ele mesmo. */
    public static String enumConstant(String raw) {
        String id = raw.trim().replaceAll("[^A-Za-z0-9_]+", "_");
        if (id.isEmpty()) return "VALUE";
        return Character.isDigit(id.charAt(0)) ? "V_" + id : id;
    }
}
