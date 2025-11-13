package com.example.meta_backend.service.generator.layer;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ControllerGenerator {

    public void generateController(String baseDir, String name, List<Map<String, Object>> attrs) throws IOException {
        String code = """
            package com.metagen.backend.generated.controller;

            import org.springframework.web.bind.annotation.*;
            import java.util.*;
            import com.metagen.backend.generated.entity.%s;
            import com.metagen.backend.generated.service.%sService;

            @RestController
            @RequestMapping("/api/%s")
            public class %sController {

                private final %sService service;

                public %sController(%sService service) {
                    this.service = service;
                }

                @GetMapping
                public List<%s> getAll() {
                    return service.findAll();
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
            }
            """.formatted(name, name, name.toLowerCase(), name, name, name, name, name, name, name, name);

        // Cria diretório se não existir
        Files.createDirectories(Paths.get(baseDir + "controller/"));
        Files.writeString(Paths.get(baseDir + "controller/" + name + "Controller.java"), code);
    }
}
