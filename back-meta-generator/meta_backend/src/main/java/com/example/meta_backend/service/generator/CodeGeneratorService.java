package com.example.meta_backend.service.generator;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.meta_backend.service.generator.base.*;
import com.example.meta_backend.service.generator.entity.*;
import com.example.meta_backend.service.generator.layer.*;
import com.example.meta_backend.service.generator.config.*;
import com.example.meta_backend.service.generator.security.*;
import com.example.meta_backend.service.generator.functionality.*;

@Service
public class CodeGeneratorService {

    private final BaseStructureGenerator baseStructureGenerator;
    private final BaseStructureGenerator flywayGenerator;
    private final PomGenerator pomGenerator;
    private final MainClassGenerator mainClassGenerator;
    private final GitignoreGenerator gitignoreGenerator;
    private final ApplicationPropertiesGenerator appPropsGenerator;

    private final EntityGenerator entityGenerator;
    private final RepositoryGenerator repositoryGenerator;
    private final DtoGenerator dtoGenerator;
    private final ServiceGenerator serviceGenerator;
    private final ControllerGenerator controllerGenerator;

    private final SecurityGenerator securityGenerator;
    private final GlobalExceptionHandlerGenerator exceptionHandlerGenerator;
    private final ThymeleafFrontGenerator thymeleafGenerator;
    private final BackofficeControllerGenerator backofficeControllerGenerator;

    private final FunctionalityDtoGenerator functionalityDtoGenerator;
    private final FunctionalityServiceInterfaceGenerator functionalityServiceInterfaceGenerator;
    private final FunctionalityServiceImplGenerator functionalityServiceImplGenerator;
    private final FunctionalityControllerGenerator functionalityControllerGenerator;

    public CodeGeneratorService(
            BaseStructureGenerator baseStructureGenerator,
            PomGenerator pomGenerator,
            MainClassGenerator mainClassGenerator,
            GitignoreGenerator gitignoreGenerator,
            ApplicationPropertiesGenerator appPropsGenerator,
            EntityGenerator entityGenerator,
            RepositoryGenerator repositoryGenerator,
            DtoGenerator dtoGenerator,
            ServiceGenerator serviceGenerator,
            ControllerGenerator controllerGenerator,
            SecurityGenerator securityGenerator,
            GlobalExceptionHandlerGenerator exceptionHandlerGenerator,
            ThymeleafFrontGenerator thymeleafGenerator,
            BackofficeControllerGenerator backofficeControllerGenerator,
            FunctionalityDtoGenerator functionalityDtoGenerator,
            FunctionalityServiceInterfaceGenerator functionalityServiceInterfaceGenerator,
            FunctionalityServiceImplGenerator functionalityServiceImplGenerator,
            FunctionalityControllerGenerator functionalityControllerGenerator
    ) {
        this.baseStructureGenerator = baseStructureGenerator;
        this.flywayGenerator = baseStructureGenerator;
        this.pomGenerator = pomGenerator;
        this.mainClassGenerator = mainClassGenerator;
        this.gitignoreGenerator = gitignoreGenerator;
        this.appPropsGenerator = appPropsGenerator;
        this.entityGenerator = entityGenerator;
        this.repositoryGenerator = repositoryGenerator;
        this.dtoGenerator = dtoGenerator;
        this.serviceGenerator = serviceGenerator;
        this.controllerGenerator = controllerGenerator;
        this.securityGenerator = securityGenerator;
        this.exceptionHandlerGenerator = exceptionHandlerGenerator;
        this.thymeleafGenerator = thymeleafGenerator;
        this.backofficeControllerGenerator = backofficeControllerGenerator;
        this.functionalityDtoGenerator = functionalityDtoGenerator;
        this.functionalityServiceInterfaceGenerator = functionalityServiceInterfaceGenerator;
        this.functionalityServiceImplGenerator = functionalityServiceImplGenerator;
        this.functionalityControllerGenerator = functionalityControllerGenerator;
    }

    public void generateApplication(String appName, List<Map<String, Object>> entities, List<Map<String, Object>> functionalities) {
        try {
            generateProjectStructure(appName);
            generateCoreFiles(appName);

            String baseDir = buildBaseDir(appName);

            generateSecurityUser(baseDir);
            generateDomainLayers(baseDir, entities, appName);
            generateFunctionalities(baseDir, functionalities);
            generateInfrastructure(baseDir);
            generateFrontend(entities, appName);
            generateBackoffice(entities, appName);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar aplicação: " + e.getMessage(), e);
        }
    }

    private void generateProjectStructure(String appName) throws IOException {
        baseStructureGenerator.createBaseStructure(appName);
    }

    private void generateCoreFiles(String appName) throws IOException {
        pomGenerator.generatePom(appName);
        mainClassGenerator.generateMainClass(appName);
        gitignoreGenerator.generateGitignore(appName);
        appPropsGenerator.generateApplicationProperties(appName);
        flywayGenerator.generateFlywayMigration(appName);
    }

    private String buildBaseDir(String appName) {
        return appName + "/src/main/java/com/metagen/backend/generated/";
    }

    private void generateSecurityUser(String baseDir) throws IOException {
        entityGenerator.generateUserEntity(baseDir);
        repositoryGenerator.generateUserRepository(baseDir);
    }

    private void generateDomainLayers(String baseDir, List<Map<String, Object>> entities, String appName) throws IOException {
        for (Map<String, Object> entity : entities) {
            String name = (String) entity.get("name");

            List<Map<String, Object>> attrs =
                    (List<Map<String, Object>>) entity.getOrDefault("attributes", List.of());

            List<Map<String, Object>> rels =
                    (List<Map<String, Object>>) entity.getOrDefault("relations", List.of());

            entityGenerator.generateEntity(baseDir, name, attrs, rels, appName);
            repositoryGenerator.generateRepository(baseDir, name);
            dtoGenerator.generateDTOs(baseDir, name, attrs);
            serviceGenerator.generateService(baseDir, name, entity);

            generateControllerIfNeeded(baseDir, name, attrs, entity);
        }
    }

    private void generateControllerIfNeeded(String baseDir, String name, List<Map<String, Object>> attrs, Map<String, Object> entity) throws IOException {
        Map<String, Object> api = (Map<String, Object>) entity.getOrDefault("api", Map.of());
        List<String> endpoints = (List<String>) api.getOrDefault("endpoints", List.of());

        if (!endpoints.isEmpty()) {
            controllerGenerator.generateController(baseDir, name, attrs, entity);
        }
    }

    private void generateInfrastructure(String baseDir) throws IOException {
        securityGenerator.generateSecurityClasses(baseDir);
        exceptionHandlerGenerator.generateGlobalExceptionHandler(baseDir);
    }

    private void generateFrontend(List<Map<String, Object>> entities, String appName) throws IOException {
        thymeleafGenerator.generateTemplates(entities, appName);
    }

    private void generateBackoffice(List<Map<String, Object>> entities, String appName) throws IOException {
        backofficeControllerGenerator.generateBackofficeController(entities, appName);
    }

    private void generateFunctionalities(String baseDir, List<Map<String, Object>> functionalities) throws IOException {
        if (!functionalities.isEmpty()) {
            functionalityDtoGenerator.generateFunctionalityDTOs(baseDir, functionalities);
            functionalityServiceInterfaceGenerator.generateFunctionalityServiceInterfaces(baseDir, functionalities);
            functionalityServiceImplGenerator.generateFunctionalityServiceImpls(baseDir, functionalities);
            functionalityControllerGenerator.generateFunctionalityControllers(baseDir, functionalities);
        }
    }
}
