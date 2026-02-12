package com.example.meta_backend.service.generator.layer;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ServiceGenerator {

    public void generateService(String baseDir, String name, Map<String, Object> entity) throws IOException {
        Map<String, Object> api = (Map<String, Object>) entity.getOrDefault("api", Map.of());
        List<String> endpoints = (List<String>) api.getOrDefault("endpoints", List.of());

        StringBuilder methods = new StringBuilder();

        // Constructor
        String constructor = """
                private final %sRepository repository;

                public %sService(%sRepository repository) {
                    this.repository = repository;
                }
        """.formatted(name, name, name);

        // Conditionally add methods based on endpoints
        if (endpoints.contains("crud")) {
            methods.append("""
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
            """.formatted(name, name, name, name));
        }

        if (endpoints.contains("search")) {
            methods.append("""
                    public List<%s> search(String query) {
                        // Implement search logic here
                        return repository.findAll(); // Placeholder
                    }
            """.formatted(name));
        }

        if (endpoints.contains("filter_by_date_range")) {
            methods.append("""
                    public List<%s> filterByDateRange(String startDate, String endDate) {
                        // Implement filter logic here
                        return repository.findAll(); // Placeholder
                    }
            """.formatted(name));
        }

        if (endpoints.contains("bulk_create")) {
            methods.append("""
                    public List<%s> saveAll(List<%s> objs) {
                        return repository.saveAll(objs);
                    }
            """.formatted(name, name));
        }

        if (endpoints.contains("bulk_delete")) {
            methods.append("""
                    public void deleteAll(List<Long> ids) {
                        repository.deleteAllById(ids);
                    }
            """.formatted());
        }

        if (endpoints.contains("export_csv")) {
            methods.append("""
                    public ResponseEntity<byte[]> exportCsv() {
                        // Implement export logic here
                        return null; // Placeholder
                    }
            """.formatted());
        }

        if (endpoints.contains("import_csv")) {
            methods.append("""
                    public List<%s> importCsv(MultipartFile file) {
                        // Implement import logic here
                        return List.of(); // Placeholder
                    }
            """.formatted(name));
        }

        String code = """
            package com.metagen.backend.generated.service;

            import org.springframework.stereotype.Service;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.multipart.MultipartFile;
            import java.util.*;
            import com.metagen.backend.generated.entity.%s;
            import com.metagen.backend.generated.repository.%sRepository;

            @Service
            public class %sService {

                %s

                %s
            }
            """.formatted(name, name, name, constructor, methods.toString());

        // Cria diretório se não existir
        Files.createDirectories(Paths.get(baseDir + "service/"));
        Files.writeString(Paths.get(baseDir + "service/" + name + "Service.java"), code);
    }
}
