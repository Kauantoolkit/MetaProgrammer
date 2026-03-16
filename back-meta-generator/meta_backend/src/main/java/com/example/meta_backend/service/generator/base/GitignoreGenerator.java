package com.example.meta_backend.service.generator.base;

import java.io.IOException;
import java.nio.file.*;

import org.springframework.stereotype.Component;

@Component
public class GitignoreGenerator {

    public void generateGitignore(String appName) throws IOException {
        String gitignore = """
            /target/
            /.idea/
            /.classpath
            /.project
            /.settings/
            *.iml
            *.log
            """;
        Files.writeString(Paths.get(appName + "/.gitignore"), gitignore);
    }
}
