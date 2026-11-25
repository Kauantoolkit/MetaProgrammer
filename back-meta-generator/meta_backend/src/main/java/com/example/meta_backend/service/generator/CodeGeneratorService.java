package com.example.meta_backend.service.generator;

import com.example.meta_backend.service.generator.base.FlywayMigrationGenerator;
import com.example.meta_backend.service.generator.base.GitignoreGenerator;
import com.example.meta_backend.service.generator.base.MainClassGenerator;
import com.example.meta_backend.service.generator.base.PomGenerator;
import com.example.meta_backend.service.generator.config.ApplicationPropertiesGenerator;
import com.example.meta_backend.service.generator.entity.DtoGenerator;
import com.example.meta_backend.service.generator.entity.EntityGenerator;
import com.example.meta_backend.service.generator.entity.RepositoryGenerator;
import com.example.meta_backend.service.generator.functionality.ClaudeTodoGenerator;
import com.example.meta_backend.service.generator.frontend.EntityPageGenerator;
import com.example.meta_backend.service.generator.frontend.FrontendScaffoldGenerator;
import com.example.meta_backend.service.generator.frontend.ShellConfigGenerator;
import com.example.meta_backend.service.generator.functionality.FunctionalityGenerator;
import com.example.meta_backend.service.generator.layer.ControllerGenerator;
import com.example.meta_backend.service.generator.layer.ServiceGenerator;
import com.example.meta_backend.service.generator.model.AppSchema;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FunctionalitySpec;
import com.example.meta_backend.service.generator.model.Names;
import com.example.meta_backend.service.generator.model.SchemaParser;
import com.example.meta_backend.service.generator.security.SecurityGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Gera um sistema Spring Boot sobre o Basilisk a partir do payload da tela. Login, usuários, tenants e permissões
 * vêm das libs; daqui sai só o domínio.
 */
@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    private final SchemaParser schemaParser;
    private final PomGenerator pomGenerator;
    private final MainClassGenerator mainClassGenerator;
    private final GitignoreGenerator gitignoreGenerator;
    private final ApplicationPropertiesGenerator appPropsGenerator;
    private final SecurityGenerator securityGenerator;
    private final FlywayMigrationGenerator flywayMigrationGenerator;
    private final EntityGenerator entityGenerator;
    private final RepositoryGenerator repositoryGenerator;
    private final DtoGenerator dtoGenerator;
    private final ServiceGenerator serviceGenerator;
    private final ControllerGenerator controllerGenerator;
    private final FunctionalityGenerator functionalityGenerator;
    private final ClaudeTodoGenerator claudeTodoGenerator;
    private final FrontendScaffoldGenerator frontendScaffoldGenerator;
    private final ShellConfigGenerator shellConfigGenerator;
    private final EntityPageGenerator entityPageGenerator;

    @Value("${generator.output-dir:generated-apps}")
    private String outputDir;

    public Result generateApplication(Map<String, Object> payload) {
        AppSchema schema = schemaParser.parse(payload);

        Path root = Path.of(outputDir).toAbsolutePath().resolve(Names.kebab(schema.className()));
        // Gerar por cima apagaria o que já foi implementado nos TODO(claude); pasta existente é recusada
        if (Files.exists(root)) {
            throw new IllegalStateException("A pasta " + root + " já existe. Apague ou troque o appName.");
        }
        ProjectWriter out = new ProjectWriter(root);

        pomGenerator.generate(schema, out);
        mainClassGenerator.generate(schema, out);
        gitignoreGenerator.generate(out);
        appPropsGenerator.generate(schema, out);
        securityGenerator.generate(out);
        flywayMigrationGenerator.generate(schema, out);

        for (EntitySpec e : schema.entities()) {
            entityGenerator.generate(e, out);
            repositoryGenerator.generate(e, out);
            dtoGenerator.generate(e, out);
            serviceGenerator.generate(e, out);
            controllerGenerator.generate(e, out);
        }
        for (FunctionalitySpec f : schema.functionalities()) {
            functionalityGenerator.generate(schema, f, out);
        }
        frontendScaffoldGenerator.generate(schema, out);
        shellConfigGenerator.generate(schema, out);
        for (EntitySpec e : schema.entities()) {
            entityPageGenerator.generate(schema, e, out);
        }
        claudeTodoGenerator.generate(schema, out);

        return new Result(root.toString(), schema.entities().size(), schema.functionalities().size(),
                schema.notes().size());
    }

    public record Result(String path, int entities, int functionalities, int pendingNotes) {
    }
}
