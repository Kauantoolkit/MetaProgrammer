package com.example.meta_backend.service.generator.config;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema;
import com.example.meta_backend.service.generator.model.Names;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/** application.yml no formato do estudio-app: Flyway para as tabelas do sistema, Hibernate para as do Basilisk. */
@Component
public class ApplicationPropertiesGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public void generate(AppSchema schema, ProjectWriter out) {
        String db = Names.snake(schema.className());
        String yml = """
            server:
              port: ${SERVER_PORT:8081}

            spring:
              application:
                name: @NAME@
              datasource:
                url: ${DB_URL:jdbc:postgresql://127.0.0.1:5432/@DB@}
                username: ${DB_USER:postgres}
                password: ${DB_PASSWORD:postgres}
              jpa:
                # Tabelas do Basilisk (tenants, users, roles...) nascem pelo Hibernate
                hibernate:
                  ddl-auto: update
                open-in-view: false
              flyway:
                # Se o banco já tiver as tabelas do Basilisk, começa a contar a partir delas
                baseline-on-migrate: true
                baseline-version: 0

            basilisk:
              security:
                jwt:
                  # Chave sorteada na geração, só para rodar local; em qualquer outro lugar, passar JWT_SECRET
                  secret: ${JWT_SECRET:@SECRET@}
                  expiration-ms: 86400000
            """.replace("@NAME@", Names.kebab(schema.className()))
                .replace("@DB@", db)
                .replace("@SECRET@", randomSecret());
        out.backend("src/main/resources/application.yml", yml);
    }

    private static String randomSecret() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
