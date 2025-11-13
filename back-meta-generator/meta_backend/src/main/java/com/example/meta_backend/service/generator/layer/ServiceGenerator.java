package com.example.meta_backend.service.generator.layer;

import java.io.IOException;
import java.nio.file.*;

public class ServiceGenerator {

    public void generateService(String baseDir, String name) throws IOException {
        String code = """
            package com.metagen.backend.generated.service;

            import org.springframework.stereotype.Service;
            import java.util.*;
            import com.metagen.backend.generated.entity.%s;
            import com.metagen.backend.generated.repository.%sRepository;

            @Service
            public class %sService {

                private final %sRepository repository;

                public %sService(%sRepository repository) {
                    this.repository = repository;
                }

                public List<%s> findAll() {
                    return repository.findAll();
                }

                public Optional<%s> findById(Long id) {
                    return repository.findById(id);
                }

                public %s save(%s obj) {
                    return repository.save(obj);
                }

                public void delete(Long id) {
                    repository.deleteById(id);
                }
            }
            """.formatted(name, name, name, name, name, name, name, name, name, name);

        // Cria diretório se não existir
        Files.createDirectories(Paths.get(baseDir + "service/"));
        Files.writeString(Paths.get(baseDir + "service/" + name + "Service.java"), code);
    }
}
