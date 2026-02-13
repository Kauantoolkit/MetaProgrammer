package com.example.meta_backend.service.generator.base;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
public class FlywayMigrationGenerator {

    private static final String BASE_PACKAGE = "com/metagen/backend/generated/";

    public void generateFlywayMigration(String appName, List<Map<String, Object>> entities) throws IOException {
        String dbMigrationDir = appName + "/src/main/resources/db/migration/";
        
        // Generate V1__Initial_schema.sql with all tables
        StringBuilder sql = new StringBuilder();
        
        // Create users table
        sql.append(createUsersTable());
        sql.append("\n\n");
        
        // Create entity tables
        if (entities != null) {
            for (Map<String, Object> entity : entities) {
                String name = (String) entity.get("name");
                if (name == null || name.isBlank()) continue;
                
                String tableName = name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
                List<Map<String, Object>> attrs = (List<Map<String, Object>>) entity.getOrDefault("attributes", List.of());
                List<Map<String, Object>> rels = (List<Map<String, Object>>) entity.getOrDefault("relations", List.of());
                
                sql.append(createTableStatement(tableName, attrs, rels));
                sql.append("\n\n");
            }
            
            // Create join tables for ManyToMany relationships
            for (Map<String, Object> entity : entities) {
                List<Map<String, Object>> rels = (List<Map<String, Object>>) entity.getOrDefault("relations", List.of());
                String sourceTable = ((String) entity.get("name")).toLowerCase().replaceAll("[^a-z0-9_]", "_");
                
                for (Map<String, Object> rel : rels) {
                    String type = (String) rel.get("type");
                    String target = (String) rel.get("target");
                    
                    if (type != null && target != null && type.equalsIgnoreCase("manytomany")) {
                        String targetTable = target.toLowerCase().replaceAll("[^a-z0-9_]", "_");
                        sql.append(createJoinTable(sourceTable, targetTable));
                        sql.append("\n\n");
                    }
                }
            }
        }
        
        Files.writeString(Paths.get(dbMigrationDir + "V1__Initial_schema.sql"), sql.toString());
        
        // Generate V2__Create_admin_user.sql
        String adminMigration = """
            -- Create admin user
            INSERT INTO users (username, password, roles) VALUES
            ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN')
            ON CONFLICT (username) DO NOTHING;
            """;
        
        Files.writeString(Paths.get(dbMigrationDir + "V2__Create_admin_user.sql"), adminMigration);
    }
    
    private String createUsersTable() {
        return """
            CREATE TABLE users (
                id BIGSERIAL PRIMARY KEY,
                username VARCHAR(255) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                roles VARCHAR(255) NOT NULL
            );
            """;
    }
    
    private String createTableStatement(String tableName, 
                                         List<Map<String, Object>> attrs, 
                                         List<Map<String, Object>> rels) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(tableName).append(" (\n");
        sb.append("    id BIGSERIAL PRIMARY KEY,\n");
        
        // Add columns from attributes
        if (attrs != null) {
            for (Map<String, Object> attr : attrs) {
                if (attr == null) continue;
                
                String fieldName = (String) attr.get("name");
                if (fieldName == null || fieldName.isBlank() || fieldName.equalsIgnoreCase("id")) continue;
                
                String fieldType = mapToSqlType((String) attr.get("type"));
                String columnName = fieldName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
                
                sb.append("    ").append(columnName).append(" ").append(fieldType).append(" NOT NULL,\n");
            }
        }
        
        // Add foreign keys for ManyToOne relationships
        if (rels != null) {
            for (Map<String, Object> rel : rels) {
                if (rel == null) continue;
                
                String type = (String) rel.get("type");
                String target = (String) rel.get("name");
                
                if (type != null && target != null && type.equalsIgnoreCase("manytoone")) {
                    String targetTable = target.toLowerCase().replaceAll("[^a-z0-9_]", "_");
                    String columnName = targetTable + "_id";
                    sb.append("    ").append(columnName).append(" BIGINT");
                    sb.append(" REFERENCES ").append(targetTable).append("(id),\n");
                }
            }
        }
        
        // Remove last comma and newline, close the statement
        String result = sb.toString();
        if (result.endsWith(",\n")) {
            result = result.substring(0, result.length() - 2) + "\n";
        }
        
        result += ");\n";
        
        // Add indexes for better performance
        result += "\n-- Create indexes for " + tableName + "\n";
        result += "CREATE INDEX idx_" + tableName + "_id ON " + tableName + "(id);\n";
        
        return result;
    }
    
    private String createJoinTable(String sourceTable, String targetTable) {
        String joinTableName = sourceTable + "_" + targetTable;
        
        // Ensure consistent ordering
        if (joinTableName.compareTo(targetTable + "_" + sourceTable) > 0) {
            String temp = sourceTable;
            // Swap would happen here but we keep naming consistent
        }
        
        return """
            CREATE TABLE %s (
                %s_id BIGINT NOT NULL REFERENCES %s(id) ON DELETE CASCADE,
                %s_id BIGINT NOT NULL REFERENCES %s(id) ON DELETE CASCADE,
                PRIMARY KEY (%s_id, %s_id)
            );
            
            CREATE INDEX idx_%s_%s_id ON %s(%s_id);
            CREATE INDEX idx_%s_%s_id ON %s(%s_id);
            """.formatted(
                joinTableName,
                sourceTable, sourceTable,
                targetTable, targetTable,
                sourceTable, targetTable,
                joinTableName, sourceTable, joinTableName, sourceTable,
                joinTableName, targetTable, joinTableName, targetTable
            );
    }
    
    private String mapToSqlType(String type) {
        if (type == null) return "VARCHAR(255)";
        
        return switch (type.toLowerCase()) {
            case "string" -> "VARCHAR(255)";
            case "int", "integer" -> "INTEGER";
            case "long" -> "BIGINT";
            case "boolean" -> "BOOLEAN";
            case "double" -> "DOUBLE PRECISION";
            case "date" -> "TIMESTAMP";
            case "localdate", "localdatetime" -> "TIMESTAMP";
            case "bigdecimal" -> "DECIMAL(19,2)";
            default -> "VARCHAR(255)";
        };
    }
}
