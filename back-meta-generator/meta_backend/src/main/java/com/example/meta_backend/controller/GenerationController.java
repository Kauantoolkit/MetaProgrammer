package com.example.meta_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta_backend.service.generator.CodeGeneratorService;

@RestController
@RequestMapping("/generate")
@CrossOrigin(origins = "http://localhost:5173")
public class GenerationController {

    private final CodeGeneratorService generatorService;

    @Value("${llm.enabled:false}")
    private boolean llmEnabled;

    public GenerationController(CodeGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

   @PostMapping
   public String generate(@RequestBody Map<String, Object> payload) {
       String appName = (String) payload.get("appName");
       @SuppressWarnings("unchecked")
       List<Map<String, Object>> entities = (List<Map<String, Object>>) payload.get("entities");
       @SuppressWarnings("unchecked")
       List<Map<String, Object>> functionalities = (List<Map<String, Object>>) payload.getOrDefault("functionalities", List.of());
       // Se o payload não especificar useAi, usa o valor de llm.enabled como padrão
       boolean useAi = payload.containsKey("useAi")
               ? Boolean.TRUE.equals(payload.get("useAi"))
               : llmEnabled;
       generatorService.generateApplication(appName, entities, functionalities, useAi);
       return "Código gerado em /" + appName + "/";
   }
}

