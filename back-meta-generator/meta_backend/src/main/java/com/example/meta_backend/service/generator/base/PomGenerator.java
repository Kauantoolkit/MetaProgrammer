package com.example.meta_backend.service.generator.base;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema;
import com.example.meta_backend.service.generator.model.Names;
import org.springframework.stereotype.Component;

/** pom do sistema gerado: o mesmo do estudio-app (app de referência do Basilisk). */
@Component
public class PomGenerator {

    static final String BASILISK_VERSION = "0.3.1";

    public void generate(AppSchema schema, ProjectWriter out) {
        String artifact = Names.kebab(schema.className());
        String pom = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>

                <parent>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-parent</artifactId>
                    <version>3.3.5</version>
                    <relativePath/>
                </parent>

                <groupId>com.metagen.backend</groupId>
                <artifactId>@ARTIFACT@</artifactId>
                <version>0.1.0</version>
                <name>@ARTIFACT@</name>
                <description>Gerado pelo MetaProgrammer sobre o Basilisk</description>

                <properties>
                    <java.version>21</java.version>
                    <basilisk.version>@BASILISK@</basilisk.version>
                </properties>

                <!-- Libs Basilisk: pacote privado no GitHub Packages (ou já instaladas no ~/.m2) -->
                <repositories>
                    <repository>
                        <id>github</id>
                        <url>https://maven.pkg.github.com/Kauantoolkit/basilisk-libs-java</url>
                    </repository>
                </repositories>

                <dependencies>
                    <!-- Basilisk: user puxa tenant, security, permission e core -->
                    <dependency>
                        <groupId>com.basilisk</groupId>
                        <artifactId>basilisk-user</artifactId>
                        <version>${basilisk.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>com.basilisk</groupId>
                        <artifactId>basilisk-audit</artifactId>
                        <version>${basilisk.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>com.basilisk</groupId>
                        <artifactId>basilisk-web</artifactId>
                        <version>${basilisk.version}</version>
                    </dependency>

                    <dependency>
                        <groupId>org.postgresql</groupId>
                        <artifactId>postgresql</artifactId>
                        <scope>runtime</scope>
                    </dependency>
                    <!-- Tabelas do sistema por migration; as do Basilisk pelo Hibernate -->
                    <dependency>
                        <groupId>org.flywaydb</groupId>
                        <artifactId>flyway-core</artifactId>
                    </dependency>
                    <dependency>
                        <groupId>org.flywaydb</groupId>
                        <artifactId>flyway-database-postgresql</artifactId>
                    </dependency>

                    <dependency>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <optional>true</optional>
                    </dependency>

                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-test</artifactId>
                        <scope>test</scope>
                    </dependency>
                </dependencies>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-maven-plugin</artifactId>
                            <configuration>
                                <excludes>
                                    <exclude>
                                        <groupId>org.projectlombok</groupId>
                                        <artifactId>lombok</artifactId>
                                    </exclude>
                                </excludes>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """.replace("@ARTIFACT@", artifact).replace("@BASILISK@", BASILISK_VERSION);
        out.backend("pom.xml", pom);
    }
}
