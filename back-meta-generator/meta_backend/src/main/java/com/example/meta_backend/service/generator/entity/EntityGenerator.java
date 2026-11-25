package com.example.meta_backend.service.generator.entity;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FieldSpec;
import com.example.meta_backend.service.generator.model.AppSchema.ManyToManySpec;
import com.example.meta_backend.service.generator.model.AppSchema.RefSpec;
import com.example.meta_backend.service.generator.model.FieldType;
import org.springframework.stereotype.Component;

/**
 * Entidade sobre TenantEntity (o tenant_id entra sozinho no PrePersist), id UUID e referências como UUID solto,
 * como no estudio-app. As colunas repetem o que a migration cria, para o ddl-auto=update não mexer nelas.
 */
@Component
public class EntityGenerator {

    public void generate(EntitySpec e, ProjectWriter out) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(ProjectWriter.pkg("entity")).append(";\n\n")
          .append("import com.basilisk.tenant.entity.TenantEntity;\n")
          .append("import jakarta.persistence.*;\n")
          .append("import lombok.Getter;\n")
          .append("import lombok.NoArgsConstructor;\n")
          .append("import lombok.Setter;\n\n")
          .append("import java.math.BigDecimal;\n")
          .append("import java.time.*;\n")
          .append("import java.util.*;\n\n")
          .append("@Entity\n")
          .append("@Table(name = \"").append(e.table()).append("\")\n")
          .append("@Getter\n@Setter\n@NoArgsConstructor\n");

        if (e.has("soft_delete")) {
            String where = e.has("versioning") ? "id = ? AND version = ?" : "id = ?";
            sb.append("@org.hibernate.annotations.SQLDelete(sql = \"UPDATE ").append(e.table())
              .append(" SET deleted_at = now() WHERE ").append(where).append("\")\n")
              .append("@org.hibernate.annotations.SQLRestriction(\"deleted_at IS NULL\")\n");
        }

        sb.append("public class ").append(e.className()).append(" extends TenantEntity {\n\n")
          .append("    @Id\n")
          .append("    @GeneratedValue(strategy = GenerationType.UUID)\n")
          .append("    private UUID id;\n\n");

        for (FieldSpec f : e.fields()) {
            if (f.type() == FieldType.ENUM) sb.append("    @Enumerated(EnumType.STRING)\n");
            sb.append("    @Column(name = \"").append(f.column()).append("\"");
            if (f.required()) sb.append(", nullable = false");
            if (f.type() == FieldType.STRING) sb.append(", length = ").append(f.length());
            if (f.type() == FieldType.TEXT) sb.append(", columnDefinition = \"text\"");
            if (f.type() == FieldType.NUMBER) sb.append(", precision = 19, scale = 4");
            sb.append(")\n")
              .append("    private ").append(f.javaType()).append(" ").append(f.name()).append(";\n\n");
        }

        for (RefSpec r : e.refs()) {
            sb.append("    /** ").append(r.toUser() ? "Usuário (basilisk-user)" : r.targetClass()).append(" */\n")
              .append("    @Column(name = \"").append(r.column()).append("\"")
              .append(r.required() ? ", nullable = false" : "").append(")\n")
              .append("    private UUID ").append(r.name()).append(";\n\n");
        }

        for (ManyToManySpec m : e.manyToMany()) {
            sb.append("    @ElementCollection\n")
              .append("    @CollectionTable(name = \"").append(m.joinTable())
              .append("\", joinColumns = @JoinColumn(name = \"").append(m.ownerColumn()).append("\"))\n")
              .append("    @Column(name = \"").append(m.targetColumn()).append("\")\n")
              .append("    private Set<UUID> ").append(m.name()).append(" = new HashSet<>();\n\n");
        }

        if (e.has("timestamps")) {
            sb.append("    @org.hibernate.annotations.CreationTimestamp\n")
              .append("    @Column(name = \"created_at\", nullable = false, updatable = false)\n")
              .append("    private Instant createdAt;\n\n")
              .append("    @org.hibernate.annotations.UpdateTimestamp\n")
              .append("    @Column(name = \"updated_at\", nullable = false)\n")
              .append("    private Instant updatedAt;\n\n");
        }
        if (e.has("soft_delete")) {
            sb.append("    @Column(name = \"deleted_at\")\n")
              .append("    private Instant deletedAt;\n\n");
        }
        if (e.has("versioning")) {
            sb.append("    @Version\n")
              .append("    @Column(name = \"version\", nullable = false)\n")
              .append("    private Long version;\n\n");
        }

        sb.setLength(sb.length() - 1);
        sb.append("}\n");
        out.java("entity", e.className(), sb.toString());

        for (FieldSpec f : e.fields()) {
            if (f.type() == FieldType.ENUM) generateEnum(f, out);
        }
    }

    private void generateEnum(FieldSpec f, ProjectWriter out) {
        String code = "package " + ProjectWriter.pkg("entity") + ";\n\n"
                + "public enum " + f.enumClass() + " {\n"
                + "    " + String.join(",\n    ", f.enumValues()) + "\n"
                + "}\n";
        out.java("entity", f.enumClass(), code);
    }
}
