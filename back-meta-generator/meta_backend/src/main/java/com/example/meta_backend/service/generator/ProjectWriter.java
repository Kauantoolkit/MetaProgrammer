package com.example.meta_backend.service.generator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Escreve os arquivos do sistema gerado: `<app>/backend` (Spring) e `<app>/frontend` (Angular). */
public class ProjectWriter {

    public static final String PACKAGE = "com.metagen.backend.generated";

    private final Path root;

    public ProjectWriter(Path root) {
        this.root = root;
    }

    public Path root() {
        return root;
    }

    /** Classe Java em `PACKAGE.<subPackage>`; subPackage vazio = pacote raiz. */
    public void java(String subPackage, String className, String content) {
        String pkgPath = (PACKAGE + (subPackage.isEmpty() ? "" : "." + subPackage)).replace('.', '/');
        write("backend/src/main/java/" + pkgPath + "/" + className + ".java", content);
    }

    public void backend(String relativePath, String content) {
        write("backend/" + relativePath, content);
    }

    public void frontend(String relativePath, String content) {
        write("frontend/" + relativePath, content);
    }

    public void write(String relativePath, String content) {
        Path file = root.resolve(relativePath);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao escrever " + file, e);
        }
    }

    public static String pkg(String subPackage) {
        return PACKAGE + (subPackage.isEmpty() ? "" : "." + subPackage);
    }
}
