package com.example.meta_backend.service.generator.base;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FieldSpec;
import com.example.meta_backend.service.generator.model.AppSchema.ManyToManySpec;
import com.example.meta_backend.service.generator.model.AppSchema.RefSpec;
import org.springframework.stereotype.Component;

/**
 * V1 com as tabelas do sistema (as do Basilisk nascem pelo Hibernate). As FKs vão no fim, depois de todas as
 * tabelas existirem, porque a ordem do payload não respeita dependência. "unique" vale por tenant.
 */
@Component
public class FlywayMigrationGenerator {

    public void generate(AppSchema schema, ProjectWriter out) {
        StringBuilder tables = new StringBuilder();
        StringBuilder constraints = new StringBuilder();

        for (EntitySpec e : schema.entities()) {
            tables.append("CREATE TABLE ").append(e.table()).append(" (\n")
                  .append("    id UUID PRIMARY KEY,\n")
                  .append("    tenant_id UUID NOT NULL");
            for (FieldSpec f : e.fields()) {
                tables.append(",\n    ").append(f.column()).append(' ').append(f.sqlType());
                if (f.required()) tables.append(" NOT NULL");
            }
            for (RefSpec r : e.refs()) {
                tables.append(",\n    ").append(r.column()).append(" UUID");
                if (r.required()) tables.append(" NOT NULL");
            }
            if (e.has("timestamps")) {
                tables.append(",\n    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()")
                      .append(",\n    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()");
            }
            if (e.has("soft_delete")) tables.append(",\n    deleted_at TIMESTAMP WITH TIME ZONE");
            if (e.has("versioning")) tables.append(",\n    version BIGINT NOT NULL DEFAULT 0");
            for (FieldSpec f : e.fields()) {
                if (f.unique()) {
                    tables.append(",\n    CONSTRAINT uk_").append(e.table()).append('_').append(f.column())
                          .append(" UNIQUE (tenant_id, ").append(f.column()).append(')');
                }
            }
            for (RefSpec r : e.refs()) {
                if (r.unique()) {
                    tables.append(",\n    CONSTRAINT uk_").append(e.table()).append('_').append(r.column())
                          .append(" UNIQUE (").append(r.column()).append(')');
                }
            }
            tables.append("\n);\n\n")
                  .append("CREATE INDEX idx_").append(e.table()).append("_tenant ON ").append(e.table()).append(" (tenant_id);\n");
            for (FieldSpec f : e.fields()) {
                if (f.indexed() && !f.unique()) {
                    tables.append("CREATE INDEX idx_").append(e.table()).append('_').append(f.column())
                          .append(" ON ").append(e.table()).append(" (tenant_id, ").append(f.column()).append(");\n");
                }
            }
            tables.append('\n');

            for (RefSpec r : e.refs()) {
                if (r.toUser()) continue; // usuário é tabela do Basilisk, criada depois pelo Hibernate
                constraints.append("ALTER TABLE ").append(e.table()).append(" ADD CONSTRAINT fk_").append(e.table())
                           .append('_').append(r.column()).append(" FOREIGN KEY (").append(r.column())
                           .append(") REFERENCES ").append(r.targetTable()).append(" (id)")
                           .append(r.cascadeDelete() ? " ON DELETE CASCADE" : "").append(";\n")
                           .append("CREATE INDEX idx_").append(e.table()).append('_').append(r.column())
                           .append(" ON ").append(e.table()).append(" (").append(r.column()).append(");\n");
            }
            for (ManyToManySpec m : e.manyToMany()) {
                tables.append("CREATE TABLE ").append(m.joinTable()).append(" (\n")
                      .append("    ").append(m.ownerColumn()).append(" UUID NOT NULL REFERENCES ").append(e.table())
                      .append(" (id) ON DELETE CASCADE,\n")
                      .append("    ").append(m.targetColumn()).append(" UUID NOT NULL,\n")
                      .append("    PRIMARY KEY (").append(m.ownerColumn()).append(", ").append(m.targetColumn()).append(")\n")
                      .append(");\n\n");
                constraints.append("ALTER TABLE ").append(m.joinTable()).append(" ADD CONSTRAINT fk_")
                           .append(m.joinTable()).append('_').append(m.targetColumn()).append(" FOREIGN KEY (")
                           .append(m.targetColumn()).append(") REFERENCES ").append(m.targetTable())
                           .append(" (id) ON DELETE CASCADE;\n");
            }
        }

        String sql = "-- Gerado pelo MetaProgrammer. Tabelas do Basilisk (tenants, users, roles...) não estão aqui:\n"
                + "-- nascem pelo Hibernate (ddl-auto: update).\n\n"
                + tables
                + (constraints.isEmpty() ? "" : "-- Chaves estrangeiras\n" + constraints);
        out.backend("src/main/resources/db/migration/V1__initial_schema.sql", sql);
    }
}
