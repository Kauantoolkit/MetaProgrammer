package com.example.meta_backend.controller;

import java.util.List;
import java.util.Map;

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

    public GenerationController(CodeGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

   @PostMapping
public String generate(@RequestBody List<Map<String, Object>> entities) {
    generatorService.generateEntities(entities);
    return "Código gerado em /generated/";
}}

