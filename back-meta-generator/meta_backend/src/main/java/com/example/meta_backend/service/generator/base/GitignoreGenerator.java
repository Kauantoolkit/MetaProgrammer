package com.example.meta_backend.service.generator.base;

import java.io.IOException;
import java.nio.file.*;

public class GitignoreGenerator {
    private static final String BASE_DIR = "generated_app/";

    public void generateGitignore() throws IOException {
        String gitignore = """
            /target/
            /.idea/
            /.classpath
            /.project
            /.settings/
            *.iml
            *.log
            """;
        Files.writeString(Paths.get(BASE_DIR + ".gitignore"), gitignore);
    }
}
