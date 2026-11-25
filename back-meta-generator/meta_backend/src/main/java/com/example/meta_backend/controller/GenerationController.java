package com.example.meta_backend.controller;

import com.example.meta_backend.service.generator.CodeGeneratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/generate")
@CrossOrigin(origins = "http://localhost:5173")
public class GenerationController {

    private final CodeGeneratorService generatorService;

    public GenerationController(CodeGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @PostMapping
    public ResponseEntity<?> generate(@RequestBody Map<String, Object> payload) {
        try {
            return ResponseEntity.ok(generatorService.generateApplication(payload));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
