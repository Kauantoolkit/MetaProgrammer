package com.example.meta_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta_backend.service.generator.CodeGeneratorService;

@RestController
@RequestMapping("/generate")
@CrossOrigin(origins = "${cors.allowed-origins:http://localhost:5173}")
public class GenerationController {

    private static final int MAX_ENTITIES = 50;
    private static final int MAX_FUNCTIONALITIES = 30;
    private static final int MAX_APP_NAME_LENGTH = 64;

    private final CodeGeneratorService generatorService;

    @Value("${llm.enabled:false}")
    private boolean llmEnabled;

    public GenerationController(CodeGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @PostMapping
    public ResponseEntity<String> generate(@RequestBody Map<String, Object> payload) {
        String appName = (String) payload.get("appName");

        if (appName == null || appName.isBlank()) {
            return ResponseEntity.badRequest().body("appName é obrigatório e não pode ser vazio.");
        }
        if (appName.length() > MAX_APP_NAME_LENGTH) {
            return ResponseEntity.badRequest().body("appName não pode ter mais de " + MAX_APP_NAME_LENGTH + " caracteres.");
        }
        if (!appName.matches("[a-zA-Z0-9\\-_]+")) {
            return ResponseEntity.badRequest().body("appName deve conter apenas letras, números, hífens e underscores.");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entities = (List<Map<String, Object>>) payload.get("entities");
        if (entities == null || entities.isEmpty()) {
            return ResponseEntity.badRequest().body("O payload deve conter pelo menos uma entidade.");
        }
        if (entities.size() > MAX_ENTITIES) {
            return ResponseEntity.badRequest().body("O número máximo de entidades permitido é " + MAX_ENTITIES + ".");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> functionalities = (List<Map<String, Object>>) payload.getOrDefault("functionalities", List.of());
        if (functionalities.size() > MAX_FUNCTIONALITIES) {
            return ResponseEntity.badRequest().body("O número máximo de funcionalidades permitido é " + MAX_FUNCTIONALITIES + ".");
        }

        boolean useAi = payload.containsKey("useAi")
                ? Boolean.TRUE.equals(payload.get("useAi"))
                : llmEnabled;

        generatorService.generateApplication(appName, entities, functionalities, useAi);
        return ResponseEntity.ok("Código gerado em /" + appName + "/");
    }
}

