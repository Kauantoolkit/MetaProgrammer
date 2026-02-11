package com.example.meta_backend.service.generator.functionality;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FunctionalityControllerGenerator {

    public void generateFunctionalityControllers(String baseDir, List<Map<String, Object>> functionalities) throws IOException {
        for (Map<String, Object> functionality : functionalities) {
            String funcName = (String) functionality.get("name");
            String controllerName = funcName + "Controller";
            String serviceInterfaceName = funcName + "Service";

            StringBuilder controllerCode = new StringBuilder();
            controllerCode.append("package com.metagen.backend.generated.controller;\n\n")
                          .append("import org.springframework.web.bind.annotation.*;\n")
                          .append("import org.springframework.beans.factory.annotation.Autowired;\n\n")
                          .append("import com.metagen.backend.generated.service.functionality.").append(serviceInterfaceName).append(";\n")
                          .append("import com.metagen.backend.generated.dto.functionality.*;\n\n")
                          .append("@RestController\n")
                          .append("@RequestMapping(\"/api/").append(funcName.toLowerCase()).append("\")\n")
                          .append("public class ").append(controllerName).append(" {\n\n")
                          .append("    @Autowired\n")
                          .append("    private ").append(serviceInterfaceName).append(" ").append(serviceInterfaceName.substring(0, 1).toLowerCase() + serviceInterfaceName.substring(1)).append(";\n\n");

            String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
            String requestDto = funcName + "RequestDto";
            String responseDto = funcName + "ResponseDto";

            controllerCode.append("    @PostMapping\n")
                          .append("    public ").append(responseDto).append(" ").append(methodName).append("(@RequestBody ").append(requestDto).append(" request) {\n")
                          .append("        return ").append(serviceInterfaceName.substring(0, 1).toLowerCase() + serviceInterfaceName.substring(1)).append(".").append(methodName).append("(request);\n")
                          .append("    }\n\n");

            controllerCode.append("}\n");

            // Create directory if not exists
            Files.createDirectories(Paths.get(baseDir + "controller/"));
            Files.writeString(Paths.get(baseDir + "controller/" + controllerName + ".java"), controllerCode.toString());
        }
    }
}
