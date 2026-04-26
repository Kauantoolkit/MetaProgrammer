package com.example.meta_backend.service.generator.layer;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class ControllerGenerator {

    public void generateController(String baseDir, String name, List<Map<String, Object>> attrs, Map<String, Object> entity) throws IOException {
        Map<String, Object> api = (Map<String, Object>) entity.getOrDefault("api", Map.of());
        List<String> endpoints = (List<String>) api.getOrDefault("endpoints", List.of());

        // Se não há endpoints configurados, deleta o controller se existir e não gera
        if (endpoints.isEmpty()) {
            Path controllerPath = Paths.get(baseDir + "controller/" + name + "Controller.java");
            try {
                Files.deleteIfExists(controllerPath);
            } catch (IOException e) {
                // Ignora erro ao deletar
            }
            return;
        }

        StringBuilder methods = new StringBuilder();

        // Always include constructor
        String constructor = """
                private final %sService service;

                public %sController(%sService service) {
                    this.service = service;
                }
        """.formatted(name, name, name);

        // Conditionally add CRUD methods
        if (endpoints.contains("crud")) {
            methods.append("""
                    @GetMapping
                    public org.springframework.data.domain.Page<%s> getAll(
                            @org.springframework.data.web.PageableDefault(size = 20, sort = "id") Pageable pageable) {
                        return service.findAll(pageable);
                    }

                    @GetMapping("/{id}")
                    public Optional<%s> getById(@PathVariable Long id) {
                        return service.findById(id);
                    }

                    @PostMapping
                    public %s create(@RequestBody %s obj) {
                        return service.save(obj);
                    }

                    @DeleteMapping("/{id}")
                    public void delete(@PathVariable Long id) {
                        service.delete(id);
                    }
            """.formatted(name, name, name, name));
        }

        // Add other endpoints based on configuration
        if (endpoints.contains("search")) {
            methods.append("""
                    @GetMapping("/search")
                    public List<%s> search(@RequestParam String query) {
                        return service.search(query);
                    }
            """.formatted(name));
        }

        if (endpoints.contains("filter_by_date_range")) {
            methods.append("""
                    @GetMapping("/filter")
                    public List<%s> filterByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
                        return service.filterByDateRange(startDate, endDate);
                    }
            """.formatted(name));
        }

        if (endpoints.contains("bulk_create")) {
            methods.append("""
                    @PostMapping("/bulk")
                    public List<%s> bulkCreate(@RequestBody List<%s> objs) {
                        return service.saveAll(objs);
                    }
            """.formatted(name, name));
        }

        if (endpoints.contains("bulk_delete")) {
            methods.append("""
                    @DeleteMapping("/bulk")
                    public void bulkDelete(@RequestBody List<Long> ids) {
                        service.deleteAll(ids);
                    }
            """.formatted());
        }

        if (endpoints.contains("export_csv")) {
            methods.append("""
                    @GetMapping("/export")
                    public ResponseEntity<byte[]> exportCsv() {
                        return service.exportCsv();
                    }
            """.formatted());
        }

        if (endpoints.contains("import_csv")) {
            methods.append("""
                    @PostMapping("/import")
                    public List<%s> importCsv(@RequestParam("file") MultipartFile file) {
                        return service.importCsv(file);
                    }
            """.formatted(name));
        }

        String code = """
            package com.metagen.backend.generated.controller;

            import org.springframework.web.bind.annotation.*;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.multipart.MultipartFile;
            import org.springframework.data.domain.Pageable;
            import java.util.*;
            import com.metagen.backend.generated.entity.%s;
            import com.metagen.backend.generated.service.%sService;

            @RestController
            @RequestMapping("/api/%s")
            public class %sController {

                %s

                %s
            }
            """.formatted(name, name, name.toLowerCase(), name, constructor, methods.toString());

        // Cria diretório se não existir
        Files.createDirectories(Paths.get(baseDir + "controller/"));
        Files.writeString(Paths.get(baseDir + "controller/" + name + "Controller.java"), code);
    }
}
