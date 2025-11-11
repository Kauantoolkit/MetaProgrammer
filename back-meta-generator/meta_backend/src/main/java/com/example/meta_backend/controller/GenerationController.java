package com.example.meta_backend.controller;

import com.example.meta_backend.service.CodeGeneratorService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/generate")
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

