package com.example.meta_backend.service.generator.base;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema;
import org.springframework.stereotype.Component;

@Component
public class MainClassGenerator {

    public void generate(AppSchema schema, ProjectWriter out) {
        String className = schema.className() + "Application";
        String content = """
            package @PKG@;

            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;
            import org.springframework.boot.autoconfigure.domain.EntityScan;
            import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
            import org.springframework.scheduling.annotation.EnableAsync;

            // As autoconfigurações do Basilisk declaram @EntityScan/@EnableJpaRepositories nos pacotes delas, o que
            // desliga a varredura padrão do app: os pacotes do sistema precisam ser ditos aqui.
            // @EnableAsync: o basilisk-audit grava o log de auditoria em segundo plano.
            @SpringBootApplication
            @EntityScan(basePackages = "@PKG@.entity")
            @EnableJpaRepositories(basePackages = "@PKG@.repository")
            @EnableAsync
            public class @CLASS@ {

                public static void main(String[] args) {
                    SpringApplication.run(@CLASS@.class, args);
                }
            }
            """.replace("@PKG@", ProjectWriter.PACKAGE).replace("@CLASS@", className);
        out.java("", className, content);
    }
}
