package com.example.meta_backend.service.generator.layer;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

public class BackofficeControllerGenerator {

    private static final String BASE_DIR = "generated_app/src/main/java/com/metagen/backend/generated/controller/";

    public void generateBackofficeController(List<Map<String, Object>> entities) throws IOException {
        Files.createDirectories(Paths.get(BASE_DIR));

        StringBuilder sb = new StringBuilder();
        sb.append("package com.metagen.backend.generated.controller;\n\n")
          .append("import org.springframework.stereotype.Controller;\n")
          .append("import org.springframework.ui.Model;\n")
          .append("import org.springframework.web.bind.annotation.GetMapping;\n\n")
          .append("@Controller\n")
          .append("public class BackofficeController {\n\n")
          .append("    @GetMapping(\"/backoffice\")\n")
          .append("    public String backofficeHome(Model model) {\n")
          .append("        return \"backoffice/index\";\n")
          .append("    }\n\n");

        for (Map<String, Object> entity : entities) {
            String name = (String) entity.get("name");
            sb.append(String.format(
                "    @GetMapping(\"/%s/list\")\n" +
                "    public String list%s(Model model) {\n" +
                "        return \"%s/list\";\n" +
                "    }\n\n",
                name.toLowerCase(), name, name.toLowerCase()
            ));
        }

        sb.append("}\n");

        Path path = Paths.get(BASE_DIR + "BackofficeController.java");
        Files.writeString(path, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
