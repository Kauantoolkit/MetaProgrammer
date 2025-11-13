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

    private static final String PROJECT_NAME = "generated_app";
    private static final String BASE_DIR = PROJECT_NAME + "/src/main/java/com/metagen/backend/generated/";

    public void generateEntities(List<Map<String, Object>> entities) {
        try {
            // Cria a estrutura base do projeto
            baseStructureGenerator.createBaseStructure(); // sem argumentos

            // Gera arquivos fundamentais do projeto
            pomGenerator.generatePom(PROJECT_NAME); // apenas projectName
            mainClassGenerator.generateMainClass(PROJECT_NAME); // apenas projectName
            gitignoreGenerator.generateGitignore(); // sem argumentos
            appPropsGenerator.generateApplicationProperties(PROJECT_NAME); // apenas projectName

            // Gera entidades e camadas associadas
            for (Map<String, Object> entity : entities) {
                String name = (String) entity.get("name");
                List<Map<String, Object>> attrs = (List<Map<String, Object>>) entity.getOrDefault("attributes", List.of());
                List<Map<String, Object>> rels = (List<Map<String, Object>>) entity.getOrDefault("relations", List.of());

                entityGenerator.generateEntity(BASE_DIR, name, attrs, rels);
                repositoryGenerator.generateRepository(BASE_DIR, name);
                dtoGenerator.generateDTOs(BASE_DIR, name, attrs);
                serviceGenerator.generateService(BASE_DIR, name);
                controllerGenerator.generateController(BASE_DIR, name, attrs);
            }

            // Gera camadas adicionais
            securityGenerator.generateSecurityClasses(BASE_DIR);
            exceptionHandlerGenerator.generateGlobalExceptionHandler(BASE_DIR);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar backend: " + e.getMessage(), e);
        }
    }
}
