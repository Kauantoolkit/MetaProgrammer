package com.example.meta_backend.service.generator;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

import com.example.meta_backend.service.generator.base.*;
import com.example.meta_backend.service.generator.entity.*;
import com.example.meta_backend.service.generator.layer.*;
import com.example.meta_backend.service.generator.config.*;
import com.example.meta_backend.service.generator.security.*;

@Service
public class CodeGeneratorService {

    private final BaseStructureGenerator baseStructureGenerator = new BaseStructureGenerator();
    private final PomGenerator pomGenerator = new PomGenerator();
    private final MainClassGenerator mainClassGenerator = new MainClassGenerator();
    private final GitignoreGenerator gitignoreGenerator = new GitignoreGenerator();
    private final ApplicationPropertiesGenerator appPropsGenerator = new ApplicationPropertiesGenerator();

    private final EntityGenerator entityGenerator = new EntityGenerator();
    private final RepositoryGenerator repositoryGenerator = new RepositoryGenerator();
    private final DtoGenerator dtoGenerator = new DtoGenerator();
    private final ServiceGenerator serviceGenerator = new ServiceGenerator();
    private final ControllerGenerator controllerGenerator = new ControllerGenerator();

    private final SecurityGenerator securityGenerator = new SecurityGenerator();
    private final GlobalExceptionHandlerGenerator exceptionHandlerGenerator = new GlobalExceptionHandlerGenerator();

    private final ThymeleafFrontGenerator thymeleafGenerator = new ThymeleafFrontGenerator();

    private static final String PROJECT_NAME = "generated_app";
    private static final String BASE_DIR = PROJECT_NAME + "/src/main/java/com/metagen/backend/generated/";

    public void generateEntities(String appName, List<Map<String, Object>> entities) {
        try {
            // Cria a estrutura base do projeto
            baseStructureGenerator.createBaseStructure(appName);

            // Gera arquivos fundamentais do projeto
            pomGenerator.generatePom(appName);
            mainClassGenerator.generateMainClass(appName);
            gitignoreGenerator.generateGitignore(appName);
            appPropsGenerator.generateApplicationProperties(appName);

            String baseDir = appName + "/src/main/java/com/metagen/backend/generated/";

            // Sempre gera entidade User para autenticação
            entityGenerator.generateUserEntity(baseDir);
            repositoryGenerator.generateUserRepository(baseDir);

            // Gera entidades e camadas associadas
            for (Map<String, Object> entity : entities) {
                String name = (String) entity.get("name");
                List<Map<String, Object>> attrs = (List<Map<String, Object>>) entity.getOrDefault("attributes", List.of());
                List<Map<String, Object>> rels = (List<Map<String, Object>>) entity.getOrDefault("relations", List.of());

                // Sempre gera entidade, repositório, DTO e serviço
                entityGenerator.generateEntity(baseDir, name, attrs, rels, appName);
                repositoryGenerator.generateRepository(baseDir, name);
                dtoGenerator.generateDTOs(baseDir, name, attrs);
                serviceGenerator.generateService(baseDir, name, entity);

                // Gera controller apenas se houver endpoints configurados
                Map<String, Object> api = (Map<String, Object>) entity.getOrDefault("api", Map.of());
                List<String> endpoints = (List<String>) api.getOrDefault("endpoints", List.of());
                if (!endpoints.isEmpty()) {
                    controllerGenerator.generateController(baseDir, name, attrs, entity);
                }
            }

            // Gera camadas adicionais
            securityGenerator.generateSecurityClasses(baseDir);
            exceptionHandlerGenerator.generateGlobalExceptionHandler(baseDir);

            // Gera front-end Thymeleaf
            thymeleafGenerator.generateTemplates(entities, appName);

            // Gera controller do backoffice
            BackofficeControllerGenerator backofficeControllerGenerator = new BackofficeControllerGenerator();
            backofficeControllerGenerator.generateBackofficeController(entities, appName);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar backend: " + e.getMessage(), e);
        }
    }
}
