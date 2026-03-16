package com.example.meta_backend.service.generator.functionality;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.meta_backend.service.llm.ContextBuilder;
import com.example.meta_backend.service.llm.LlmClient;
import com.example.meta_backend.service.llm.RepositoryInjector;

@Component
public class FunctionalityServiceImplGenerator {

    private static final Logger log = LoggerFactory.getLogger(FunctionalityServiceImplGenerator.class);
    private static final String STUB = "        throw new UnsupportedOperationException(\"Not implemented yet\");";

    private final LlmClient llmClient;
    private final ContextBuilder contextBuilder;
    private final RepositoryInjector repositoryInjector;

    public FunctionalityServiceImplGenerator(
            LlmClient llmClient,
            ContextBuilder contextBuilder,
            RepositoryInjector repositoryInjector) {
        this.llmClient = llmClient;
        this.contextBuilder = contextBuilder;
        this.repositoryInjector = repositoryInjector;
    }

    public void generateFunctionalityServiceImpls(
            String baseDir,
            List<Map<String, Object>> functionalities,
            List<Map<String, Object>> entities,
            boolean useAi) throws IOException {

        for (Map<String, Object> functionality : functionalities) {
            String funcName = (String) functionality.get("name");

            if (!useAi || !llmClient.isEnabled()) {
                writeImplFile(baseDir, funcName, STUB, entities);
                continue;
            }

            try {
                String systemPrompt = contextBuilder.buildSystemPrompt();
                String userPrompt = contextBuilder.buildUserPrompt(functionality, entities);
                String response = llmClient.generate(systemPrompt, userPrompt);

                ParsedResponse parsed = parseStructuredResponse(response, funcName);

                writeImplFile(baseDir, funcName, indentLines(parsed.implBody, 8), entities);

                for (Map.Entry<String, List<String>> entry : parsed.repoMethods.entrySet()) {
                    String repoPath = baseDir + "repository/" + entry.getKey() + ".java";
                    repositoryInjector.injectMethods(repoPath, entry.getValue());
                }

                log.info("IA gerou implementacao para '{}' ({} repos atualizados)",
                        funcName, parsed.repoMethods.size());

            } catch (Exception e) {
                log.warn("Falha via IA para '{}': {} — {}. Usando stub.",
                        funcName, e.getClass().getSimpleName(), e.getMessage());
                writeImplFile(baseDir, funcName, STUB, entities);
            }
        }
    }

    // --- Parsing da resposta estruturada ---

    private record ParsedResponse(String implBody, Map<String, List<String>> repoMethods) {}

    private ParsedResponse parseStructuredResponse(String response, String funcName) {
        String implBody = extractBlock(response, "=== IMPL ===", "=== END IMPL ===");

        if (implBody == null || implBody.isBlank()) {
            // Fallback: modelo não seguiu o formato — tenta extração antiga
            implBody = extractLegacy(response, funcName);
        }

        if (implBody != null && !implBody.isBlank()) {
            // Strip ALL markdown fences anywhere in the block
            implBody = implBody.replaceAll("```[a-zA-Z]*\\r?\\n?", "").trim();
            // If model included full method signature inside block, extract just the body
            if (looksLikeMethodDeclaration(implBody, funcName)) {
                String extracted = extractMethodBodyFromClass(implBody, funcName);
                if (extracted != null && !extracted.isBlank()) {
                    implBody = extracted;
                }
            }
        }

        if (implBody == null || implBody.isBlank()) {
            throw new IllegalArgumentException("Nao foi possivel extrair o corpo do metodo da resposta");
        }

        Map<String, List<String>> repoMethods = new LinkedHashMap<>();
        Pattern repoPattern = Pattern.compile(
                "=== REPO (\\w+) ===\\s*([\\s\\S]*?)=== END REPO ===");
        Matcher m = repoPattern.matcher(response);
        while (m.find()) {
            String repoName = m.group(1).trim();
            // Normalize: split on newlines, then re-split lines that contain multiple signatures (model may put them on one line separated by ";")
            String repoBlock = m.group(2).replaceAll(";\\s*(?=[A-Za-z])", ";\n");
            List<String> methods = Arrays.stream(repoBlock.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank() && !s.startsWith("//") && !s.contains("`"))
                    .collect(Collectors.toList());
            if (!methods.isEmpty()) {
                repoMethods.put(repoName, methods);
            }
        }

        return new ParsedResponse(implBody, repoMethods);
    }

