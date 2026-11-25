package com.example.meta_backend.service.generator.base;

import com.example.meta_backend.service.generator.ProjectWriter;
import org.springframework.stereotype.Component;

@Component
public class GitignoreGenerator {

    public void generate(ProjectWriter out) {
        String gitignore = """
            backend/target/
            frontend/node_modules/
            frontend/dist/
            frontend/.angular/
            .idea/
            .vscode/
            *.iml
            *.log
            .env
            """;
        out.write(".gitignore", gitignore);
    }
}
