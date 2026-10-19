package com.example.meta_backend.service.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.security.SecureRandom;

@Service
public class CodeGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(CodeGeneratorService.class);

    private final BaseStructureGenerator baseStructureGenerator;
    private final FlywayMigrationGenerator flywayMigrationGenerator;
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
    private final AdminUserInitializerGenerator adminUserInitializerGenerator;

    private final FunctionalityDtoGenerator functionalityDtoGenerator;
    private final FunctionalityServiceInterfaceGenerator functionalityServiceInterfaceGenerator;
    private final FunctionalityServiceImplGenerator functionalityServiceImplGenerator;
    private final FunctionalityControllerGenerator functionalityControllerGenerator;

    public CodeGeneratorService(
            BaseStructureGenerator baseStructureGenerator,
            FlywayMigrationGenerator flywayMigrationGenerator,
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
            AdminUserInitializerGenerator adminUserInitializerGenerator,
            FunctionalityDtoGenerator functionalityDtoGenerator,
            FunctionalityServiceInterfaceGenerator functionalityServiceInterfaceGenerator,
            FunctionalityServiceImplGenerator functionalityServiceImplGenerator,
            FunctionalityControllerGenerator functionalityControllerGenerator
    ) {

        this.baseStructureGenerator = baseStructureGenerator;
        this.flywayMigrationGenerator = flywayMigrationGenerator;
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
        this.adminUserInitializerGenerator = adminUserInitializerGenerator;
        this.functionalityDtoGenerator = functionalityDtoGenerator;

        this.functionalityServiceInterfaceGenerator = functionalityServiceInterfaceGenerator;
        this.functionalityServiceImplGenerator = functionalityServiceImplGenerator;
        this.functionalityControllerGenerator = functionalityControllerGenerator;
    }

    public void generateApplication(String appName, List<Map<String, Object>> entities, List<Map<String, Object>> functionalities, boolean useAi) {
        String step = "inicialização";
        try {
            String adminPassword = generateRandomPassword(16);
            String javaClassName = AppNameUtils.toClassName(appName);

            step = "estrutura de diretórios";
            generateProjectStructure(appName);

            step = "arquivos base (pom, main class, properties)";
            generateCoreFiles(appName, adminPassword, javaClassName);

            String baseDir = buildBaseDir(appName);

            step = "entidade e repositório Users (segurança)";
            generateSecurityUsers(baseDir);

            step = "camadas de domínio (entities, repositories, services, controllers)";
            generateDomainLayers(baseDir, entities, appName);

            step = "migrations Flyway";
            flywayMigrationGenerator.generateFlywayMigration(appName, entities);

            step = "funcionalidades customizadas";
            generateFunctionalities(baseDir, functionalities, entities, useAi);

            step = "infraestrutura (security, exception handler)";
            generateInfrastructure(baseDir);

            step = "templates Thymeleaf";
            generateFrontend(entities, appName);

            step = "controllers de backoffice";
            generateBackoffice(entities, appName);

            step = "inicializador de admin";
            generateAdminInitializer(baseDir);

            log.info("Aplicação '{}' gerada com sucesso. Defina a variável de ambiente ADMIN_PASSWORD antes de fazer deploy em produção.", appName);

        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("Falha ao gerar aplicação '%s' na etapa '%s': %s", appName, step, e.getMessage()), e);
        }
    }
    
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


    private void generateProjectStructure(String appName) throws IOException {
        baseStructureGenerator.createBaseStructure(appName);
    }

    private void generateCoreFiles(String appName, String adminPassword, String javaClassName) throws IOException {
        pomGenerator.generatePom(appName);
        mainClassGenerator.generateMainClass(appName);
        gitignoreGenerator.generateGitignore(appName);
        appPropsGenerator.generateApplicationProperties(appName, adminPassword, javaClassName);
    }


    private String buildBaseDir(String appName) {
        return appName + "/src/main/java/com/metagen/backend/generated/";
    }

    private void generateSecurityUsers(String baseDir) throws IOException {
        entityGenerator.generateUsersEntity(baseDir);
        repositoryGenerator.generateUsersRepository(baseDir);
    }

    private void generateDomainLayers(String baseDir, List<Map<String, Object>> entities, String appName) throws IOException {
        for (Map<String, Object> entity : entities) {
            String name = (String) entity.get("name");
            
            //skippa pra n sobrescrever
            if ("Users".equalsIgnoreCase(name)) {
                continue;
            }

            List<Map<String, Object>> attrs =
                    (List<Map<String, Object>>) entity.getOrDefault("attributes", List.of());

            List<Map<String, Object>> rels =
                    (List<Map<String, Object>>) entity.getOrDefault("relations", List.of());

            List<String> behaviors = (List<String>) entity.getOrDefault("behaviors", List.of());

            entityGenerator.generateEntity(baseDir, name, attrs, rels, behaviors, appName);

            repositoryGenerator.generateRepository(baseDir, name, attrs);
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
    
    private void generateAdminInitializer(String baseDir) throws IOException {
        adminUserInitializerGenerator.generateAdminUserInitializer(baseDir);
    }


    private void generateFrontend(List<Map<String, Object>> entities, String appName) throws IOException {
        thymeleafGenerator.generateTemplates(entities, appName);
    }

    private void generateBackoffice(List<Map<String, Object>> entities, String appName) throws IOException {
        backofficeControllerGenerator.generateBackofficeController(entities, appName);
    }

    private void generateFunctionalities(String baseDir, List<Map<String, Object>> functionalities, List<Map<String, Object>> entities, boolean useAi) throws IOException {
        if (!functionalities.isEmpty()) {
            functionalityDtoGenerator.generateFunctionalityDTOs(baseDir, functionalities);
            functionalityServiceInterfaceGenerator.generateFunctionalityServiceInterfaces(baseDir, functionalities);
            functionalityServiceImplGenerator.generateFunctionalityServiceImpls(baseDir, functionalities, entities, useAi);
            functionalityControllerGenerator.generateFunctionalityControllers(baseDir, functionalities);
        }
    }
}