    private String extractBlock(String text, String start, String end) {
        int s = text.indexOf(start);
        if (s == -1) return null;
        s += start.length();
        int e = text.indexOf(end, s);
        return (e == -1 ? text.substring(s) : text.substring(s, e)).trim();
    }

    // Extração antiga para compatibilidade se o modelo não seguir o formato
    private String extractLegacy(String response, String funcName) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("(?s)^```[a-zA-Z]*\\s*", "")
                             .replaceAll("(?s)```\\s*$", "").trim();
        }
        if (cleaned.contains("@Service") || cleaned.contains("public class ")) {
            cleaned = extractMethodBodyFromClass(cleaned, funcName);
        }
        return cleaned;
    }

    private boolean looksLikeMethodDeclaration(String code, String funcName) {
        String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
        // Checks for a pattern like: <modifier> <ReturnType> methodName(
        return code.matches("(?s).*\\b(public|protected|private)\\s+\\w[\\w<>,\\s]*\\s+" + Pattern.quote(methodName) + "\\s*\\(.*");
    }

    private String extractMethodBodyFromClass(String classCode, String funcName) {
        String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
        int methodStart = classCode.indexOf(methodName + "(");
        if (methodStart == -1) return classCode;
        int braceStart = classCode.indexOf("{", methodStart);
        if (braceStart == -1) return classCode;
        int depth = 0, braceEnd = -1;
        for (int i = braceStart; i < classCode.length(); i++) {
            char c = classCode.charAt(i);
            if (c == '{') depth++;
            else if (c == '}' && --depth == 0) { braceEnd = i; break; }
        }
        return braceEnd == -1 ? classCode : classCode.substring(braceStart + 1, braceEnd).trim();
    }

    private String indentLines(String body, int spaces) {
        String indent = " ".repeat(spaces);
        return Arrays.stream(body.split("\n"))
                .map(line -> line.isBlank() ? "" : indent + line)
                .collect(Collectors.joining("\n"));
    }

    // --- Escrita do arquivo impl ---

    private void writeImplFile(
            String baseDir,
            String funcName,
            String methodBody,
            List<Map<String, Object>> entities) throws IOException {

        String interfaceName = funcName + "Service";
        String implName = funcName + "ServiceImpl";
        String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
        String requestDto = funcName + "RequestDto";
        String responseDto = funcName + "ResponseDto";

        StringBuilder implCode = new StringBuilder();
        implCode.append("package com.metagen.backend.generated.service.functionality.impl;\n\n")
                .append("import org.springframework.stereotype.Service;\n")
                .append("import org.springframework.beans.factory.annotation.Autowired;\n")
                .append("import java.util.*;\n")
                .append("import java.time.*;\n\n")
                .append("import com.metagen.backend.generated.dto.functionality.*;\n")
                .append("import com.metagen.backend.generated.repository.*;\n")
                .append("import com.metagen.backend.generated.entity.*;\n")
                .append("import com.metagen.backend.generated.service.functionality.")
                .append(interfaceName).append(";\n\n")
                .append("@Service\n")
                .append("public class ").append(implName).append(" implements ").append(interfaceName).append(" {\n\n");

        for (Map<String, Object> entity : entities) {
            String entityName = (String) entity.get("name");
            if ("Users".equalsIgnoreCase(entityName)) continue;
            String varName = entityName.substring(0, 1).toLowerCase() + entityName.substring(1) + "Repository";
            implCode.append("    @Autowired\n")
                    .append("    private ").append(entityName).append("Repository ").append(varName).append(";\n\n");
        }

        implCode.append("    @Override\n")
                .append("    public ").append(responseDto).append(" ").append(methodName)
                .append("(").append(requestDto).append(" request) {\n")
                .append(methodBody).append("\n")
                .append("    }\n\n")
                .append("}\n");

        Files.createDirectories(Paths.get(baseDir + "service/functionality/impl/"));
        Files.writeString(Paths.get(baseDir + "service/functionality/impl/" + implName + ".java"), implCode.toString());
    }
}
