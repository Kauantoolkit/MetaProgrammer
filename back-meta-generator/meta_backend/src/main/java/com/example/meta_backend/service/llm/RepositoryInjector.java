package com.example.meta_backend.service.llm;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RepositoryInjector {

    private static final Logger log = LoggerFactory.getLogger(RepositoryInjector.class);

    // Métodos padrão do JpaRepository — não precisam ser declarados
    private static final List<String> JPA_DEFAULTS = List.of(
            "findAll", "findById", "save", "saveAll", "delete", "deleteById",
            "deleteAll", "deleteAllById", "count", "existsById", "flush",
            "saveAndFlush", "getReferenceById"
    );

    public void injectMethods(String repoFilePath, List<String> methodSignatures) throws IOException {
        Path path = Paths.get(repoFilePath);
        if (!Files.exists(path)) {
            log.warn("Repositorio nao encontrado para injecao: {}", repoFilePath);
            return;
        }

        String content = Files.readString(path);

        List<String> toAdd = methodSignatures.stream()
                .map(String::trim)
                .filter(sig -> !sig.isBlank() && !sig.startsWith("//") && !sig.contains("`"))
                .filter(sig -> looksLikeValidSignature(sig))
                .filter(sig -> !isAlreadyPresent(content, sig))
                .filter(sig -> !isJpaDefault(sig))
                .map(sig -> sig.endsWith(";") ? sig : sig + ";")
                .collect(Collectors.toList());

        if (toAdd.isEmpty()) return;

        String methods = toAdd.stream()
                .map(sig -> "    " + sig)
                .collect(Collectors.joining("\n"));

        int lastBrace = content.lastIndexOf("}");
        if (lastBrace == -1) return;

        String header = content.contains("// --- métodos gerados automaticamente ---")
                ? "\n"
                : "\n    // --- métodos gerados automaticamente ---\n";

        String updated = content.substring(0, lastBrace)
                + header
                + methods + "\n"
                + "}";

        Files.writeString(path, updated);
        log.info("Injetados {} metodos em {}", toAdd.size(), path.getFileName());
    }

    // A valid signature must have at least a return type before the method name: "Type methodName("
    private boolean looksLikeValidSignature(String sig) {
        int paren = sig.indexOf('(');
        if (paren == -1) return false;
        String[] parts = sig.substring(0, paren).trim().split("\\s+");
        return parts.length >= 2; // at least: ReturnType methodName
    }

    private boolean isAlreadyPresent(String content, String signature) {
        String methodName = extractMethodName(signature);
        return methodName != null && content.contains(methodName + "(");
    }

    private boolean isJpaDefault(String signature) {
        String methodName = extractMethodName(signature);
        return methodName != null && JPA_DEFAULTS.stream()
                .anyMatch(def -> methodName.equals(def));
    }

    private String extractMethodName(String signature) {
        int paren = signature.indexOf('(');
        if (paren == -1) return null;
        String[] parts = signature.substring(0, paren).trim().split("\\s+");
        return parts[parts.length - 1];
    }
}
