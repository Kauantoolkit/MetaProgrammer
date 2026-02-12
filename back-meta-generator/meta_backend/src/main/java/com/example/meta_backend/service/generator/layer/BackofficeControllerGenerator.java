package com.example.meta_backend.service.generator.layer;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class BackofficeControllerGenerator {

    public void generateBackofficeController(List<Map<String, Object>> entities, String appName) throws IOException {
        String baseDir = appName + "/src/main/java/com/metagen/backend/generated/controller/";
        Files.createDirectories(Paths.get(baseDir));

        StringBuilder sb = new StringBuilder();
        sb.append("package com.metagen.backend.generated.controller;\n\n")
          .append("import org.springframework.stereotype.Controller;\n")
          .append("import org.springframework.ui.Model;\n")
          .append("import org.springframework.web.bind.annotation.GetMapping;\n\n")
          .append("@Controller\n")
          .append("public class BackofficeController {\n\n")

          // LOGIN
          .append("    @GetMapping(\"/login\")\n")
          .append("    public String login() {\n")
          .append("        return \"backoffice/login\";\n")
          .append("    }\n\n")

          // HOME
          .append("    @GetMapping(\"/backoffice\")\n")
          .append("    public String backofficeHome(Model model) {\n")
          .append("        return \"backoffice/index\";\n")
          .append("    }\n\n");

        for (Map<String, Object> entity : entities) {
            Map<String, Object> api = (Map<String, Object>) entity.getOrDefault("api", Map.of());
            List<String> endpoints = (List<String>) api.getOrDefault("endpoints", List.of());

            if (!endpoints.isEmpty()) {
                String name = (String) entity.get("name");
                String pathName = name.toLowerCase();

                sb.append(String.format(
                    "    @GetMapping(\"/backoffice/%s\")\n" +
                    "    public String list%s(Model model) {\n" +
                    "        return \"backoffice/%s/list\";\n" +
                    "    }\n\n",
                    pathName, name, pathName
                ));
            }
        }

        sb.append("}\n");

        Path path = Paths.get(baseDir + "BackofficeController.java");
        Files.writeString(path, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
