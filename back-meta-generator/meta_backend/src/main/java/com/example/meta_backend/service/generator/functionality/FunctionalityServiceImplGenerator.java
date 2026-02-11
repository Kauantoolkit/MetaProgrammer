package com.example.meta_backend.service.generator.functionality;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FunctionalityServiceImplGenerator {

    public void generateFunctionalityServiceImpls(String baseDir, List<Map<String, Object>> functionalities) throws IOException {
        for (Map<String, Object> functionality : functionalities) {
            String funcName = (String) functionality.get("name");
            String interfaceName = funcName + "Service";
            String implName = funcName + "ServiceImpl";
            String entityName = (String) functionality.getOrDefault("entity", null);

            StringBuilder implCode = new StringBuilder();
            implCode.append("package com.metagen.backend.generated.service.functionality.impl;\n\n")
                    .append("import org.springframework.stereotype.Service;\n")
                    .append("import org.springframework.beans.factory.annotation.Autowired;\n\n");

            if (entityName != null) {
                String repositoryName = entityName + "Repository";
                implCode.append("import com.metagen.backend.generated.repository.").append(repositoryName).append(";\n")
                        .append("import com.metagen.backend.generated.entity.").append(entityName).append(";\n");
            }

            implCode.append("import com.metagen.backend.generated.dto.functionality.*;\n")
                    .append("import com.metagen.backend.generated.service.functionality.").append(interfaceName).append(";\n\n")
                    .append("@Service\n")
                    .append("public class ").append(implName).append(" implements ").append(interfaceName).append(" {\n\n");

            if (entityName != null) {
                String repositoryName = entityName + "Repository";
                implCode.append("    @Autowired\n")
                        .append("    private ").append(repositoryName).append(" ").append(repositoryName.substring(0, 1).toLowerCase() + repositoryName.substring(1)).append(";\n\n");
            }

            String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
            String requestDto = funcName + "RequestDto";
            String responseDto = funcName + "ResponseDto";

            implCode.append("    @Override\n")
                    .append("    public ").append(responseDto).append(" ").append(methodName).append("(").append(requestDto).append(" request) {\n")
                    .append("        throw new UnsupportedOperationException(\"Not implemented yet\");\n")
                    .append("    }\n\n");

            implCode.append("}\n");

            // Create directory if not exists
            Files.createDirectories(Paths.get(baseDir + "service/functionality/impl/"));
            Files.writeString(Paths.get(baseDir + "service/functionality/impl/" + implName + ".java"), implCode.toString());
        }
    }
}
