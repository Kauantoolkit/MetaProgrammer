package com.example.meta_backend.service.generator.functionality;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FunctionalityServiceInterfaceGenerator {

    public void generateFunctionalityServiceInterfaces(String baseDir, List<Map<String, Object>> functionalities) throws IOException {
        for (Map<String, Object> functionality : functionalities) {
            String funcName = (String) functionality.get("name");
            String interfaceName = funcName + "Service";

            StringBuilder interfaceCode = new StringBuilder();
            interfaceCode.append("package com.metagen.backend.generated.service.functionality;\n\n")
                         .append("import com.metagen.backend.generated.dto.functionality.*;\n\n")
                         .append("public interface ").append(interfaceName).append(" {\n\n");

            String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
            String requestDto = funcName + "RequestDto";
            String responseDto = funcName + "ResponseDto";

            interfaceCode.append("    ").append(responseDto).append(" ").append(methodName).append("(").append(requestDto).append(" request);\n\n");

            interfaceCode.append("}\n");

            // Create directory if not exists
            Files.createDirectories(Paths.get(baseDir + "service/functionality/"));
            Files.writeString(Paths.get(baseDir + "service/functionality/" + interfaceName + ".java"), interfaceCode.toString());
        }
    }
}
