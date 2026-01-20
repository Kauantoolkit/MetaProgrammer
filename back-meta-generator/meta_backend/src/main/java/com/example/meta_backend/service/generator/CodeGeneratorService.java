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

    public void generateEntities(List<Map<String, Object>> entities) {
        try {
            // Cria a estrutura base do projeto
            baseStructureGenerator.createBaseStructure();

            // Gera arquivos fundamentais do projeto
            pomGenerator.generatePom(PROJECT_NAME);
            mainClassGenerator.generateMainClass(PROJECT_NAME);
            gitignoreGenerator.generateGitignore();
            appPropsGenerator.generateApplicationProperties(PROJECT_NAME);

            // Sempre gera entidade User para autenticação
            entityGenerator.generateUserEntity(BASE_DIR);
            repositoryGenerator.generateUserRepository(BASE_DIR);

            // Gera entidades e camadas associadas
            for (Map<String, Object> entity : entities) {
                String name = (String) entity.get("name");
                List<Map<String, Object>> attrs = (List<Map<String, Object>>) entity.getOrDefault("attributes", List.of());
                List<Map<String, Object>> rels = (List<Map<String, Object>>) entity.getOrDefault("relations", List.of());

                // Sempre gera entidade, repositório, DTO e serviço
                entityGenerator.generateEntity(BASE_DIR, name, attrs, rels);
                repositoryGenerator.generateRepository(BASE_DIR, name);
                dtoGenerator.generateDTOs(BASE_DIR, name, attrs);
                serviceGenerator.generateService(BASE_DIR, name, entity);

                // Gera controller apenas se houver endpoints configurados
                Map<String, Object> api = (Map<String, Object>) entity.getOrDefault("api", Map.of());
                List<String> endpoints = (List<String>) api.getOrDefault("endpoints", List.of());
                if (!endpoints.isEmpty()) {
                    controllerGenerator.generateController(BASE_DIR, name, attrs, entity);
                }
            }

            // Gera camadas adicionais
            securityGenerator.generateSecurityClasses(BASE_DIR);
            exceptionHandlerGenerator.generateGlobalExceptionHandler(BASE_DIR);

            // Gera front-end Thymeleaf
            thymeleafGenerator.generateTemplates(entities);

            // Gera controller do backoffice
            BackofficeControllerGenerator backofficeControllerGenerator = new BackofficeControllerGenerator();
            backofficeControllerGenerator.generateBackofficeController(entities);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar backend: " + e.getMessage(), e);
        }
    }
}
