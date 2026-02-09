package com.example.meta_backend.service.generator.base;

import java.io.IOException;
import java.nio.file.*;

public class GitignoreGenerator {

    public void generateGitignore(String appName) throws IOException {
        Path rootDir = Paths.get(System.getProperty("user.dir"));
        String gitignore = """
            /target/
            /.idea/
            /.classpath
            /.project
            /.settings/
            *.iml
            *.log
            """;
        Files.writeString(rootDir.resolve(appName + "/.gitignore"), gitignore);
    }
}
