package com.example.meta_backend.service.generator.frontend;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.Names;
import org.springframework.stereotype.Component;

/** Menu do sistema: um módulo do ng-shell por entidade com API, protegido pela permissão de leitura dela. */
@Component
public class ShellConfigGenerator {

    public void generate(AppSchema schema, ProjectWriter out) {
        StringBuilder imports = new StringBuilder("import { ShellConfig } from '@basilisk/ng-shell';\n\n");
        StringBuilder modules = new StringBuilder();
        for (EntitySpec e : schema.entities()) {
            if (!e.hasApi()) continue;
            imports.append("import { ").append(EntityPageGenerator.pageClass(e)).append(" } from '")
                   .append(EntityPageGenerator.pageImport(e)).append("';\n");
            String label = EntityPageGenerator.label(e);
            modules.append("    {\n")
                   .append("      id: '").append(Names.kebab(e.className())).append("',\n")
                   .append("      name: '").append(label).append("',\n")
                   .append("      basePath: '/").append(e.resource()).append("',\n")
                   .append("      requiredPermission: '").append(e.resource()).append(":read',\n")
                   .append("      routes: [{ path: '', component: ").append(EntityPageGenerator.pageClass(e)).append(" }],\n")
                   .append("      navItems: [{ label: '").append(label).append("', path: '/").append(e.resource()).append("' }],\n")
                   .append("    },\n");
        }
        String code = imports + "\n"
                + "export const shellConfig: ShellConfig = {\n"
                + "  branding: { name: '" + schema.appName().replace("'", "\\'") + "' },\n"
                + "  modules: [\n" + modules + "  ],\n"
                + "};\n";
        out.frontend("src/app/shell.config.ts", code);
    }
}
