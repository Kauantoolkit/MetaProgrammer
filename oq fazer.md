🧠 CONTEXTO DO SISTEMA – META BACKEND GENERATOR

Este projeto é um metagerador de aplicações Spring Boot.
Ele já gera automaticamente a estrutura base de uma aplicação e código a partir de um modelo de entidades.

Atualmente o sistema é fortemente orientado a entidade (CRUD).
O objetivo da nova fase é torná-lo orientado a funcionalidades (use cases) sem remover o suporte a CRUD.

📦 ESTADO ATUAL DO GERADOR

O sistema já gera automaticamente:

Estrutura base

Projeto Maven

Classe principal

application.properties

.gitignore

Camada de domínio baseada em entidades

Para cada entidade:

Classe Entity

Repository

DTOs:

CreateDto

UpdateDto

ResponseDto

Mapper

Service com base em endpoints configurados

Controller REST (somente se houver endpoints)

Infraestrutura já existente

Segurança (login, User, configuração Spring Security)

Global Exception Handler

Backoffice (controller + templates)

Templates Thymeleaf básicos

🎯 OBJETIVO DA NOVA ARQUITETURA

Adicionar uma segunda dimensão de geração, além de entidades:

Modelo Atual	Novo Modelo
CRUD por entidade	Funcionalidades por caso de uso
Service genérico por entidade	Interfaces de serviço por funcionalidade
DTOs genéricos de entidade	DTOs específicos de entrada e saída
Controller por entidade	Controllers orientados a ações de negócio
🧩 NOVO CONCEITO: FUNCTIONALITIES

Cada entidade poderá declarar funcionalidades de negócio, por exemplo:

"functionalities": [
  {
    "name": "ApproveOrder",
    "inputDto": "ApproveOrderRequest",
    "outputDto": "OrderApprovalResponse"
  }
]


Essas funcionalidades NÃO substituem CRUD, elas coexistem.

🏗️ O QUE O AGENTE PRECISA IMPLEMENTAR

Abaixo está o checklist completo dividido por camadas.

1️⃣ MODELO DE CONFIGURAÇÃO (BASE DO GERADOR)
✅ Tasks

 Permitir que cada entidade tenha um bloco "functionalities"

 Cada funcionalidade deve conter:

 name

 input (campos do DTO de entrada)

 output (campos do DTO de saída)

 Garantir que o parser do gerador reconheça esse novo bloco

 Manter compatibilidade total com entidades sem funcionalidades

2️⃣ GERAÇÃO DE DTOs DE FUNCIONALIDADE

Hoje só existem DTOs de CRUD. Precisamos gerar DTOs específicos por funcionalidade.

Para cada funcionalidade:

 Gerar DTO de entrada

Nome: <FunctionalityName>RequestDto

 Gerar DTO de saída

Nome: <FunctionalityName>ResponseDto

 Colocar em:
generated/dto/functionality/

Regras:

 NÃO reutilizar CreateDto ou UpdateDto

 Campos vêm da definição da funcionalidade, não da entidade

3️⃣ GERAÇÃO DE INTERFACES DE SERVIÇO (NOVA CAMADA)

Hoje o gerador cria apenas classes de service concretas.
Agora ele deve gerar interfaces de serviço por entidade.

Para cada entidade com funcionalidades:

 Criar interface:
<EntityName>FunctionalityService

 Um método por funcionalidade:

OrderApprovalResponseDto approveOrder(ApproveOrderRequestDto request);


 Salvar em:
generated/service/functionality/

4️⃣ IMPLEMENTAÇÃO PADRÃO DOS SERVICES

Além da interface, gerar a implementação:

Classe:

<EntityName>FunctionalityServiceImpl

Regras:

 Anotada com @Service

 Implementa a interface gerada

 Injeta:

Repository da entidade

Mapper (se necessário)

 Métodos com corpo inicial padrão:

throw new UnsupportedOperationException("Not implemented yet");


Salvar em:
generated/service/functionality/impl/

5️⃣ CONTROLLERS DE FUNCIONALIDADE

Hoje o controller é só CRUD.
Agora cada funcionalidade deve virar um endpoint.

Para cada funcionalidade:

 Criar endpoint POST

 URL padrão:

/api/<entity>/<functionality>

Exemplo:
@PostMapping("/approve-order")
public OrderApprovalResponseDto approveOrder(
        @RequestBody ApproveOrderRequestDto request) {
    return functionalityService.approveOrder(request);
}

Tasks

 Controller deve injetar a interface, não a implementação

 Controller CRUD e Controller de Funcionalidades podem ser:

 Mesma classe (modo simples) OU

 Classes separadas (modo avançado — preferível)

6️⃣ AJUSTE NO CODEGENERATOR PRINCIPAL

O CodeGeneratorService precisa orquestrar a nova geração.

Atualizar fluxo:

Para cada entidade:

 Gerar Entity / Repository / Mapper (como já faz)

 Gerar DTOs CRUD (como já faz)

 Gerar Service CRUD (como já faz)

 NOVO: Se houver funcionalidades:

 Gerar DTOs de funcionalidade

 Gerar interface de funcionalidade

 Gerar implementação

 Gerar endpoints no controller

7️⃣ BACKOFFICE ORIENTADO A FUNCIONALIDADES (FUTURO)

Hoje o backoffice só lista entidades.

Preparação futura

 Permitir que funcionalidades tenham flag:

"exposeInBackoffice": true


 Gerar telas para executar funcionalidades

(NÃO implementar agora — apenas deixar arquitetura pronta)

8️⃣ BOAS PRÁTICAS QUE O AGENTE DEVE SEGUIR

 Nunca misturar DTO de CRUD com DTO de funcionalidade

 Nunca colocar regra de negócio dentro do controller gerado

 Services de funcionalidade devem depender de repositories

 Interfaces devem ser usadas para injeção

 Código gerado deve compilar mesmo sem implementação real

🧭 RESULTADO FINAL ESPERADO

Após essas tasks, o gerador suportará:

Tipo	Gerado?
CRUD por entidade	✅
Funcionalidades de negócio	✅
DTOs específicos por caso de uso	✅
Interfaces de service	✅
Implementações separadas	✅
Controllers orientados a ação	✅


update 1.2

diff --git a/TODO.md b/TODO.md
index d349795..7403aaa 100644
--- a/TODO.md
+++ b/TODO.md
@@ -7,11 +7,12 @@
 - [ ] Manter compatibilidade total com entidades sem funcionalidades
 
 ## 2. Gera├º├úo de DTOs de Funcionalidade
-- [ ] Criar novo gerador: FunctionalityDtoGenerator
-- [ ] Para cada funcionalidade: Gerar DTO de entrada (<FunctionalityName>RequestDto)
-- [ ] Para cada funcionalidade: Gerar DTO de sa├¡da (<FunctionalityName>ResponseDto)
-- [ ] Colocar em: generated/dto/functionality/
-- [ ] Regras: N├âO reutilizar CreateDto ou UpdateDto, campos v├¬m da defini├º├úo da funcionalidade
+- [x] Criar novo gerador: FunctionalityDtoGenerator
+- [x] Para cada funcionalidade: Gerar DTO de entrada (<FunctionalityName>RequestDto)
+- [x] Para cada funcionalidade: Gerar DTO de sa├¡da (<FunctionalityName>ResponseDto)
+- [x] Colocar em: generated/dto/functionality/
+- [x] Regras: N├âO reutilizar CreateDto ou UpdateDto, campos v├¬m da defini├º├úo da funcionalidade
+- [x] Corrigido bug no m├®todo generateFunctionalityDTOs (par├ómetro entityName removido)
 
 ## 3. Gera├º├úo de Interfaces de Servi├ºo (NOVA CAMADA)
 - [ ] Criar novo gerador: FunctionalityServiceInterfaceGenerator
diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
index 09eff91..e1df813 100644
--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
+++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
@@ -23,14 +23,14 @@ public class GenerationController {
     }
 
    @PostMapping
-public String generate(@RequestBody Map<String, Object> payload) {
-    String appName = (String) payload.get("appName");
-    @SuppressWarnings("unchecked")
-    List<Map<String, Object>> entities = (List<Map<String, Object>>) payload.get("entities");
-    @SuppressWarnings("unchecked")
-    List<Map<String, Object>> functionalities = (List<Map<String, Object>>) payload.getOrDefault("functionalities", List.of());
-    generatorService.generateApplication(appName, entities, functionalities);
-    return "C├│digo gerado em /" + appName + "/";
-}
+   public String generate(@RequestBody Map<String, Object> payload) {
+       String appName = (String) payload.get("appName");
+       @SuppressWarnings("unchecked")
+       List<Map<String, Object>> entities = (List<Map<String, Object>>) payload.get("entities");
+       @SuppressWarnings("unchecked")
+       List<Map<String, Object>> functionalities = (List<Map<String, Object>>) payload.getOrDefault("functionalities", List.of());
+       generatorService.generateApplication(appName, entities, functionalities);
+       return "C├│digo gerado em /" + appName + "/";
+   }
 }
 
diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
index 9ebd63c..834e418 100644
--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
+++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
@@ -25,10 +25,11 @@ public class FunctionalityControllerGenerator {
                           .append("    private ").append(serviceInterfaceName).append(" ").append(serviceInterfaceName.substring(0, 1).toLowerCase() + serviceInterfaceName.substring(1)).append(";\n\n");
 
             String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
+            String endpoint = "/" + methodName.replaceAll("([A-Z])", "-$1").toLowerCase();
             String requestDto = funcName + "RequestDto";
             String responseDto = funcName + "ResponseDto";
 
-            controllerCode.append("    @PostMapping\n")
+            controllerCode.append("    @PostMapping(\"").append(endpoint).append("\")\n")
                           .append("    public ").append(responseDto).append(" ").append(methodName).append("(@RequestBody ").append(requestDto).append(" request) {\n")
                           .append("        return ").append(serviceInterfaceName.substring(0, 1).toLowerCase() + serviceInterfaceName.substring(1)).append(".").append(methodName).append("(request);\n")
                           .append("    }\n\n");
diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
index a903df7..177d852 100644
--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
+++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
@@ -11,30 +11,16 @@ public class FunctionalityServiceImplGenerator {
             String funcName = (String) functionality.get("name");
             String interfaceName = funcName + "Service";
             String implName = funcName + "ServiceImpl";
-            String entityName = (String) functionality.getOrDefault("entity", null);
 
             StringBuilder implCode = new StringBuilder();
             implCode.append("package com.metagen.backend.generated.service.functionality.impl;\n\n")
                     .append("import org.springframework.stereotype.Service;\n")
-                    .append("import org.springframework.beans.factory.annotation.Autowired;\n\n");
-
-            if (entityName != null) {
-                String repositoryName = entityName + "Repository";
-                implCode.append("import com.metagen.backend.generated.repository.").append(repositoryName).append(";\n")
-                        .append("import com.metagen.backend.generated.entity.").append(entityName).append(";\n");
-            }
-
-            implCode.append("import com.metagen.backend.generated.dto.functionality.*;\n")
+                    .append("import org.springframework.beans.factory.annotation.Autowired;\n\n")
+                    .append("import com.metagen.backend.generated.dto.functionality.*;\n")
                     .append("import com.metagen.backend.generated.service.functionality.").append(interfaceName).append(";\n\n")
                     .append("@Service\n")
                     .append("public class ").append(implName).append(" implements ").append(interfaceName).append(" {\n\n");
 
-            if (entityName != null) {
-                String repositoryName = entityName + "Repository";
-                implCode.append("    @Autowired\n")
-                        .append("    private ").append(repositoryName).append(" ").append(repositoryName.substring(0, 1).toLowerCase() + repositoryName.substring(1)).append(";\n\n");
-            }
-
             String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
             String requestDto = funcName + "RequestDto";
             String responseDto = funcName + "ResponseDto";
diff --git a/diff.txt b/diff.txt
deleted file mode 100644
index 86df831..0000000
Binary files a/diff.txt and /dev/null differ
diff --git a/front-meta-generator/metafront/package-lock.json b/front-meta-generator/metafront/package-lock.json
index 79c73ab..095795f 100644
--- a/front-meta-generator/metafront/package-lock.json
+++ b/front-meta-generator/metafront/package-lock.json
@@ -8,9 +8,13 @@
       "name": "metafront",
       "version": "1.0.0",
       "dependencies": {
+        "@radix-ui/react-label": "^2.1.8",
+        "class-variance-authority": "^0.7.1",
+        "clsx": "^2.1.1",
         "lucide-react": "^0.553.0",
         "react": "^18.3.1",
-        "react-dom": "^18.3.1"
+        "react-dom": "^18.3.1",
+        "tailwind-merge": "^3.4.0"
       },
       "devDependencies": {
         "@vitejs/plugin-react": "^4.3.0",
@@ -876,6 +880,85 @@
         "node": ">=14"
       }
     },
+    "node_modules/@radix-ui/react-compose-refs": {
+      "version": "1.1.2",
+      "resolved": "https://registry.npmjs.org/@radix-ui/react-compose-refs/-/react-compose-refs-1.1.2.tgz",
+      "integrity": "sha512-z4eqJvfiNnFMHIIvXP3CY57y2WJs5g2v3X0zm9mEJkrkNv4rDxu+sg9Jh8EkXyeqBkB7SOcboo9dMVqhyrACIg==",
+      "license": "MIT",
+      "peerDependencies": {
+        "@types/react": "*",
+        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
+      },
+      "peerDependenciesMeta": {
+        "@types/react": {
+          "optional": true
+        }
+      }
+    },
+    "node_modules/@radix-ui/react-label": {
+      "version": "2.1.8",
+      "resolved": "https://registry.npmjs.org/@radix-ui/react-label/-/react-label-2.1.8.tgz",
+      "integrity": "sha512-FmXs37I6hSBVDlO4y764TNz1rLgKwjJMQ0EGte6F3Cb3f4bIuHB/iLa/8I9VKkmOy+gNHq8rql3j686ACVV21A==",
+      "license": "MIT",
+      "dependencies": {
+        "@radix-ui/react-primitive": "2.1.4"
+      },
+      "peerDependencies": {
+        "@types/react": "*",
+        "@types/react-dom": "*",
+        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc",
+        "react-dom": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
+      },
+      "peerDependenciesMeta": {
+        "@types/react": {
+          "optional": true
+        },
+        "@types/react-dom": {
+          "optional": true
+        }
+      }
+    },
+    "node_modules/@radix-ui/react-primitive": {
+      "version": "2.1.4",
+      "resolved": "https://registry.npmjs.org/@radix-ui/react-primitive/-/react-primitive-2.1.4.tgz",
+      "integrity": "sha512-9hQc4+GNVtJAIEPEqlYqW5RiYdrr8ea5XQ0ZOnD6fgru+83kqT15mq2OCcbe8KnjRZl5vF3ks69AKz3kh1jrhg==",
+      "license": "MIT",
+      "dependencies": {
+        "@radix-ui/react-slot": "1.2.4"
+      },
+      "peerDependencies": {
+        "@types/react": "*",
+        "@types/react-dom": "*",
+        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc",
+        "react-dom": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
+      },
+      "peerDependenciesMeta": {
+        "@types/react": {
+          "optional": true
+        },
+        "@types/react-dom": {
+          "optional": true
+        }
+      }
+    },
+    "node_modules/@radix-ui/react-slot": {
+      "version": "1.2.4",
+      "resolved": "https://registry.npmjs.org/@radix-ui/react-slot/-/react-slot-1.2.4.tgz",
+      "integrity": "sha512-Jl+bCv8HxKnlTLVrcDE8zTMJ09R9/ukw4qBs/oZClOfoQk/cOTbDn+NceXfV7j09YPVQUryJPHurafcSg6EVKA==",
+      "license": "MIT",
+      "dependencies": {
+        "@radix-ui/react-compose-refs": "1.1.2"
+      },
+      "peerDependencies": {
+        "@types/react": "*",
+        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
+      },
+      "peerDependenciesMeta": {
+        "@types/react": {
+          "optional": true
+        }
+      }
+    },
     "node_modules/@rolldown/pluginutils": {
       "version": "1.0.0-beta.27",
       "resolved": "https://registry.npmjs.org/@rolldown/pluginutils/-/pluginutils-1.0.0-beta.27.tgz",
@@ -1513,6 +1596,27 @@
         "node": ">= 6"
       }
     },
+    "node_modules/class-variance-authority": {
+      "version": "0.7.1",
+      "resolved": "https://registry.npmjs.org/class-variance-authority/-/class-variance-authority-0.7.1.tgz",
+      "integrity": "sha512-Ka+9Trutv7G8M6WT6SeiRWz792K5qEqIGEGzXKhAE6xOWAY6pPH8U+9IY3oCMv6kqTmLsv7Xh/2w2RigkePMsg==",
+      "license": "Apache-2.0",
+      "dependencies": {
+        "clsx": "^2.1.1"
+      },
+      "funding": {
+        "url": "https://polar.sh/cva"
+      }
+    },
+    "node_modules/clsx": {
+      "version": "2.1.1",
+      "resolved": "https://registry.npmjs.org/clsx/-/clsx-2.1.1.tgz",
+      "integrity": "sha512-eYm0QWBtUrBWZWG0d386OGAw16Z995PiOVo2B7bjWSbHedGl5e0ZWaq65kOGgUSNesEIDkB9ISbTg/JK9dhCZA==",
+      "license": "MIT",
+      "engines": {
+        "node": ">=6"
+      }
+    },
     "node_modules/color-convert": {
       "version": "2.0.1",
       "resolved": "https://registry.npmjs.org/color-convert/-/color-convert-2.0.1.tgz",
@@ -2820,6 +2924,16 @@
         "url": "https://github.com/sponsors/ljharb"
       }
     },
+    "node_modules/tailwind-merge": {
+      "version": "3.4.0",
+      "resolved": "https://registry.npmjs.org/tailwind-merge/-/tailwind-merge-3.4.0.tgz",
+      "integrity": "sha512-uSaO4gnW+b3Y2aWoWfFpX62vn2sR3skfhbjsEnaBI81WD1wBLlHZe5sWf0AqjksNdYTbGBEd0UasQMT3SNV15g==",
+      "license": "MIT",
+      "funding": {
+        "type": "github",
+        "url": "https://github.com/sponsors/dcastil"
+      }
+    },
     "node_modules/tailwindcss": {
       "version": "3.4.18",
       "resolved": "https://registry.npmjs.org/tailwindcss/-/tailwindcss-3.4.18.tgz",
diff --git a/front-meta-generator/metafront/package.json b/front-meta-generator/metafront/package.json
index a24f05f..5dac066 100644
--- a/front-meta-generator/metafront/package.json
+++ b/front-meta-generator/metafront/package.json
@@ -9,21 +9,20 @@
     "preview": "vite preview"
   },
   "dependencies": {
+    "@radix-ui/react-label": "^2.1.8",
+    "class-variance-authority": "^0.7.1",
+    "clsx": "^2.1.1",
     "lucide-react": "^0.553.0",
     "react": "^18.3.1",
-    "react-dom": "^18.3.1"
+    "react-dom": "^18.3.1",
+    "tailwind-merge": "^3.4.0"
   },
   "devDependencies": {
     "@vitejs/plugin-react": "^4.3.0",
     "autoprefixer": "^10.4.22",
     "postcss": "^8.5.6",
+    "sonner": "1.4.0",
     "tailwindcss": "^3.4.18",
-    "vite": "^7.2.4",
-    "sonner": "1.4.0"
-
-
-
+    "vite": "^7.2.4"
   }
-  
-
 }
diff --git a/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx b/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
index b9ebf1b..a2b15c6 100644
--- a/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
+++ b/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
@@ -1,9 +1,9 @@
 import React, { useState } from 'react';
 import { Button } from "@/components/ui/button";
-import  Input  from "@/components/ui/input";
+import Input from "@/components/ui/input";
 import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
-import  Switch  from "@/components/ui/switch";
-import  Badge  from "@/components/ui/badge";
+import Switch from "@/components/ui/switch";
+import Badge from "@/components/ui/badge";
 import { Plus, Trash2, Zap, Clock, Shield, History, GitBranch, X } from 'lucide-react';
 import { cn } from "@/lib/utils";
 
diff --git a/front-meta-generator/metafront/src/components/schema-builder/JsonPreview.jsx b/front-meta-generator/metafront/src/components/schema-builder/JsonPreview.jsx
index 11a0f50..a600850 100644
--- a/front-meta-generator/metafront/src/components/schema-builder/JsonPreview.jsx
+++ b/front-meta-generator/metafront/src/components/schema-builder/JsonPreview.jsx
@@ -4,7 +4,7 @@ import { ScrollArea } from "@/components/ui/scroll-area";
 import { Copy, Check, Download, Minimize2, Maximize2, Rocket, Upload } from 'lucide-react';
 import { toast } from "sonner";
 
-export default function JsonPreview({ entities, appName, nodePositions, onLoadProject }) {
+export default function JsonPreview({ entities, functionalities, appName, nodePositions, onLoadProject }) {
   const [copied, setCopied] = useState(false);
   const [showSimplified, setShowSimplified] = useState(false);
   const [showLoadModal, setShowLoadModal] = useState(false);
@@ -13,6 +13,19 @@ export default function JsonPreview({ entities, appName, nodePositions, onLoadPr
   const simplifiedJson = {
     appName,
     nodePositions,
+    functionalities: functionalities.map(f => ({
+      name: f.name,
+      input: f.input.map(i => ({
+        name: i.name,
+        type: i.type
+      })),
+      output: f.output.map(o => ({
+        name: o.name,
+        type: o.type
+      })),
+      entity: f.entity,
+      exposeInBackoffice: f.exposeInBackoffice
+    })),
     entities: entities.map(e => ({
       name: e.name,
       attributes: e.attributes.map(a => ({
@@ -30,6 +43,19 @@ export default function JsonPreview({ entities, appName, nodePositions, onLoadPr
   const enrichedJson = {
     appName,
     nodePositions,
+    functionalities: functionalities.map(f => ({
+      name: f.name,
+      input: f.input.map(i => ({
+        name: i.name,
+        type: i.type
+      })),
+      output: f.output.map(o => ({
+        name: o.name,
+        type: o.type
+      })),
+      entity: f.entity,
+      exposeInBackoffice: f.exposeInBackoffice
+    })),
     entities: entities.map(e => ({
       name: e.name,
       attributes: e.attributes.map(a => {
@@ -181,7 +207,7 @@ export default function JsonPreview({ entities, appName, nodePositions, onLoadPr
       )}
 
       <div className="p-3 border-t border-slate-800 flex items-center justify-between text-xs text-slate-500">
-        <span>{entities.length} entidades</span>
+        <span>{entities.length} entidades ÔÇó {functionalities.length} funcionalidades</span>
         <span>{jsonString.length.toLocaleString()} caracteres</span>
       </div>
     </div>
diff --git a/front-meta-generator/metafront/src/components/ui/input.jsx b/front-meta-generator/metafront/src/components/ui/input.jsx
index 5c0ff18..eca6f9d 100644
--- a/front-meta-generator/metafront/src/components/ui/input.jsx
+++ b/front-meta-generator/metafront/src/components/ui/input.jsx
@@ -1,8 +1,20 @@
-export default function Input({ className = "", ...props }) {
+import * as React from "react";
+import { cn } from "@/lib/utils";
+
+const Input = React.forwardRef(({ className, type, ...props }, ref) => {
   return (
     <input
-      className={`px-3 py-2 rounded-md border border-slate-700 bg-slate-900 text-slate-100 focus:outline-none focus:ring-2 focus:ring-violet-500 ${className}`}
+      type={type}
+      className={cn(
+        "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
+        className
+      )}
+      ref={ref}
       {...props}
     />
   );
-}
+});
+Input.displayName = "Input";
+
+export { Input };
+export default Input;
diff --git a/front-meta-generator/metafront/src/components/ui/select.jsx b/front-meta-generator/metafront/src/components/ui/select.jsx
index ffbd4b1..7ab5d17 100644
--- a/front-meta-generator/metafront/src/components/ui/select.jsx
+++ b/front-meta-generator/metafront/src/components/ui/select.jsx
@@ -1,6 +1,12 @@
-import { useState, useRef, useEffect } from "react";
+import { useState, useRef, useEffect, createContext, useContext } from "react";
 import { ChevronDown } from "lucide-react";
 
+// ---------------------------
+// CONTEXT
+// ---------------------------
+const SelectContext = createContext();
+const useSelect = () => useContext(SelectContext);
+
 export function Select({ children, value, onValueChange }) {
   const [open, setOpen] = useState(false);
 
@@ -11,14 +17,6 @@ export function Select({ children, value, onValueChange }) {
   );
 }
 
-// ---------------------------
-// CONTEXT
-// ---------------------------
-import { createContext, useContext } from "react";
-
-const SelectContext = createContext();
-const useSelect = () => useContext(SelectContext);
-
 // ---------------------------
 // TRIGGER
 // ---------------------------
diff --git a/front-meta-generator/metafront/src/components/ui/switch.jsx b/front-meta-generator/metafront/src/components/ui/switch.jsx
index f36f9e5..898b337 100644
--- a/front-meta-generator/metafront/src/components/ui/switch.jsx
+++ b/front-meta-generator/metafront/src/components/ui/switch.jsx
@@ -1,12 +1,12 @@
 import { useState } from "react";
 
-export default function Switch({ checked, onChange, className = "" }) {
+export function Switch({ checked, onCheckedChange, className = "" }) {
   const [internalChecked, setInternalChecked] = useState(checked || false);
 
   const toggle = () => {
     const newValue = !internalChecked;
     setInternalChecked(newValue);
-    onChange && onChange(newValue);
+    onCheckedChange && onCheckedChange(newValue);
   };
 
   return (
@@ -30,3 +30,5 @@ export default function Switch({ checked, onChange, className = "" }) {
     </button>
   );
 }
+
+export default Switch;
diff --git a/front-meta-generator/metafront/src/home.jsx b/front-meta-generator/metafront/src/home.jsx
index 443bbba..8ba37fc 100644
--- a/front-meta-generator/metafront/src/home.jsx
+++ b/front-meta-generator/metafront/src/home.jsx
@@ -1,11 +1,17 @@
 import React, { useState, useEffect } from 'react';
 import EntityList from './components/schema-builder/EntityList';
 import EntityEditor from './components/schema-builder/EntityEditor';
+import FunctionalityList from './components/schema-builder/FunctionalityList';
+import FunctionalityEditor from './components/schema-builder/FunctionalityEditor';
 import JsonPreview from './components/schema-builder/JsonPreview';
 import RelationDiagram from './components/schema-builder/RelationDiagram';
+import { Tabs, TabsContent, TabsList, TabsTrigger } from './components/ui/tabs';
+import { Database, Zap } from 'lucide-react';
 
 const STORAGE_KEY = 'schema_builder_entities_v1';
 const SELECTED_ENTITY_KEY = 'schema_builder_selected_entity_v1';
+const FUNCTIONALITIES_KEY = 'schema_builder_functionalities_v1';
+const SELECTED_FUNCTIONALITY_KEY = 'schema_builder_selected_functionality_v1';
 const RIGHT_PANEL_KEY = 'schema_builder_right_panel_v1';
 const APP_NAME_KEY = 'schema_builder_app_name_v1';
 const NODE_POSITIONS_KEY = 'schema_builder_node_positions_v1';
@@ -69,6 +75,34 @@ const loadNodePositions = () => {
   return [];
 };
 
+const loadFunctionalities = () => {
+  if (!storage) return [];
+  try {
+    const raw = storage.getItem(FUNCTIONALITIES_KEY);
+    if (raw) {
+      const parsed = JSON.parse(raw);
+      if (Array.isArray(parsed)) {
+        return parsed.map(func => ({
+          name: func.name,
+          input: (func.input || []).map(field => ({
+            name: field.name,
+            type: field.type
+          })),
+          output: (func.output || []).map(field => ({
+            name: field.name,
+            type: field.type
+          })),
+          entity: func.entity,
+          exposeInBackoffice: func.exposeInBackoffice || false
+        }));
+      }
+    }
+  } catch (err) {
+    console.warn("Erro ao ler funcionalidades:", err);
+  }
+  return [];
+};
+
 const SYSTEM_USER_ENTITY = {
   name: "User",
   fixed: true,
@@ -143,6 +177,9 @@ export default function Home() {
   const [selectedEntity, setSelectedEntity] = useState(null);
   const [rightPanel, setRightPanel] = useState('json');
   const [appName, setAppName] = useState('my-app');
+  const [functionalities, setFunctionalities] = useState(loadFunctionalities);
+  const [selectedFunctionality, setSelectedFunctionality] = useState(null);
+  const [leftPanelTab, setLeftPanelTab] = useState('entities');
 
   useEffect(() => {
     if (storage) {
@@ -154,6 +191,14 @@ export default function Home() {
         setSelectedEntity(entities[0] || null);
       }
 
+      const selectedFunctionalityName = storage.getItem(SELECTED_FUNCTIONALITY_KEY);
+      if (selectedFunctionalityName) {
+        const selected = functionalities.find(f => f.name === selectedFunctionalityName);
+        setSelectedFunctionality(selected || functionalities[0] || null);
+      } else {
+        setSelectedFunctionality(functionalities[0] || null);
+      }
+
       const savedRightPanel = storage.getItem(RIGHT_PANEL_KEY);
       if (savedRightPanel === 'json' || savedRightPanel === 'diagram') {
         setRightPanel(savedRightPanel);
@@ -162,7 +207,7 @@ export default function Home() {
       const savedAppName = storage.getItem(APP_NAME_KEY);
       if (savedAppName) setAppName(savedAppName);
     }
-  }, [entities]);
+  }, [entities, functionalities]);
 
   useEffect(() => {
     storage?.setItem(STORAGE_KEY, JSON.stringify(entities));
@@ -186,6 +231,29 @@ export default function Home() {
     storage?.setItem(APP_NAME_KEY, appName);
   }, [appName]);
 
+  useEffect(() => {
+    storage?.setItem(FUNCTIONALITIES_KEY, JSON.stringify(functionalities));
+  }, [functionalities]);
+
+  useEffect(() => {
+    if (selectedFunctionality) storage?.setItem(SELECTED_FUNCTIONALITY_KEY, selectedFunctionality.name);
+    else storage?.removeItem(SELECTED_FUNCTIONALITY_KEY);
+  }, [selectedFunctionality]);
+
+  useEffect(() => {
+    if (leftPanelTab === 'functionalities') {
+      if (functionalities.length > 0 && !selectedFunctionality) {
+        setSelectedFunctionality(functionalities[0]);
+        setSelectedEntity(null);
+      }
+    } else if (leftPanelTab === 'entities') {
+      if (entities.length > 0 && !selectedEntity) {
+        setSelectedEntity(entities[0]);
+        setSelectedFunctionality(null);
+      }
+    }
+  }, [leftPanelTab, functionalities, entities, selectedFunctionality, selectedEntity]);
+
   const handleUpdateEntity = (updatedEntity) => {
   setEntities(prev => {
     const updatedList = prev.map(e =>
@@ -208,6 +276,7 @@ export default function Home() {
     };
     setEntities(prev => [...prev, newEntity]);
     setSelectedEntity(newEntity);
+    setSelectedFunctionality(null);
   };
 
   const handleDeleteEntity = (entityName) => {
@@ -245,6 +314,44 @@ export default function Home() {
     setSelectedEntity(prev => ({ ...prev, name: newName }));
 };
 
+  const handleAddFunctionality = () => {
+    const newFunctionality = {
+      name: `NovaFuncionalidade${functionalities.length + 1}`,
+      input: [],
+      output: [],
+      entity: null,
+      exposeInBackoffice: false
+    };
+    setFunctionalities(prev => [...prev, newFunctionality]);
+    setSelectedFunctionality(newFunctionality);
+    setSelectedEntity(null);
+  };
+
+  const handleDeleteFunctionality = (functionalityName) => {
+    setFunctionalities(prev => prev.filter(f => f.name !== functionalityName));
+    if (selectedFunctionality?.name === functionalityName) setSelectedFunctionality(null);
+  };
+
+  const handleUpdateFunctionality = (updatedFunctionality) => {
+    setFunctionalities(prev => prev.map(f => f.name === updatedFunctionality.name ? updatedFunctionality : f));
+    setSelectedFunctionality(updatedFunctionality);
+  };
+
+  const handleRenameFunctionality = (oldName, newName) => {
+    setFunctionalities(prev => prev.map(f => f.name === oldName ? { ...f, name: newName } : f));
+    if (selectedFunctionality?.name === oldName) setSelectedFunctionality(prev => ({ ...prev, name: newName }));
+  };
+
+  const handleSelectEntity = (entity) => {
+    setSelectedEntity(entity);
+    setSelectedFunctionality(null);
+  };
+
+  const handleSelectFunctionality = (functionality) => {
+    setSelectedFunctionality(functionality);
+    setSelectedEntity(null);
+  };
+
 
 
   const rightPanelWidth =
@@ -277,6 +384,7 @@ export default function Home() {
             <span className="px-2 py-1 rounded bg-gray-700">
               {entities.reduce((acc, e) => acc + e.attributes.length, 0)} atributos
             </span>
+            <span className="px-2 py-1 rounded bg-gray-700">{functionalities.length} funcionalidades</span>
           </div>
         </div>
       </header>
@@ -284,13 +392,46 @@ export default function Home() {
       <div className="flex-1 flex overflow-hidden">
         {/* LEFT */}
         <div className="w-64 border-r border-gray-800 bg-gray-800/30 flex flex-col">
-          <EntityList
-            entities={entities}
-            selectedEntity={selectedEntity}
-            onSelect={setSelectedEntity}
-            onAdd={handleAddEntity}
-            onDelete={handleDeleteEntity}
-          />
+          <Tabs value={leftPanelTab} onValueChange={setLeftPanelTab} className="flex-1 flex flex-col">
+            <div className="border-b border-slate-800 px-4">
+              <TabsList className="bg-transparent h-12 p-0 gap-1">
+                <TabsTrigger
+                  value="entities"
+                  className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-purple-500"
+                >
+                  <Database className="w-4 h-4" />
+                  Entidades
+                </TabsTrigger>
+                <TabsTrigger
+                  value="functionalities"
+                  className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-emerald-500"
+                >
+                  <Zap className="w-4 h-4" />
+                  Funcionalidades
+                </TabsTrigger>
+              </TabsList>
+            </div>
+
+            <TabsContent value="entities" className="m-0 flex-1">
+              <EntityList
+                entities={entities}
+                selectedEntity={selectedEntity}
+                onSelect={handleSelectEntity}
+                onAdd={handleAddEntity}
+                onDelete={handleDeleteEntity}
+              />
+            </TabsContent>
+
+            <TabsContent value="functionalities" className="m-0 flex-1">
+              <FunctionalityList
+                functionalities={functionalities}
+                selectedFunctionality={selectedFunctionality}
+                onSelect={handleSelectFunctionality}
+                onAdd={handleAddFunctionality}
+                onDelete={handleDeleteFunctionality}
+              />
+            </TabsContent>
+          </Tabs>
         </div>
 
         {/* CENTER */}
@@ -302,11 +443,18 @@ export default function Home() {
               onUpdate={handleUpdateEntity}
               onRename={handleRenameEntity}
             />
+          ) : selectedFunctionality ? (
+            <FunctionalityEditor
+              functionality={selectedFunctionality}
+              allEntities={entities}
+              onUpdate={handleUpdateFunctionality}
+              onRename={handleRenameFunctionality}
+            />
           ) : (
             <div className="h-full flex items-center justify-center text-gray-500">
               <div className="text-center">
                 ­ƒôª
-                <p>Selecione uma entidade para editar</p>
+                <p>Selecione uma entidade ou funcionalidade para editar</p>
                 <p className="text-sm mt-1">ou crie uma nova</p>
               </div>
             </div>
@@ -334,6 +482,7 @@ export default function Home() {
             {rightPanel === 'json' ? (
               <JsonPreview
   entities={entities}
+  functionalities={functionalities}
   appName={appName}
   nodePositions={nodePositions}
   onLoadProject={(data) => {
@@ -350,6 +499,8 @@ export default function Home() {
   setNodePositions(data.nodePositions || []);
   setEntities(finalEntities);
   setSelectedEntity(finalEntities[0] || null);
+  setFunctionalities(data.functionalities || []);
+  setSelectedFunctionality((data.functionalities || [])[0] || null);
 }}
 />
             ) : (
@@ -358,7 +509,7 @@ export default function Home() {
   entities={entities}
   nodePositions={nodePositions}
   setNodePositions={setNodePositions}
-  onSelectEntity={setSelectedEntity}
+  onSelectEntity={handleSelectEntity}
 />
 
               </div>
diff --git a/front-meta-generator/metafront/src/lib/utils.jsx b/front-meta-generator/metafront/src/lib/utils.jsx
index ed9e2e4..10044ce 100644
--- a/front-meta-generator/metafront/src/lib/utils.jsx
+++ b/front-meta-generator/metafront/src/lib/utils.jsx
@@ -1,4 +1,7 @@
-export function cn(...classes) {
-  return classes.filter(Boolean).join(" ");
+import { clsx } from "clsx";
+import { twMerge } from "tailwind-merge";
+
+export function cn(...inputs) {
+  return twMerge(clsx(inputs));
 }
 
diff --git a/oq fazer.md b/oq fazer.md
index a0aa6eb..a8a582e 100644
--- a/oq fazer.md	
+++ b/oq fazer.md	
@@ -257,4 +257,1850 @@ Funcionalidades de neg├│cio	Ô£à
 DTOs espec├¡ficos por caso de uso	Ô£à
 Interfaces de service	Ô£à
 Implementa├º├Áes separadas	Ô£à
-Controllers orientados a a├º├úo	Ô£à
\ No newline at end of file
+Controllers orientados a a├º├úo	Ô£à
+
+
+update 1.1
+
+diff --git a/TODO.md b/TODO.md
+index d349795..7403aaa 100644
+--- a/TODO.md
++++ b/TODO.md
+@@ -7,11 +7,12 @@
+ - [ ] Manter compatibilidade total com entidades sem funcionalidades
+ 
+ ## 2. GeraÔö£┬║Ôö£├║o de DTOs de Funcionalidade
+-- [ ] Criar novo gerador: FunctionalityDtoGenerator
+-- [ ] Para cada funcionalidade: Gerar DTO de entrada (<FunctionalityName>RequestDto)
+-- [ ] Para cada funcionalidade: Gerar DTO de saÔö£┬ída (<FunctionalityName>ResponseDto)
+-- [ ] Colocar em: generated/dto/functionality/
+-- [ ] Regras: NÔö£├óO reutilizar CreateDto ou UpdateDto, campos vÔö£┬¼m da definiÔö£┬║Ôö£├║o da funcionalidade
++- [x] Criar novo gerador: FunctionalityDtoGenerator
++- [x] Para cada funcionalidade: Gerar DTO de entrada (<FunctionalityName>RequestDto)
++- [x] Para cada funcionalidade: Gerar DTO de saÔö£┬ída (<FunctionalityName>ResponseDto)
++- [x] Colocar em: generated/dto/functionality/
++- [x] Regras: NÔö£├óO reutilizar CreateDto ou UpdateDto, campos vÔö£┬¼m da definiÔö£┬║Ôö£├║o da funcionalidade
++- [x] Corrigido bug no mÔö£┬«todo generateFunctionalityDTOs (parÔö£├│metro entityName removido)
+ 
+ ## 3. GeraÔö£┬║Ôö£├║o de Interfaces de ServiÔö£┬║o (NOVA CAMADA)
+ - [ ] Criar novo gerador: FunctionalityServiceInterfaceGenerator
+diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
+index 09eff91..e1df813 100644
+--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
++++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
+@@ -23,14 +23,14 @@ public class GenerationController {
+     }
+ 
+    @PostMapping
+-public String generate(@RequestBody Map<String, Object> payload) {
+-    String appName = (String) payload.get("appName");
+-    @SuppressWarnings("unchecked")
+-    List<Map<String, Object>> entities = (List<Map<String, Object>>) payload.get("entities");
+-    @SuppressWarnings("unchecked")
+-    List<Map<String, Object>> functionalities = (List<Map<String, Object>>) payload.getOrDefault("functionalities", List.of());
+-    generatorService.generateApplication(appName, entities, functionalities);
+-    return "CÔö£Ôöédigo gerado em /" + appName + "/";
+-}
++   public String generate(@RequestBody Map<String, Object> payload) {
++       String appName = (String) payload.get("appName");
++       @SuppressWarnings("unchecked")
++       List<Map<String, Object>> entities = (List<Map<String, Object>>) payload.get("entities");
++       @SuppressWarnings("unchecked")
++       List<Map<String, Object>> functionalities = (List<Map<String, Object>>) payload.getOrDefault("functionalities", List.of());
++       generatorService.generateApplication(appName, entities, functionalities);
++       return "CÔö£Ôöédigo gerado em /" + appName + "/";
++   }
+ }
+ 
+diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
+index 9ebd63c..834e418 100644
+--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
++++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
+@@ -25,10 +25,11 @@ public class FunctionalityControllerGenerator {
+                           .append("    private ").append(serviceInterfaceName).append(" ").append(serviceInterfaceName.substring(0, 1).toLowerCase() + serviceInterfaceName.substring(1)).append(";\n\n");
+ 
+             String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
++            String endpoint = "/" + methodName.replaceAll("([A-Z])", "-$1").toLowerCase();
+             String requestDto = funcName + "RequestDto";
+             String responseDto = funcName + "ResponseDto";
+ 
+-            controllerCode.append("    @PostMapping\n")
++            controllerCode.append("    @PostMapping(\"").append(endpoint).append("\")\n")
+                           .append("    public ").append(responseDto).append(" ").append(methodName).append("(@RequestBody ").append(requestDto).append(" request) {\n")
+                           .append("        return ").append(serviceInterfaceName.substring(0, 1).toLowerCase() + serviceInterfaceName.substring(1)).append(".").append(methodName).append("(request);\n")
+                           .append("    }\n\n");
+diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
+index a903df7..177d852 100644
+--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
++++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
+@@ -11,30 +11,16 @@ public class FunctionalityServiceImplGenerator {
+             String funcName = (String) functionality.get("name");
+             String interfaceName = funcName + "Service";
+             String implName = funcName + "ServiceImpl";
+-            String entityName = (String) functionality.getOrDefault("entity", null);
+ 
+             StringBuilder implCode = new StringBuilder();
+             implCode.append("package com.metagen.backend.generated.service.functionality.impl;\n\n")
+                     .append("import org.springframework.stereotype.Service;\n")
+-                    .append("import org.springframework.beans.factory.annotation.Autowired;\n\n");
+-
+-            if (entityName != null) {
+-                String repositoryName = entityName + "Repository";
+-                implCode.append("import com.metagen.backend.generated.repository.").append(repositoryName).append(";\n")
+-                        .append("import com.metagen.backend.generated.entity.").append(entityName).append(";\n");
+-            }
+-
+-            implCode.append("import com.metagen.backend.generated.dto.functionality.*;\n")
++                    .append("import org.springframework.beans.factory.annotation.Autowired;\n\n")
++                    .append("import com.metagen.backend.generated.dto.functionality.*;\n")
+                     .append("import com.metagen.backend.generated.service.functionality.").append(interfaceName).append(";\n\n")
+                     .append("@Service\n")
+                     .append("public class ").append(implName).append(" implements ").append(interfaceName).append(" {\n\n");
+ 
+-            if (entityName != null) {
+-                String repositoryName = entityName + "Repository";
+-                implCode.append("    @Autowired\n")
+-                        .append("    private ").append(repositoryName).append(" ").append(repositoryName.substring(0, 1).toLowerCase() + repositoryName.substring(1)).append(";\n\n");
+-            }
+-
+             String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
+             String requestDto = funcName + "RequestDto";
+             String responseDto = funcName + "ResponseDto";
+diff --git a/diff.txt b/diff.txt
+deleted file mode 100644
+index 86df831..0000000
+Binary files a/diff.txt and /dev/null differ
+diff --git a/front-meta-generator/metafront/package-lock.json b/front-meta-generator/metafront/package-lock.json
+index 79c73ab..095795f 100644
+--- a/front-meta-generator/metafront/package-lock.json
++++ b/front-meta-generator/metafront/package-lock.json
+@@ -8,9 +8,13 @@
+       "name": "metafront",
+       "version": "1.0.0",
+       "dependencies": {
++        "@radix-ui/react-label": "^2.1.8",
++        "class-variance-authority": "^0.7.1",
++        "clsx": "^2.1.1",
+         "lucide-react": "^0.553.0",
+         "react": "^18.3.1",
+-        "react-dom": "^18.3.1"
++        "react-dom": "^18.3.1",
++        "tailwind-merge": "^3.4.0"
+       },
+       "devDependencies": {
+         "@vitejs/plugin-react": "^4.3.0",
+@@ -876,6 +880,85 @@
+         "node": ">=14"
+       }
+     },
++    "node_modules/@radix-ui/react-compose-refs": {
++      "version": "1.1.2",
++      "resolved": "https://registry.npmjs.org/@radix-ui/react-compose-refs/-/react-compose-refs-1.1.2.tgz",
++      "integrity": "sha512-z4eqJvfiNnFMHIIvXP3CY57y2WJs5g2v3X0zm9mEJkrkNv4rDxu+sg9Jh8EkXyeqBkB7SOcboo9dMVqhyrACIg==",
++      "license": "MIT",
++      "peerDependencies": {
++        "@types/react": "*",
++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
++      },
++      "peerDependenciesMeta": {
++        "@types/react": {
++          "optional": true
++        }
++      }
++    },
++    "node_modules/@radix-ui/react-label": {
++      "version": "2.1.8",
++      "resolved": "https://registry.npmjs.org/@radix-ui/react-label/-/react-label-2.1.8.tgz",
++      "integrity": "sha512-FmXs37I6hSBVDlO4y764TNz1rLgKwjJMQ0EGte6F3Cb3f4bIuHB/iLa/8I9VKkmOy+gNHq8rql3j686ACVV21A==",
++      "license": "MIT",
++      "dependencies": {
++        "@radix-ui/react-primitive": "2.1.4"
++      },
++      "peerDependencies": {
++        "@types/react": "*",
++        "@types/react-dom": "*",
++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc",
++        "react-dom": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
++      },
++      "peerDependenciesMeta": {
++        "@types/react": {
++          "optional": true
++        },
++        "@types/react-dom": {
++          "optional": true
++        }
++      }
++    },
++    "node_modules/@radix-ui/react-primitive": {
++      "version": "2.1.4",
++      "resolved": "https://registry.npmjs.org/@radix-ui/react-primitive/-/react-primitive-2.1.4.tgz",
++      "integrity": "sha512-9hQc4+GNVtJAIEPEqlYqW5RiYdrr8ea5XQ0ZOnD6fgru+83kqT15mq2OCcbe8KnjRZl5vF3ks69AKz3kh1jrhg==",
++      "license": "MIT",
++      "dependencies": {
++        "@radix-ui/react-slot": "1.2.4"
++      },
++      "peerDependencies": {
++        "@types/react": "*",
++        "@types/react-dom": "*",
++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc",
++        "react-dom": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
++      },
++      "peerDependenciesMeta": {
++        "@types/react": {
++          "optional": true
++        },
++        "@types/react-dom": {
++          "optional": true
++        }
++      }
++    },
++    "node_modules/@radix-ui/react-slot": {
++      "version": "1.2.4",
++      "resolved": "https://registry.npmjs.org/@radix-ui/react-slot/-/react-slot-1.2.4.tgz",
++      "integrity": "sha512-Jl+bCv8HxKnlTLVrcDE8zTMJ09R9/ukw4qBs/oZClOfoQk/cOTbDn+NceXfV7j09YPVQUryJPHurafcSg6EVKA==",
++      "license": "MIT",
++      "dependencies": {
++        "@radix-ui/react-compose-refs": "1.1.2"
++      },
++      "peerDependencies": {
++        "@types/react": "*",
++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
++      },
++      "peerDependenciesMeta": {
++        "@types/react": {
++          "optional": true
++        }
++      }
++    },
+     "node_modules/@rolldown/pluginutils": {
+       "version": "1.0.0-beta.27",
+       "resolved": "https://registry.npmjs.org/@rolldown/pluginutils/-/pluginutils-1.0.0-beta.27.tgz",
+@@ -1513,6 +1596,27 @@
+         "node": ">= 6"
+       }
+     },
++    "node_modules/class-variance-authority": {
++      "version": "0.7.1",
++      "resolved": "https://registry.npmjs.org/class-variance-authority/-/class-variance-authority-0.7.1.tgz",
++      "integrity": "sha512-Ka+9Trutv7G8M6WT6SeiRWz792K5qEqIGEGzXKhAE6xOWAY6pPH8U+9IY3oCMv6kqTmLsv7Xh/2w2RigkePMsg==",
++      "license": "Apache-2.0",
++      "dependencies": {
++        "clsx": "^2.1.1"
++      },
++      "funding": {
++        "url": "https://polar.sh/cva"
++      }
++    },
++    "node_modules/clsx": {
++      "version": "2.1.1",
++      "resolved": "https://registry.npmjs.org/clsx/-/clsx-2.1.1.tgz",
++      "integrity": "sha512-eYm0QWBtUrBWZWG0d386OGAw16Z995PiOVo2B7bjWSbHedGl5e0ZWaq65kOGgUSNesEIDkB9ISbTg/JK9dhCZA==",
++      "license": "MIT",
++      "engines": {
++        "node": ">=6"
++      }
++    },
+     "node_modules/color-convert": {
+       "version": "2.0.1",
+       "resolved": "https://registry.npmjs.org/color-convert/-/color-convert-2.0.1.tgz",
+@@ -2820,6 +2924,16 @@
+         "url": "https://github.com/sponsors/ljharb"
+       }
+     },
++    "node_modules/tailwind-merge": {
++      "version": "3.4.0",
++      "resolved": "https://registry.npmjs.org/tailwind-merge/-/tailwind-merge-3.4.0.tgz",
++      "integrity": "sha512-uSaO4gnW+b3Y2aWoWfFpX62vn2sR3skfhbjsEnaBI81WD1wBLlHZe5sWf0AqjksNdYTbGBEd0UasQMT3SNV15g==",
++      "license": "MIT",
++      "funding": {
++        "type": "github",
++        "url": "https://github.com/sponsors/dcastil"
++      }
++    },
+     "node_modules/tailwindcss": {
+       "version": "3.4.18",
+       "resolved": "https://registry.npmjs.org/tailwindcss/-/tailwindcss-3.4.18.tgz",
+diff --git a/front-meta-generator/metafront/package.json b/front-meta-generator/metafront/package.json
+index a24f05f..5dac066 100644
+--- a/front-meta-generator/metafront/package.json
++++ b/front-meta-generator/metafront/package.json
+@@ -9,21 +9,20 @@
+     "preview": "vite preview"
+   },
+   "dependencies": {
++    "@radix-ui/react-label": "^2.1.8",
++    "class-variance-authority": "^0.7.1",
++    "clsx": "^2.1.1",
+     "lucide-react": "^0.553.0",
+     "react": "^18.3.1",
+-    "react-dom": "^18.3.1"
++    "react-dom": "^18.3.1",
++    "tailwind-merge": "^3.4.0"
+   },
+   "devDependencies": {
+     "@vitejs/plugin-react": "^4.3.0",
+     "autoprefixer": "^10.4.22",
+     "postcss": "^8.5.6",
++    "sonner": "1.4.0",
+     "tailwindcss": "^3.4.18",
+-    "vite": "^7.2.4",
+-    "sonner": "1.4.0"
+-
+-
+-
++    "vite": "^7.2.4"
+   }
+-  
+-
+ }
+diff --git a/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx b/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
+index b9ebf1b..a2b15c6 100644
+--- a/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
++++ b/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
+@@ -1,9 +1,9 @@
+ import React, { useState } from 'react';
+ import { Button } from "@/components/ui/button";
+-import  Input  from "@/components/ui/input";
++import Input from "@/components/ui/input";
+ import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
+-import  Switch  from "@/components/ui/switch";
+-import  Badge  from "@/components/ui/badge";
++import Switch from "@/components/ui/switch";
++import Badge from "@/components/ui/badge";
+ import { Plus, Trash2, Zap, Clock, Shield, History, GitBranch, X } from 'lucide-react';
+ import { cn } from "@/lib/utils";
+ 
+diff --git a/front-meta-generator/metafront/src/components/ui/input.jsx b/front-meta-generator/metafront/src/components/ui/input.jsx
+index 5c0ff18..eca6f9d 100644
+--- a/front-meta-generator/metafront/src/components/ui/input.jsx
++++ b/front-meta-generator/metafront/src/components/ui/input.jsx
+@@ -1,8 +1,20 @@
+-export default function Input({ className = "", ...props }) {
++import * as React from "react";
++import { cn } from "@/lib/utils";
++
++const Input = React.forwardRef(({ className, type, ...props }, ref) => {
+   return (
+     <input
+-      className={`px-3 py-2 rounded-md border border-slate-700 bg-slate-900 text-slate-100 focus:outline-none focus:ring-2 focus:ring-violet-500 ${className}`}
++      type={type}
++      className={cn(
++        "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
++        className
++      )}
++      ref={ref}
+       {...props}
+     />
+   );
+-}
++});
++Input.displayName = "Input";
++
++export { Input };
++export default Input;
+diff --git a/front-meta-generator/metafront/src/components/ui/select.jsx b/front-meta-generator/metafront/src/components/ui/select.jsx
+index ffbd4b1..7ab5d17 100644
+--- a/front-meta-generator/metafront/src/components/ui/select.jsx
++++ b/front-meta-generator/metafront/src/components/ui/select.jsx
+@@ -1,6 +1,12 @@
+-import { useState, useRef, useEffect } from "react";
++import { useState, useRef, useEffect, createContext, useContext } from "react";
+ import { ChevronDown } from "lucide-react";
+ 
++// ---------------------------
++// CONTEXT
++// ---------------------------
++const SelectContext = createContext();
++const useSelect = () => useContext(SelectContext);
++
+ export function Select({ children, value, onValueChange }) {
+   const [open, setOpen] = useState(false);
+ 
+@@ -11,14 +17,6 @@ export function Select({ children, value, onValueChange }) {
+   );
+ }
+ 
+-// ---------------------------
+-// CONTEXT
+-// ---------------------------
+-import { createContext, useContext } from "react";
+-
+-const SelectContext = createContext();
+-const useSelect = () => useContext(SelectContext);
+-
+ // ---------------------------
+ // TRIGGER
+ // ---------------------------
+diff --git a/front-meta-generator/metafront/src/components/ui/switch.jsx b/front-meta-generator/metafront/src/components/ui/switch.jsx
+index f36f9e5..898b337 100644
+--- a/front-meta-generator/metafront/src/components/ui/switch.jsx
++++ b/front-meta-generator/metafront/src/components/ui/switch.jsx
+@@ -1,12 +1,12 @@
+ import { useState } from "react";
+ 
+-export default function Switch({ checked, onChange, className = "" }) {
++export function Switch({ checked, onCheckedChange, className = "" }) {
+   const [internalChecked, setInternalChecked] = useState(checked || false);
+ 
+   const toggle = () => {
+     const newValue = !internalChecked;
+     setInternalChecked(newValue);
+-    onChange && onChange(newValue);
++    onCheckedChange && onCheckedChange(newValue);
+   };
+ 
+   return (
+@@ -30,3 +30,5 @@ export default function Switch({ checked, onChange, className = "" }) {
+     </button>
+   );
+ }
++
++export default Switch;
+diff --git a/front-meta-generator/metafront/src/home.jsx b/front-meta-generator/metafront/src/home.jsx
+index 443bbba..8ba37fc 100644
+--- a/front-meta-generator/metafront/src/home.jsx
++++ b/front-meta-generator/metafront/src/home.jsx
+@@ -1,11 +1,17 @@
+ import React, { useState, useEffect } from 'react';
+ import EntityList from './components/schema-builder/EntityList';
+ import EntityEditor from './components/schema-builder/EntityEditor';
++import FunctionalityList from './components/schema-builder/FunctionalityList';
++import FunctionalityEditor from './components/schema-builder/FunctionalityEditor';
+ import JsonPreview from './components/schema-builder/JsonPreview';
+ import RelationDiagram from './components/schema-builder/RelationDiagram';
++import { Tabs, TabsContent, TabsList, TabsTrigger } from './components/ui/tabs';
++import { Database, Zap } from 'lucide-react';
+ 
+ const STORAGE_KEY = 'schema_builder_entities_v1';
+ const SELECTED_ENTITY_KEY = 'schema_builder_selected_entity_v1';
++const FUNCTIONALITIES_KEY = 'schema_builder_functionalities_v1';
++const SELECTED_FUNCTIONALITY_KEY = 'schema_builder_selected_functionality_v1';
+ const RIGHT_PANEL_KEY = 'schema_builder_right_panel_v1';
+ const APP_NAME_KEY = 'schema_builder_app_name_v1';
+ const NODE_POSITIONS_KEY = 'schema_builder_node_positions_v1';
+@@ -69,6 +75,34 @@ const loadNodePositions = () => {
+   return [];
+ };
+ 
++const loadFunctionalities = () => {
++  if (!storage) return [];
++  try {
++    const raw = storage.getItem(FUNCTIONALITIES_KEY);
++    if (raw) {
++      const parsed = JSON.parse(raw);
++      if (Array.isArray(parsed)) {
++        return parsed.map(func => ({
++          name: func.name,
++          input: (func.input || []).map(field => ({
++            name: field.name,
++            type: field.type
++          })),
++          output: (func.output || []).map(field => ({
++            name: field.name,
++            type: field.type
++          })),
++          entity: func.entity,
++          exposeInBackoffice: func.exposeInBackoffice || false
++        }));
++      }
++    }
++  } catch (err) {
++    console.warn("Erro ao ler funcionalidades:", err);
++  }
++  return [];
++};
++
+ const SYSTEM_USER_ENTITY = {
+   name: "User",
+   fixed: true,
+@@ -143,6 +177,9 @@ export default function Home() {
+   const [selectedEntity, setSelectedEntity] = useState(null);
+   const [rightPanel, setRightPanel] = useState('json');
+   const [appName, setAppName] = useState('my-app');
++  const [functionalities, setFunctionalities] = useState(loadFunctionalities);
++  const [selectedFunctionality, setSelectedFunctionality] = useState(null);
++  const [leftPanelTab, setLeftPanelTab] = useState('entities');
+ 
+   useEffect(() => {
+     if (storage) {
+@@ -154,6 +191,14 @@ export default function Home() {
+         setSelectedEntity(entities[0] || null);
+       }
+ 
++      const selectedFunctionalityName = storage.getItem(SELECTED_FUNCTIONALITY_KEY);
++      if (selectedFunctionalityName) {
++        const selected = functionalities.find(f => f.name === selectedFunctionalityName);
++        setSelectedFunctionality(selected || functionalities[0] || null);
++      } else {
++        setSelectedFunctionality(functionalities[0] || null);
++      }
++
+       const savedRightPanel = storage.getItem(RIGHT_PANEL_KEY);
+       if (savedRightPanel === 'json' || savedRightPanel === 'diagram') {
+         setRightPanel(savedRightPanel);
+@@ -162,7 +207,7 @@ export default function Home() {
+       const savedAppName = storage.getItem(APP_NAME_KEY);
+       if (savedAppName) setAppName(savedAppName);
+     }
+-  }, [entities]);
++  }, [entities, functionalities]);
+ 
+   useEffect(() => {
+     storage?.setItem(STORAGE_KEY, JSON.stringify(entities));
+@@ -186,6 +231,29 @@ export default function Home() {
+     storage?.setItem(APP_NAME_KEY, appName);
+   }, [appName]);
+ 
++  useEffect(() => {
++    storage?.setItem(FUNCTIONALITIES_KEY, JSON.stringify(functionalities));
++  }, [functionalities]);
++
++  useEffect(() => {
++    if (selectedFunctionality) storage?.setItem(SELECTED_FUNCTIONALITY_KEY, selectedFunctionality.name);
++    else storage?.removeItem(SELECTED_FUNCTIONALITY_KEY);
++  }, [selectedFunctionality]);
++
++  useEffect(() => {
++    if (leftPanelTab === 'functionalities') {
++      if (functionalities.length > 0 && !selectedFunctionality) {
++        setSelectedFunctionality(functionalities[0]);
++        setSelectedEntity(null);
++      }
++    } else if (leftPanelTab === 'entities') {
++      if (entities.length > 0 && !selectedEntity) {
++        setSelectedEntity(entities[0]);
++        setSelectedFunctionality(null);
++      }
++    }
++  }, [leftPanelTab, functionalities, entities, selectedFunctionality, selectedEntity]);
++
+   const handleUpdateEntity = (updatedEntity) => {
+   setEntities(prev => {
+     const updatedList = prev.map(e =>
+@@ -208,6 +276,7 @@ export default function Home() {
+     };
+     setEntities(prev => [...prev, newEntity]);
+     setSelectedEntity(newEntity);
++    setSelectedFunctionality(null);
+   };
+ 
+   const handleDeleteEntity = (entityName) => {
+@@ -245,6 +314,44 @@ export default function Home() {
+     setSelectedEntity(prev => ({ ...prev, name: newName }));
+ };
+ 
++  const handleAddFunctionality = () => {
++    const newFunctionality = {
++      name: `NovaFuncionalidade${functionalities.length + 1}`,
++      input: [],
++      output: [],
++      entity: null,
++      exposeInBackoffice: false
++    };
++    setFunctionalities(prev => [...prev, newFunctionality]);
++    setSelectedFunctionality(newFunctionality);
++    setSelectedEntity(null);
++  };
++
++  const handleDeleteFunctionality = (functionalityName) => {
++    setFunctionalities(prev => prev.filter(f => f.name !== functionalityName));
++    if (selectedFunctionality?.name === functionalityName) setSelectedFunctionality(null);
++  };
++
++  const handleUpdateFunctionality = (updatedFunctionality) => {
++    setFunctionalities(prev => prev.map(f => f.name === updatedFunctionality.name ? updatedFunctionality : f));
++    setSelectedFunctionality(updatedFunctionality);
++  };
++
++  const handleRenameFunctionality = (oldName, newName) => {
++    setFunctionalities(prev => prev.map(f => f.name === oldName ? { ...f, name: newName } : f));
++    if (selectedFunctionality?.name === oldName) setSelectedFunctionality(prev => ({ ...prev, name: newName }));
++  };
++
++  const handleSelectEntity = (entity) => {
++    setSelectedEntity(entity);
++    setSelectedFunctionality(null);
++  };
++
++  const handleSelectFunctionality = (functionality) => {
++    setSelectedFunctionality(functionality);
++    setSelectedEntity(null);
++  };
++
+ 
+ 
+   const rightPanelWidth =
+@@ -277,6 +384,7 @@ export default function Home() {
+             <span className="px-2 py-1 rounded bg-gray-700">
+               {entities.reduce((acc, e) => acc + e.attributes.length, 0)} atributos
+             </span>
++            <span className="px-2 py-1 rounded bg-gray-700">{functionalities.length} funcionalidades</span>
+           </div>
+         </div>
+       </header>
+@@ -284,13 +392,46 @@ export default function Home() {
+       <div className="flex-1 flex overflow-hidden">
+         {/* LEFT */}
+         <div className="w-64 border-r border-gray-800 bg-gray-800/30 flex flex-col">
+-          <EntityList
+-            entities={entities}
+-            selectedEntity={selectedEntity}
+-            onSelect={setSelectedEntity}
+-            onAdd={handleAddEntity}
+-            onDelete={handleDeleteEntity}
+-          />
++          <Tabs value={leftPanelTab} onValueChange={setLeftPanelTab} className="flex-1 flex flex-col">
++            <div className="border-b border-slate-800 px-4">
++              <TabsList className="bg-transparent h-12 p-0 gap-1">
++                <TabsTrigger
++                  value="entities"
++                  className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-purple-500"
++                >
++                  <Database className="w-4 h-4" />
++                  Entidades
++                </TabsTrigger>
++                <TabsTrigger
++                  value="functionalities"
++                  className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-emerald-500"
++                >
++                  <Zap className="w-4 h-4" />
++                  Funcionalidades
++                </TabsTrigger>
++              </TabsList>
++            </div>
++
++            <TabsContent value="entities" className="m-0 flex-1">
++              <EntityList
++                entities={entities}
++                selectedEntity={selectedEntity}
++                onSelect={handleSelectEntity}
++                onAdd={handleAddEntity}
++                onDelete={handleDeleteEntity}
++              />
++            </TabsContent>
++
++            <TabsContent value="functionalities" className="m-0 flex-1">
++              <FunctionalityList
++                functionalities={functionalities}
++                selectedFunctionality={selectedFunctionality}
++                onSelect={handleSelectFunctionality}
++                onAdd={handleAddFunctionality}
++                onDelete={handleDeleteFunctionality}
++              />
++            </TabsContent>
++          </Tabs>
+         </div>
+ 
+         {/* CENTER */}
+@@ -302,11 +443,18 @@ export default function Home() {
+               onUpdate={handleUpdateEntity}
+               onRename={handleRenameEntity}
+             />
++          ) : selectedFunctionality ? (
++            <FunctionalityEditor
++              functionality={selectedFunctionality}
++              allEntities={entities}
++              onUpdate={handleUpdateFunctionality}
++              onRename={handleRenameFunctionality}
++            />
+           ) : (
+             <div className="h-full flex items-center justify-center text-gray-500">
+               <div className="text-center">
+                 ┬¡ãÆ├┤┬¬
+-                <p>Selecione uma entidade para editar</p>
++                <p>Selecione uma entidade ou funcionalidade para editar</p>
+                 <p className="text-sm mt-1">ou crie uma nova</p>
+               </div>
+             </div>
+@@ -334,6 +482,7 @@ export default function Home() {
+             {rightPanel === 'json' ? (
+               <JsonPreview
+   entities={entities}
++  functionalities={functionalities}
+   appName={appName}
+   nodePositions={nodePositions}
+   onLoadProject={(data) => {
+@@ -350,6 +499,8 @@ export default function Home() {
+   setNodePositions(data.nodePositions || []);
+   setEntities(finalEntities);
+   setSelectedEntity(finalEntities[0] || null);
++  setFunctionalities(data.functionalities || []);
++  setSelectedFunctionality((data.functionalities || [])[0] || null);
+ }}
+ />
+             ) : (
+@@ -358,7 +509,7 @@ export default function Home() {
+   entities={entities}
+   nodePositions={nodePositions}
+   setNodePositions={setNodePositions}
+-  onSelectEntity={setSelectedEntity}
++  onSelectEntity={handleSelectEntity}
+ />
+ 
+               </div>
+diff --git a/front-meta-generator/metafront/src/lib/utils.jsx b/front-meta-generator/metafront/src/lib/utils.jsx
+index ed9e2e4..10044ce 100644
+--- a/front-meta-generator/metafront/src/lib/utils.jsx
++++ b/front-meta-generator/metafront/src/lib/utils.jsx
+@@ -1,4 +1,7 @@
+-export function cn(...classes) {
+-  return classes.filter(Boolean).join(" ");
++import { clsx } from "clsx";
++import { twMerge } from "tailwind-merge";
++
++export function cn(...inputs) {
++  return twMerge(clsx(inputs));
+ }
+ 
+diff --git a/oq fazer.md b/oq fazer.md
+index a0aa6eb..660f1f3 100644
+--- a/oq fazer.md	
++++ b/oq fazer.md	
+@@ -257,4 +257,1142 @@ Funcionalidades de negÔö£Ôöécio	├ö┬ú├á
+ DTOs especÔö£┬íficos por caso de uso	├ö┬ú├á
+ Interfaces de service	├ö┬ú├á
+ ImplementaÔö£┬║Ôö£├ües separadas	├ö┬ú├á
+-Controllers orientados a aÔö£┬║Ôö£├║o	├ö┬ú├á
+\ No newline at end of file
++Controllers orientados a aÔö£┬║Ôö£├║o	├ö┬ú├á
++
++
++update 1.1
++
++diff --git a/TODO.md b/TODO.md
++index d349795..7403aaa 100644
++--- a/TODO.md
+++++ b/TODO.md
++@@ -7,11 +7,12 @@
++ - [ ] Manter compatibilidade total com entidades sem funcionalidades
++ 
++ ## 2. Gera├ö├Â┬úÔö¼Ôòæ├ö├Â┬úÔö£Ôòæo de DTOs de Funcionalidade
++-- [ ] Criar novo gerador: FunctionalityDtoGenerator
++-- [ ] Para cada funcionalidade: Gerar DTO de entrada (<FunctionalityName>RequestDto)
++-- [ ] Para cada funcionalidade: Gerar DTO de sa├ö├Â┬úÔö¼├¡da (<FunctionalityName>ResponseDto)
++-- [ ] Colocar em: generated/dto/functionality/
++-- [ ] Regras: N├ö├Â┬úÔö£├│O reutilizar CreateDto ou UpdateDto, campos v├ö├Â┬úÔö¼┬╝m da defini├ö├Â┬úÔö¼Ôòæ├ö├Â┬úÔö£Ôòæo da funcionalidade
+++- [x] Criar novo gerador: FunctionalityDtoGenerator
+++- [x] Para cada funcionalidade: Gerar DTO de entrada (<FunctionalityName>RequestDto)
+++- [x] Para cada funcionalidade: Gerar DTO de sa├ö├Â┬úÔö¼├¡da (<FunctionalityName>ResponseDto)
+++- [x] Colocar em: generated/dto/functionality/
+++- [x] Regras: N├ö├Â┬úÔö£├│O reutilizar CreateDto ou UpdateDto, campos v├ö├Â┬úÔö¼┬╝m da defini├ö├Â┬úÔö¼Ôòæ├ö├Â┬úÔö£Ôòæo da funcionalidade
+++- [x] Corrigido bug no m├ö├Â┬úÔö¼┬½todo generateFunctionalityDTOs (par├ö├Â┬úÔö£Ôöémetro entityName removido)
++ 
++ ## 3. Gera├ö├Â┬úÔö¼Ôòæ├ö├Â┬úÔö£Ôòæo de Interfaces de Servi├ö├Â┬úÔö¼Ôòæo (NOVA CAMADA)
++ - [ ] Criar novo gerador: FunctionalityServiceInterfaceGenerator
++diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
++index 09eff91..e1df813 100644
++--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
+++++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
++@@ -23,14 +23,14 @@ public class GenerationController {
++     }
++ 
++    @PostMapping
++-public String generate(@RequestBody Map<String, Object> payload) {
++-    String appName = (String) payload.get("appName");
++-    @SuppressWarnings("unchecked")
++-    List<Map<String, Object>> entities = (List<Map<String, Object>>) payload.get("entities");
++-    @SuppressWarnings("unchecked")
++-    List<Map<String, Object>> functionalities = (List<Map<String, Object>>) payload.getOrDefault("functionalities", List.of());
++-    generatorService.generateApplication(appName, entities, functionalities);
++-    return "C├ö├Â┬ú├ö├Â├®digo gerado em /" + appName + "/";
++-}
+++   public String generate(@RequestBody Map<String, Object> payload) {
+++       String appName = (String) payload.get("appName");
+++       @SuppressWarnings("unchecked")
+++       List<Map<String, Object>> entities = (List<Map<String, Object>>) payload.get("entities");
+++       @SuppressWarnings("unchecked")
+++       List<Map<String, Object>> functionalities = (List<Map<String, Object>>) payload.getOrDefault("functionalities", List.of());
+++       generatorService.generateApplication(appName, entities, functionalities);
+++       return "C├ö├Â┬ú├ö├Â├®digo gerado em /" + appName + "/";
+++   }
++ }
++ 
++diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
++index 9ebd63c..834e418 100644
++--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
+++++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
++@@ -25,10 +25,11 @@ public class FunctionalityControllerGenerator {
++                           .append("    private ").append(serviceInterfaceName).append(" ").append(serviceInterfaceName.substring(0, 1).toLowerCase() + serviceInterfaceName.substring(1)).append(";\n\n");
++ 
++             String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
+++            String endpoint = "/" + methodName.replaceAll("([A-Z])", "-$1").toLowerCase();
++             String requestDto = funcName + "RequestDto";
++             String responseDto = funcName + "ResponseDto";
++ 
++-            controllerCode.append("    @PostMapping\n")
+++            controllerCode.append("    @PostMapping(\"").append(endpoint).append("\")\n")
++                           .append("    public ").append(responseDto).append(" ").append(methodName).append("(@RequestBody ").append(requestDto).append(" request) {\n")
++                           .append("        return ").append(serviceInterfaceName.substring(0, 1).toLowerCase() + serviceInterfaceName.substring(1)).append(".").append(methodName).append("(request);\n")
++                           .append("    }\n\n");
++diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
++index a903df7..177d852 100644
++--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
+++++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
++@@ -11,30 +11,16 @@ public class FunctionalityServiceImplGenerator {
++             String funcName = (String) functionality.get("name");
++             String interfaceName = funcName + "Service";
++             String implName = funcName + "ServiceImpl";
++-            String entityName = (String) functionality.getOrDefault("entity", null);
++ 
++             StringBuilder implCode = new StringBuilder();
++             implCode.append("package com.metagen.backend.generated.service.functionality.impl;\n\n")
++                     .append("import org.springframework.stereotype.Service;\n")
++-                    .append("import org.springframework.beans.factory.annotation.Autowired;\n\n");
++-
++-            if (entityName != null) {
++-                String repositoryName = entityName + "Repository";
++-                implCode.append("import com.metagen.backend.generated.repository.").append(repositoryName).append(";\n")
++-                        .append("import com.metagen.backend.generated.entity.").append(entityName).append(";\n");
++-            }
++-
++-            implCode.append("import com.metagen.backend.generated.dto.functionality.*;\n")
+++                    .append("import org.springframework.beans.factory.annotation.Autowired;\n\n")
+++                    .append("import com.metagen.backend.generated.dto.functionality.*;\n")
++                     .append("import com.metagen.backend.generated.service.functionality.").append(interfaceName).append(";\n\n")
++                     .append("@Service\n")
++                     .append("public class ").append(implName).append(" implements ").append(interfaceName).append(" {\n\n");
++ 
++-            if (entityName != null) {
++-                String repositoryName = entityName + "Repository";
++-                implCode.append("    @Autowired\n")
++-                        .append("    private ").append(repositoryName).append(" ").append(repositoryName.substring(0, 1).toLowerCase() + repositoryName.substring(1)).append(";\n\n");
++-            }
++-
++             String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
++             String requestDto = funcName + "RequestDto";
++             String responseDto = funcName + "ResponseDto";
++diff --git a/diff.txt b/diff.txt
++deleted file mode 100644
++index 86df831..0000000
++Binary files a/diff.txt and /dev/null differ
++diff --git a/front-meta-generator/metafront/package-lock.json b/front-meta-generator/metafront/package-lock.json
++index 79c73ab..095795f 100644
++--- a/front-meta-generator/metafront/package-lock.json
+++++ b/front-meta-generator/metafront/package-lock.json
++@@ -8,9 +8,13 @@
++       "name": "metafront",
++       "version": "1.0.0",
++       "dependencies": {
+++        "@radix-ui/react-label": "^2.1.8",
+++        "class-variance-authority": "^0.7.1",
+++        "clsx": "^2.1.1",
++         "lucide-react": "^0.553.0",
++         "react": "^18.3.1",
++-        "react-dom": "^18.3.1"
+++        "react-dom": "^18.3.1",
+++        "tailwind-merge": "^3.4.0"
++       },
++       "devDependencies": {
++         "@vitejs/plugin-react": "^4.3.0",
++@@ -876,6 +880,85 @@
++         "node": ">=14"
++       }
++     },
+++    "node_modules/@radix-ui/react-compose-refs": {
+++      "version": "1.1.2",
+++      "resolved": "https://registry.npmjs.org/@radix-ui/react-compose-refs/-/react-compose-refs-1.1.2.tgz",
+++      "integrity": "sha512-z4eqJvfiNnFMHIIvXP3CY57y2WJs5g2v3X0zm9mEJkrkNv4rDxu+sg9Jh8EkXyeqBkB7SOcboo9dMVqhyrACIg==",
+++      "license": "MIT",
+++      "peerDependencies": {
+++        "@types/react": "*",
+++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
+++      },
+++      "peerDependenciesMeta": {
+++        "@types/react": {
+++          "optional": true
+++        }
+++      }
+++    },
+++    "node_modules/@radix-ui/react-label": {
+++      "version": "2.1.8",
+++      "resolved": "https://registry.npmjs.org/@radix-ui/react-label/-/react-label-2.1.8.tgz",
+++      "integrity": "sha512-FmXs37I6hSBVDlO4y764TNz1rLgKwjJMQ0EGte6F3Cb3f4bIuHB/iLa/8I9VKkmOy+gNHq8rql3j686ACVV21A==",
+++      "license": "MIT",
+++      "dependencies": {
+++        "@radix-ui/react-primitive": "2.1.4"
+++      },
+++      "peerDependencies": {
+++        "@types/react": "*",
+++        "@types/react-dom": "*",
+++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc",
+++        "react-dom": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
+++      },
+++      "peerDependenciesMeta": {
+++        "@types/react": {
+++          "optional": true
+++        },
+++        "@types/react-dom": {
+++          "optional": true
+++        }
+++      }
+++    },
+++    "node_modules/@radix-ui/react-primitive": {
+++      "version": "2.1.4",
+++      "resolved": "https://registry.npmjs.org/@radix-ui/react-primitive/-/react-primitive-2.1.4.tgz",
+++      "integrity": "sha512-9hQc4+GNVtJAIEPEqlYqW5RiYdrr8ea5XQ0ZOnD6fgru+83kqT15mq2OCcbe8KnjRZl5vF3ks69AKz3kh1jrhg==",
+++      "license": "MIT",
+++      "dependencies": {
+++        "@radix-ui/react-slot": "1.2.4"
+++      },
+++      "peerDependencies": {
+++        "@types/react": "*",
+++        "@types/react-dom": "*",
+++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc",
+++        "react-dom": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
+++      },
+++      "peerDependenciesMeta": {
+++        "@types/react": {
+++          "optional": true
+++        },
+++        "@types/react-dom": {
+++          "optional": true
+++        }
+++      }
+++    },
+++    "node_modules/@radix-ui/react-slot": {
+++      "version": "1.2.4",
+++      "resolved": "https://registry.npmjs.org/@radix-ui/react-slot/-/react-slot-1.2.4.tgz",
+++      "integrity": "sha512-Jl+bCv8HxKnlTLVrcDE8zTMJ09R9/ukw4qBs/oZClOfoQk/cOTbDn+NceXfV7j09YPVQUryJPHurafcSg6EVKA==",
+++      "license": "MIT",
+++      "dependencies": {
+++        "@radix-ui/react-compose-refs": "1.1.2"
+++      },
+++      "peerDependencies": {
+++        "@types/react": "*",
+++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
+++      },
+++      "peerDependenciesMeta": {
+++        "@types/react": {
+++          "optional": true
+++        }
+++      }
+++    },
++     "node_modules/@rolldown/pluginutils": {
++       "version": "1.0.0-beta.27",
++       "resolved": "https://registry.npmjs.org/@rolldown/pluginutils/-/pluginutils-1.0.0-beta.27.tgz",
++@@ -1513,6 +1596,27 @@
++         "node": ">= 6"
++       }
++     },
+++    "node_modules/class-variance-authority": {
+++      "version": "0.7.1",
+++      "resolved": "https://registry.npmjs.org/class-variance-authority/-/class-variance-authority-0.7.1.tgz",
+++      "integrity": "sha512-Ka+9Trutv7G8M6WT6SeiRWz792K5qEqIGEGzXKhAE6xOWAY6pPH8U+9IY3oCMv6kqTmLsv7Xh/2w2RigkePMsg==",
+++      "license": "Apache-2.0",
+++      "dependencies": {
+++        "clsx": "^2.1.1"
+++      },
+++      "funding": {
+++        "url": "https://polar.sh/cva"
+++      }
+++    },
+++    "node_modules/clsx": {
+++      "version": "2.1.1",
+++      "resolved": "https://registry.npmjs.org/clsx/-/clsx-2.1.1.tgz",
+++      "integrity": "sha512-eYm0QWBtUrBWZWG0d386OGAw16Z995PiOVo2B7bjWSbHedGl5e0ZWaq65kOGgUSNesEIDkB9ISbTg/JK9dhCZA==",
+++      "license": "MIT",
+++      "engines": {
+++        "node": ">=6"
+++      }
+++    },
++     "node_modules/color-convert": {
++       "version": "2.0.1",
++       "resolved": "https://registry.npmjs.org/color-convert/-/color-convert-2.0.1.tgz",
++@@ -2820,6 +2924,16 @@
++         "url": "https://github.com/sponsors/ljharb"
++       }
++     },
+++    "node_modules/tailwind-merge": {
+++      "version": "3.4.0",
+++      "resolved": "https://registry.npmjs.org/tailwind-merge/-/tailwind-merge-3.4.0.tgz",
+++      "integrity": "sha512-uSaO4gnW+b3Y2aWoWfFpX62vn2sR3skfhbjsEnaBI81WD1wBLlHZe5sWf0AqjksNdYTbGBEd0UasQMT3SNV15g==",
+++      "license": "MIT",
+++      "funding": {
+++        "type": "github",
+++        "url": "https://github.com/sponsors/dcastil"
+++      }
+++    },
++     "node_modules/tailwindcss": {
++       "version": "3.4.18",
++       "resolved": "https://registry.npmjs.org/tailwindcss/-/tailwindcss-3.4.18.tgz",
++diff --git a/front-meta-generator/metafront/package.json b/front-meta-generator/metafront/package.json
++index a24f05f..5dac066 100644
++--- a/front-meta-generator/metafront/package.json
+++++ b/front-meta-generator/metafront/package.json
++@@ -9,21 +9,20 @@
++     "preview": "vite preview"
++   },
++   "dependencies": {
+++    "@radix-ui/react-label": "^2.1.8",
+++    "class-variance-authority": "^0.7.1",
+++    "clsx": "^2.1.1",
++     "lucide-react": "^0.553.0",
++     "react": "^18.3.1",
++-    "react-dom": "^18.3.1"
+++    "react-dom": "^18.3.1",
+++    "tailwind-merge": "^3.4.0"
++   },
++   "devDependencies": {
++     "@vitejs/plugin-react": "^4.3.0",
++     "autoprefixer": "^10.4.22",
++     "postcss": "^8.5.6",
+++    "sonner": "1.4.0",
++     "tailwindcss": "^3.4.18",
++-    "vite": "^7.2.4",
++-    "sonner": "1.4.0"
++-
++-
++-
+++    "vite": "^7.2.4"
++   }
++-  
++-
++ }
++diff --git a/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx b/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
++index b9ebf1b..a2b15c6 100644
++--- a/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
+++++ b/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
++@@ -1,9 +1,9 @@
++ import React, { useState } from 'react';
++ import { Button } from "@/components/ui/button";
++-import  Input  from "@/components/ui/input";
+++import Input from "@/components/ui/input";
++ import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
++-import  Switch  from "@/components/ui/switch";
++-import  Badge  from "@/components/ui/badge";
+++import Switch from "@/components/ui/switch";
+++import Badge from "@/components/ui/badge";
++ import { Plus, Trash2, Zap, Clock, Shield, History, GitBranch, X } from 'lucide-react';
++ import { cn } from "@/lib/utils";
++ 
++diff --git a/front-meta-generator/metafront/src/components/ui/input.jsx b/front-meta-generator/metafront/src/components/ui/input.jsx
++index 5c0ff18..eca6f9d 100644
++--- a/front-meta-generator/metafront/src/components/ui/input.jsx
+++++ b/front-meta-generator/metafront/src/components/ui/input.jsx
++@@ -1,8 +1,20 @@
++-export default function Input({ className = "", ...props }) {
+++import * as React from "react";
+++import { cn } from "@/lib/utils";
+++
+++const Input = React.forwardRef(({ className, type, ...props }, ref) => {
++   return (
++     <input
++-      className={`px-3 py-2 rounded-md border border-slate-700 bg-slate-900 text-slate-100 focus:outline-none focus:ring-2 focus:ring-violet-500 ${className}`}
+++      type={type}
+++      className={cn(
+++        "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
+++        className
+++      )}
+++      ref={ref}
++       {...props}
++     />
++   );
++-}
+++});
+++Input.displayName = "Input";
+++
+++export { Input };
+++export default Input;
++diff --git a/front-meta-generator/metafront/src/components/ui/select.jsx b/front-meta-generator/metafront/src/components/ui/select.jsx
++index ffbd4b1..7ab5d17 100644
++--- a/front-meta-generator/metafront/src/components/ui/select.jsx
+++++ b/front-meta-generator/metafront/src/components/ui/select.jsx
++@@ -1,6 +1,12 @@
++-import { useState, useRef, useEffect } from "react";
+++import { useState, useRef, useEffect, createContext, useContext } from "react";
++ import { ChevronDown } from "lucide-react";
++ 
+++// ---------------------------
+++// CONTEXT
+++// ---------------------------
+++const SelectContext = createContext();
+++const useSelect = () => useContext(SelectContext);
+++
++ export function Select({ children, value, onValueChange }) {
++   const [open, setOpen] = useState(false);
++ 
++@@ -11,14 +17,6 @@ export function Select({ children, value, onValueChange }) {
++   );
++ }
++ 
++-// ---------------------------
++-// CONTEXT
++-// ---------------------------
++-import { createContext, useContext } from "react";
++-
++-const SelectContext = createContext();
++-const useSelect = () => useContext(SelectContext);
++-
++ // ---------------------------
++ // TRIGGER
++ // ---------------------------
++diff --git a/front-meta-generator/metafront/src/components/ui/switch.jsx b/front-meta-generator/metafront/src/components/ui/switch.jsx
++index f36f9e5..898b337 100644
++--- a/front-meta-generator/metafront/src/components/ui/switch.jsx
+++++ b/front-meta-generator/metafront/src/components/ui/switch.jsx
++@@ -1,12 +1,12 @@
++ import { useState } from "react";
++ 
++-export default function Switch({ checked, onChange, className = "" }) {
+++export function Switch({ checked, onCheckedChange, className = "" }) {
++   const [internalChecked, setInternalChecked] = useState(checked || false);
++ 
++   const toggle = () => {
++     const newValue = !internalChecked;
++     setInternalChecked(newValue);
++-    onChange && onChange(newValue);
+++    onCheckedChange && onCheckedChange(newValue);
++   };
++ 
++   return (
++@@ -30,3 +30,5 @@ export default function Switch({ checked, onChange, className = "" }) {
++     </button>
++   );
++ }
+++
+++export default Switch;
++diff --git a/front-meta-generator/metafront/src/home.jsx b/front-meta-generator/metafront/src/home.jsx
++index 443bbba..e249915 100644
++--- a/front-meta-generator/metafront/src/home.jsx
+++++ b/front-meta-generator/metafront/src/home.jsx
++@@ -1,11 +1,17 @@
++ import React, { useState, useEffect } from 'react';
++ import EntityList from './components/schema-builder/EntityList';
++ import EntityEditor from './components/schema-builder/EntityEditor';
+++import FunctionalityList from './components/schema-builder/FunctionalityList';
+++import FunctionalityEditor from './components/schema-builder/FunctionalityEditor';
++ import JsonPreview from './components/schema-builder/JsonPreview';
++ import RelationDiagram from './components/schema-builder/RelationDiagram';
+++import { Tabs, TabsContent, TabsList, TabsTrigger } from './components/ui/tabs';
+++import { Database, Zap } from 'lucide-react';
++ 
++ const STORAGE_KEY = 'schema_builder_entities_v1';
++ const SELECTED_ENTITY_KEY = 'schema_builder_selected_entity_v1';
+++const FUNCTIONALITIES_KEY = 'schema_builder_functionalities_v1';
+++const SELECTED_FUNCTIONALITY_KEY = 'schema_builder_selected_functionality_v1';
++ const RIGHT_PANEL_KEY = 'schema_builder_right_panel_v1';
++ const APP_NAME_KEY = 'schema_builder_app_name_v1';
++ const NODE_POSITIONS_KEY = 'schema_builder_node_positions_v1';
++@@ -69,6 +75,34 @@ const loadNodePositions = () => {
++   return [];
++ };
++ 
+++const loadFunctionalities = () => {
+++  if (!storage) return [];
+++  try {
+++    const raw = storage.getItem(FUNCTIONALITIES_KEY);
+++    if (raw) {
+++      const parsed = JSON.parse(raw);
+++      if (Array.isArray(parsed)) {
+++        return parsed.map(func => ({
+++          name: func.name,
+++          input: (func.input || []).map(field => ({
+++            name: field.name,
+++            type: field.type
+++          })),
+++          output: (func.output || []).map(field => ({
+++            name: field.name,
+++            type: field.type
+++          })),
+++          entity: func.entity,
+++          exposeInBackoffice: func.exposeInBackoffice || false
+++        }));
+++      }
+++    }
+++  } catch (err) {
+++    console.warn("Erro ao ler funcionalidades:", err);
+++  }
+++  return [];
+++};
+++
++ const SYSTEM_USER_ENTITY = {
++   name: "User",
++   fixed: true,
++@@ -143,6 +177,9 @@ export default function Home() {
++   const [selectedEntity, setSelectedEntity] = useState(null);
++   const [rightPanel, setRightPanel] = useState('json');
++   const [appName, setAppName] = useState('my-app');
+++  const [functionalities, setFunctionalities] = useState(loadFunctionalities);
+++  const [selectedFunctionality, setSelectedFunctionality] = useState(null);
+++  const [leftPanelTab, setLeftPanelTab] = useState('entities');
++ 
++   useEffect(() => {
++     if (storage) {
++@@ -154,6 +191,14 @@ export default function Home() {
++         setSelectedEntity(entities[0] || null);
++       }
++ 
+++      const selectedFunctionalityName = storage.getItem(SELECTED_FUNCTIONALITY_KEY);
+++      if (selectedFunctionalityName) {
+++        const selected = functionalities.find(f => f.name === selectedFunctionalityName);
+++        setSelectedFunctionality(selected || functionalities[0] || null);
+++      } else {
+++        setSelectedFunctionality(functionalities[0] || null);
+++      }
+++
++       const savedRightPanel = storage.getItem(RIGHT_PANEL_KEY);
++       if (savedRightPanel === 'json' || savedRightPanel === 'diagram') {
++         setRightPanel(savedRightPanel);
++@@ -162,7 +207,7 @@ export default function Home() {
++       const savedAppName = storage.getItem(APP_NAME_KEY);
++       if (savedAppName) setAppName(savedAppName);
++     }
++-  }, [entities]);
+++  }, [entities, functionalities]);
++ 
++   useEffect(() => {
++     storage?.setItem(STORAGE_KEY, JSON.stringify(entities));
++@@ -186,6 +231,15 @@ export default function Home() {
++     storage?.setItem(APP_NAME_KEY, appName);
++   }, [appName]);
++ 
+++  useEffect(() => {
+++    storage?.setItem(FUNCTIONALITIES_KEY, JSON.stringify(functionalities));
+++  }, [functionalities]);
+++
+++  useEffect(() => {
+++    if (selectedFunctionality) storage?.setItem(SELECTED_FUNCTIONALITY_KEY, selectedFunctionality.name);
+++    else storage?.removeItem(SELECTED_FUNCTIONALITY_KEY);
+++  }, [selectedFunctionality]);
+++
++   const handleUpdateEntity = (updatedEntity) => {
++   setEntities(prev => {
++     const updatedList = prev.map(e =>
++@@ -245,6 +299,33 @@ export default function Home() {
++     setSelectedEntity(prev => ({ ...prev, name: newName }));
++ };
++ 
+++  const handleAddFunctionality = () => {
+++    const newFunctionality = {
+++      name: `NovaFuncionalidade${functionalities.length + 1}`,
+++      input: [],
+++      output: [],
+++      entity: null,
+++      exposeInBackoffice: false
+++    };
+++    setFunctionalities(prev => [...prev, newFunctionality]);
+++    setSelectedFunctionality(newFunctionality);
+++  };
+++
+++  const handleDeleteFunctionality = (functionalityName) => {
+++    setFunctionalities(prev => prev.filter(f => f.name !== functionalityName));
+++    if (selectedFunctionality?.name === functionalityName) setSelectedFunctionality(null);
+++  };
+++
+++  const handleUpdateFunctionality = (updatedFunctionality) => {
+++    setFunctionalities(prev => prev.map(f => f.name === updatedFunctionality.name ? updatedFunctionality : f));
+++    setSelectedFunctionality(updatedFunctionality);
+++  };
+++
+++  const handleRenameFunctionality = (oldName, newName) => {
+++    setFunctionalities(prev => prev.map(f => f.name === oldName ? { ...f, name: newName } : f));
+++    if (selectedFunctionality?.name === oldName) setSelectedFunctionality(prev => ({ ...prev, name: newName }));
+++  };
+++
++ 
++ 
++   const rightPanelWidth =
++@@ -277,6 +358,7 @@ export default function Home() {
++             <span className="px-2 py-1 rounded bg-gray-700">
++               {entities.reduce((acc, e) => acc + e.attributes.length, 0)} atributos
++             </span>
+++            <span className="px-2 py-1 rounded bg-gray-700">{functionalities.length} funcionalidades</span>
++           </div>
++         </div>
++       </header>
++@@ -284,13 +366,46 @@ export default function Home() {
++       <div className="flex-1 flex overflow-hidden">
++         {/* LEFT */}
++         <div className="w-64 border-r border-gray-800 bg-gray-800/30 flex flex-col">
++-          <EntityList
++-            entities={entities}
++-            selectedEntity={selectedEntity}
++-            onSelect={setSelectedEntity}
++-            onAdd={handleAddEntity}
++-            onDelete={handleDeleteEntity}
++-          />
+++          <Tabs value={leftPanelTab} onValueChange={setLeftPanelTab} className="flex-1 flex flex-col">
+++            <div className="border-b border-slate-800 px-4">
+++              <TabsList className="bg-transparent h-12 p-0 gap-1">
+++                <TabsTrigger
+++                  value="entities"
+++                  className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-purple-500"
+++                >
+++                  <Database className="w-4 h-4" />
+++                  Entidades
+++                </TabsTrigger>
+++                <TabsTrigger
+++                  value="functionalities"
+++                  className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-emerald-500"
+++                >
+++                  <Zap className="w-4 h-4" />
+++                  Funcionalidades
+++                </TabsTrigger>
+++              </TabsList>
+++            </div>
+++
+++            <TabsContent value="entities" className="m-0 flex-1">
+++              <EntityList
+++                entities={entities}
+++                selectedEntity={selectedEntity}
+++                onSelect={setSelectedEntity}
+++                onAdd={handleAddEntity}
+++                onDelete={handleDeleteEntity}
+++              />
+++            </TabsContent>
+++
+++            <TabsContent value="functionalities" className="m-0 flex-1">
+++              <FunctionalityList
+++                functionalities={functionalities}
+++                selectedFunctionality={selectedFunctionality}
+++                onSelect={setSelectedFunctionality}
+++                onAdd={handleAddFunctionality}
+++                onDelete={handleDeleteFunctionality}
+++              />
+++            </TabsContent>
+++          </Tabs>
++         </div>
++ 
++         {/* CENTER */}
++@@ -302,11 +417,18 @@ export default function Home() {
++               onUpdate={handleUpdateEntity}
++               onRename={handleRenameEntity}
++             />
+++          ) : selectedFunctionality ? (
+++            <FunctionalityEditor
+++              functionality={selectedFunctionality}
+++              allEntities={entities}
+++              onUpdate={handleUpdateFunctionality}
+++              onRename={handleRenameFunctionality}
+++            />
++           ) : (
++             <div className="h-full flex items-center justify-center text-gray-500">
++               <div className="text-center">
++                 Ôö¼┬í├ú├åÔö£ÔöñÔö¼┬¼
++-                <p>Selecione uma entidade para editar</p>
+++                <p>Selecione uma entidade ou funcionalidade para editar</p>
++                 <p className="text-sm mt-1">ou crie uma nova</p>
++               </div>
++             </div>
++@@ -334,6 +456,7 @@ export default function Home() {
++             {rightPanel === 'json' ? (
++               <JsonPreview
++   entities={entities}
+++  functionalities={functionalities}
++   appName={appName}
++   nodePositions={nodePositions}
++   onLoadProject={(data) => {
++@@ -350,6 +473,8 @@ export default function Home() {
++   setNodePositions(data.nodePositions || []);
++   setEntities(finalEntities);
++   setSelectedEntity(finalEntities[0] || null);
+++  setFunctionalities(data.functionalities || []);
+++  setSelectedFunctionality((data.functionalities || [])[0] || null);
++ }}
++ />
++             ) : (
++diff --git a/front-meta-generator/metafront/src/lib/utils.jsx b/front-meta-generator/metafront/src/lib/utils.jsx
++index ed9e2e4..10044ce 100644
++--- a/front-meta-generator/metafront/src/lib/utils.jsx
+++++ b/front-meta-generator/metafront/src/lib/utils.jsx
++@@ -1,4 +1,7 @@
++-export function cn(...classes) {
++-  return classes.filter(Boolean).join(" ");
+++import { clsx } from "clsx";
+++import { twMerge } from "tailwind-merge";
+++
+++export function cn(...inputs) {
+++  return twMerge(clsx(inputs));
++ }
++ 
++diff --git a/oq fazer.md b/oq fazer.md
++index a0aa6eb..0949fab 100644
++--- a/oq fazer.md	
+++++ b/oq fazer.md	
++@@ -257,4 +257,477 @@ Funcionalidades de neg├ö├Â┬ú├ö├Â├®cio	Ôö£├ÂÔö¼├║Ôö£├í
++ DTOs espec├ö├Â┬úÔö¼├¡ficos por caso de uso	Ôö£├ÂÔö¼├║Ôö£├í
++ Interfaces de service	Ôö£├ÂÔö¼├║Ôö£├í
++ Implementa├ö├Â┬úÔö¼Ôòæ├ö├Â┬úÔö£├╝es separadas	Ôö£├ÂÔö¼├║Ôö£├í
++-Controllers orientados a a├ö├Â┬úÔö¼Ôòæ├ö├Â┬úÔö£Ôòæo	Ôö£├ÂÔö¼├║Ôö£├í
++\ No newline at end of file
+++Controllers orientados a a├ö├Â┬úÔö¼Ôòæ├ö├Â┬úÔö£Ôòæo	Ôö£├ÂÔö¼├║Ôö£├í
+++
+++
+++update 1.0 doq vc fez: diff --git a/TODO.md b/TODO.md
+++index d349795..7403aaa 100644
+++--- a/TODO.md
++++++ b/TODO.md
+++@@ -7,11 +7,12 @@
+++ - [ ] Manter compatibilidade total com entidades sem funcionalidades
+++ 
+++ ## 2. GeraÔö£├ÂÔö£├éÔö¼├║├ö├Â┬╝├ö├▓├ªÔö£├ÂÔö£├éÔö¼├║├ö├Â┬ú├ö├▓├ªo de DTOs de Funcionalidade
+++-- [ ] Criar novo gerador: FunctionalityDtoGenerator
+++-- [ ] Para cada funcionalidade: Gerar DTO de entrada (<FunctionalityName>RequestDto)
+++-- [ ] Para cada funcionalidade: Gerar DTO de saÔö£├ÂÔö£├éÔö¼├║├ö├Â┬╝Ôö£┬ída (<FunctionalityName>ResponseDto)
+++-- [ ] Colocar em: generated/dto/functionality/
+++-- [ ] Regras: NÔö£├ÂÔö£├éÔö¼├║├ö├Â┬úÔö£ÔöéO reutilizar CreateDto ou UpdateDto, campos vÔö£├ÂÔö£├éÔö¼├║├ö├Â┬╝Ôö¼ÔòØm da definiÔö£├ÂÔö£├éÔö¼├║├ö├Â┬╝├ö├▓├ªÔö£├ÂÔö£├éÔö¼├║├ö├Â┬ú├ö├▓├ªo da funcionalidade
++++- [x] Criar novo gerador: FunctionalityDtoGenerator
++++- [x] Para cada funcionalidade: Gerar DTO de entrada (<FunctionalityName>RequestDto)
++++- [x] Para cada funcionalidade: Gerar DTO de saÔö£├ÂÔö£├éÔö¼├║├ö├Â┬╝Ôö£┬ída (<FunctionalityName>ResponseDto)
++++- [x] Colocar em: generated/dto/functionality/
++++- [x] Regras: NÔö£├ÂÔö£├éÔö¼├║├ö├Â┬úÔö£ÔöéO reutilizar CreateDto ou UpdateDto, campos vÔö£├ÂÔö£├éÔö¼├║├ö├Â┬╝Ôö¼ÔòØm da definiÔö£├ÂÔö£├éÔö¼├║├ö├Â┬╝├ö├▓├ªÔö£├ÂÔö£├éÔö¼├║├ö├Â┬ú├ö├▓├ªo da funcionalidade
++++- [x] Corrigido bug no mÔö£├ÂÔö£├éÔö¼├║├ö├Â┬╝Ôö¼┬¢todo generateFunctionalityDTOs (parÔö£├ÂÔö£├éÔö¼├║├ö├Â┬ú├ö├Â├®metro entityName removido)
+++ 
+++ ## 3. GeraÔö£├ÂÔö£├éÔö¼├║├ö├Â┬╝├ö├▓├ªÔö£├ÂÔö£├éÔö¼├║├ö├Â┬ú├ö├▓├ªo de Interfaces de ServiÔö£├ÂÔö£├éÔö¼├║├ö├Â┬╝├ö├▓├ªo (NOVA CAMADA)
+++ - [ ] Criar novo gerador: FunctionalityServiceInterfaceGenerator
+++diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
+++index 09eff91..e1df813 100644
+++--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
++++++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/controller/GenerationController.java
+++@@ -23,14 +23,14 @@ public class GenerationController {
+++     }
+++ 
+++    @PostMapping
+++-public String generate(@RequestBody Map<String, Object> payload) {
+++-    String appName = (String) payload.get("appName");
+++-    @SuppressWarnings("unchecked")
+++-    List<Map<String, Object>> entities = (List<Map<String, Object>>) payload.get("entities");
+++-    @SuppressWarnings("unchecked")
+++-    List<Map<String, Object>> functionalities = (List<Map<String, Object>>) payload.getOrDefault("functionalities", List.of());
+++-    generatorService.generateApplication(appName, entities, functionalities);
+++-    return "CÔö£├ÂÔö£├éÔö¼├║Ôö£├ÂÔö£├éÔö£┬«digo gerado em /" + appName + "/";
+++-}
++++   public String generate(@RequestBody Map<String, Object> payload) {
++++       String appName = (String) payload.get("appName");
++++       @SuppressWarnings("unchecked")
++++       List<Map<String, Object>> entities = (List<Map<String, Object>>) payload.get("entities");
++++       @SuppressWarnings("unchecked")
++++       List<Map<String, Object>> functionalities = (List<Map<String, Object>>) payload.getOrDefault("functionalities", List.of());
++++       generatorService.generateApplication(appName, entities, functionalities);
++++       return "CÔö£├ÂÔö£├éÔö¼├║Ôö£├ÂÔö£├éÔö£┬«digo gerado em /" + appName + "/";
++++   }
+++ }
+++ 
+++diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
+++index 9ebd63c..834e418 100644
+++--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
++++++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityControllerGenerator.java
+++@@ -25,10 +25,11 @@ public class FunctionalityControllerGenerator {
+++                           .append("    private ").append(serviceInterfaceName).append(" ").append(serviceInterfaceName.substring(0, 1).toLowerCase() + serviceInterfaceName.substring(1)).append(";\n\n");
+++ 
+++             String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
++++            String endpoint = "/" + methodName.replaceAll("([A-Z])", "-$1").toLowerCase();
+++             String requestDto = funcName + "RequestDto";
+++             String responseDto = funcName + "ResponseDto";
+++ 
+++-            controllerCode.append("    @PostMapping\n")
++++            controllerCode.append("    @PostMapping(\"").append(endpoint).append("\")\n")
+++                           .append("    public ").append(responseDto).append(" ").append(methodName).append("(@RequestBody ").append(requestDto).append(" request) {\n")
+++                           .append("        return ").append(serviceInterfaceName.substring(0, 1).toLowerCase() + serviceInterfaceName.substring(1)).append(".").append(methodName).append("(request);\n")
+++                           .append("    }\n\n");
+++diff --git a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
+++index a903df7..177d852 100644
+++--- a/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
++++++ b/back-meta-generator/meta_backend/src/main/java/com/example/meta_backend/service/generator/functionality/FunctionalityServiceImplGenerator.java
+++@@ -11,30 +11,16 @@ public class FunctionalityServiceImplGenerator {
+++             String funcName = (String) functionality.get("name");
+++             String interfaceName = funcName + "Service";
+++             String implName = funcName + "ServiceImpl";
+++-            String entityName = (String) functionality.getOrDefault("entity", null);
+++ 
+++             StringBuilder implCode = new StringBuilder();
+++             implCode.append("package com.metagen.backend.generated.service.functionality.impl;\n\n")
+++                     .append("import org.springframework.stereotype.Service;\n")
+++-                    .append("import org.springframework.beans.factory.annotation.Autowired;\n\n");
+++-
+++-            if (entityName != null) {
+++-                String repositoryName = entityName + "Repository";
+++-                implCode.append("import com.metagen.backend.generated.repository.").append(repositoryName).append(";\n")
+++-                        .append("import com.metagen.backend.generated.entity.").append(entityName).append(";\n");
+++-            }
+++-
+++-            implCode.append("import com.metagen.backend.generated.dto.functionality.*;\n")
++++                    .append("import org.springframework.beans.factory.annotation.Autowired;\n\n")
++++                    .append("import com.metagen.backend.generated.dto.functionality.*;\n")
+++                     .append("import com.metagen.backend.generated.service.functionality.").append(interfaceName).append(";\n\n")
+++                     .append("@Service\n")
+++                     .append("public class ").append(implName).append(" implements ").append(interfaceName).append(" {\n\n");
+++ 
+++-            if (entityName != null) {
+++-                String repositoryName = entityName + "Repository";
+++-                implCode.append("    @Autowired\n")
+++-                        .append("    private ").append(repositoryName).append(" ").append(repositoryName.substring(0, 1).toLowerCase() + repositoryName.substring(1)).append(";\n\n");
+++-            }
+++-
+++             String methodName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
+++             String requestDto = funcName + "RequestDto";
+++             String responseDto = funcName + "ResponseDto";
+++diff --git a/front-meta-generator/metafront/package-lock.json b/front-meta-generator/metafront/package-lock.json
+++index 79c73ab..095795f 100644
+++--- a/front-meta-generator/metafront/package-lock.json
++++++ b/front-meta-generator/metafront/package-lock.json
+++@@ -8,9 +8,13 @@
+++       "name": "metafront",
+++       "version": "1.0.0",
+++       "dependencies": {
++++        "@radix-ui/react-label": "^2.1.8",
++++        "class-variance-authority": "^0.7.1",
++++        "clsx": "^2.1.1",
+++         "lucide-react": "^0.553.0",
+++         "react": "^18.3.1",
+++-        "react-dom": "^18.3.1"
++++        "react-dom": "^18.3.1",
++++        "tailwind-merge": "^3.4.0"
+++       },
+++       "devDependencies": {
+++         "@vitejs/plugin-react": "^4.3.0",
+++@@ -876,6 +880,85 @@
+++         "node": ">=14"
+++       }
+++     },
++++    "node_modules/@radix-ui/react-compose-refs": {
++++      "version": "1.1.2",
++++      "resolved": "https://registry.npmjs.org/@radix-ui/react-compose-refs/-/react-compose-refs-1.1.2.tgz",
++++      "integrity": "sha512-z4eqJvfiNnFMHIIvXP3CY57y2WJs5g2v3X0zm9mEJkrkNv4rDxu+sg9Jh8EkXyeqBkB7SOcboo9dMVqhyrACIg==",
++++      "license": "MIT",
++++      "peerDependencies": {
++++        "@types/react": "*",
++++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
++++      },
++++      "peerDependenciesMeta": {
++++        "@types/react": {
++++          "optional": true
++++        }
++++      }
++++    },
++++    "node_modules/@radix-ui/react-label": {
++++      "version": "2.1.8",
++++      "resolved": "https://registry.npmjs.org/@radix-ui/react-label/-/react-label-2.1.8.tgz",
++++      "integrity": "sha512-FmXs37I6hSBVDlO4y764TNz1rLgKwjJMQ0EGte6F3Cb3f4bIuHB/iLa/8I9VKkmOy+gNHq8rql3j686ACVV21A==",
++++      "license": "MIT",
++++      "dependencies": {
++++        "@radix-ui/react-primitive": "2.1.4"
++++      },
++++      "peerDependencies": {
++++        "@types/react": "*",
++++        "@types/react-dom": "*",
++++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc",
++++        "react-dom": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
++++      },
++++      "peerDependenciesMeta": {
++++        "@types/react": {
++++          "optional": true
++++        },
++++        "@types/react-dom": {
++++          "optional": true
++++        }
++++      }
++++    },
++++    "node_modules/@radix-ui/react-primitive": {
++++      "version": "2.1.4",
++++      "resolved": "https://registry.npmjs.org/@radix-ui/react-primitive/-/react-primitive-2.1.4.tgz",
++++      "integrity": "sha512-9hQc4+GNVtJAIEPEqlYqW5RiYdrr8ea5XQ0ZOnD6fgru+83kqT15mq2OCcbe8KnjRZl5vF3ks69AKz3kh1jrhg==",
++++      "license": "MIT",
++++      "dependencies": {
++++        "@radix-ui/react-slot": "1.2.4"
++++      },
++++      "peerDependencies": {
++++        "@types/react": "*",
++++        "@types/react-dom": "*",
++++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc",
++++        "react-dom": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
++++      },
++++      "peerDependenciesMeta": {
++++        "@types/react": {
++++          "optional": true
++++        },
++++        "@types/react-dom": {
++++          "optional": true
++++        }
++++      }
++++    },
++++    "node_modules/@radix-ui/react-slot": {
++++      "version": "1.2.4",
++++      "resolved": "https://registry.npmjs.org/@radix-ui/react-slot/-/react-slot-1.2.4.tgz",
++++      "integrity": "sha512-Jl+bCv8HxKnlTLVrcDE8zTMJ09R9/ukw4qBs/oZClOfoQk/cOTbDn+NceXfV7j09YPVQUryJPHurafcSg6EVKA==",
++++      "license": "MIT",
++++      "dependencies": {
++++        "@radix-ui/react-compose-refs": "1.1.2"
++++      },
++++      "peerDependencies": {
++++        "@types/react": "*",
++++        "react": "^16.8 || ^17.0 || ^18.0 || ^19.0 || ^19.0.0-rc"
++++      },
++++      "peerDependenciesMeta": {
++++        "@types/react": {
++++          "optional": true
++++        }
++++      }
++++    },
+++     "node_modules/@rolldown/pluginutils": {
+++       "version": "1.0.0-beta.27",
+++       "resolved": "https://registry.npmjs.org/@rolldown/pluginutils/-/pluginutils-1.0.0-beta.27.tgz",
+++@@ -1513,6 +1596,27 @@
+++         "node": ">= 6"
+++       }
+++     },
++++    "node_modules/class-variance-authority": {
++++      "version": "0.7.1",
++++      "resolved": "https://registry.npmjs.org/class-variance-authority/-/class-variance-authority-0.7.1.tgz",
++++      "integrity": "sha512-Ka+9Trutv7G8M6WT6SeiRWz792K5qEqIGEGzXKhAE6xOWAY6pPH8U+9IY3oCMv6kqTmLsv7Xh/2w2RigkePMsg==",
++++      "license": "Apache-2.0",
++++      "dependencies": {
++++        "clsx": "^2.1.1"
++++      },
++++      "funding": {
++++        "url": "https://polar.sh/cva"
++++      }
++++    },
++++    "node_modules/clsx": {
++++      "version": "2.1.1",
++++      "resolved": "https://registry.npmjs.org/clsx/-/clsx-2.1.1.tgz",
++++      "integrity": "sha512-eYm0QWBtUrBWZWG0d386OGAw16Z995PiOVo2B7bjWSbHedGl5e0ZWaq65kOGgUSNesEIDkB9ISbTg/JK9dhCZA==",
++++      "license": "MIT",
++++      "engines": {
++++        "node": ">=6"
++++      }
++++    },
+++     "node_modules/color-convert": {
+++       "version": "2.0.1",
+++       "resolved": "https://registry.npmjs.org/color-convert/-/color-convert-2.0.1.tgz",
+++@@ -2820,6 +2924,16 @@
+++         "url": "https://github.com/sponsors/ljharb"
+++       }
+++     },
++++    "node_modules/tailwind-merge": {
++++      "version": "3.4.0",
++++      "resolved": "https://registry.npmjs.org/tailwind-merge/-/tailwind-merge-3.4.0.tgz",
++++      "integrity": "sha512-uSaO4gnW+b3Y2aWoWfFpX62vn2sR3skfhbjsEnaBI81WD1wBLlHZe5sWf0AqjksNdYTbGBEd0UasQMT3SNV15g==",
++++      "license": "MIT",
++++      "funding": {
++++        "type": "github",
++++        "url": "https://github.com/sponsors/dcastil"
++++      }
++++    },
+++     "node_modules/tailwindcss": {
+++       "version": "3.4.18",
+++       "resolved": "https://registry.npmjs.org/tailwindcss/-/tailwindcss-3.4.18.tgz",
+++diff --git a/front-meta-generator/metafront/package.json b/front-meta-generator/metafront/package.json
+++index a24f05f..5dac066 100644
+++--- a/front-meta-generator/metafront/package.json
++++++ b/front-meta-generator/metafront/package.json
+++@@ -9,21 +9,20 @@
+++     "preview": "vite preview"
+++   },
+++   "dependencies": {
++++    "@radix-ui/react-label": "^2.1.8",
++++    "class-variance-authority": "^0.7.1",
++++    "clsx": "^2.1.1",
+++     "lucide-react": "^0.553.0",
+++     "react": "^18.3.1",
+++-    "react-dom": "^18.3.1"
++++    "react-dom": "^18.3.1",
++++    "tailwind-merge": "^3.4.0"
+++   },
+++   "devDependencies": {
+++     "@vitejs/plugin-react": "^4.3.0",
+++     "autoprefixer": "^10.4.22",
+++     "postcss": "^8.5.6",
++++    "sonner": "1.4.0",
+++     "tailwindcss": "^3.4.18",
+++-    "vite": "^7.2.4",
+++-    "sonner": "1.4.0"
+++-
+++-
+++-
++++    "vite": "^7.2.4"
+++   }
+++-  
+++-
+++ }
+++diff --git a/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx b/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
+++index b9ebf1b..a2b15c6 100644
+++--- a/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
++++++ b/front-meta-generator/metafront/src/components/schema-builder/BehaviorsEditor.jsx
+++@@ -1,9 +1,9 @@
+++ import React, { useState } from 'react';
+++ import { Button } from "@/components/ui/button";
+++-import  Input  from "@/components/ui/input";
++++import Input from "@/components/ui/input";
+++ import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
+++-import  Switch  from "@/components/ui/switch";
+++-import  Badge  from "@/components/ui/badge";
++++import Switch from "@/components/ui/switch";
++++import Badge from "@/components/ui/badge";
+++ import { Plus, Trash2, Zap, Clock, Shield, History, GitBranch, X } from 'lucide-react';
+++ import { cn } from "@/lib/utils";
+++ 
+++diff --git a/front-meta-generator/metafront/src/components/ui/input.jsx b/front-meta-generator/metafront/src/components/ui/input.jsx
+++index 5c0ff18..eca6f9d 100644
+++--- a/front-meta-generator/metafront/src/components/ui/input.jsx
++++++ b/front-meta-generator/metafront/src/components/ui/input.jsx
+++@@ -1,8 +1,20 @@
+++-export default function Input({ className = "", ...props }) {
++++import * as React from "react";
++++import { cn } from "@/lib/utils";
++++
++++const Input = React.forwardRef(({ className, type, ...props }, ref) => {
+++   return (
+++     <input
+++-      className={`px-3 py-2 rounded-md border border-slate-700 bg-slate-900 text-slate-100 focus:outline-none focus:ring-2 focus:ring-violet-500 ${className}`}
++++      type={type}
++++      className={cn(
++++        "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
++++        className
++++      )}
++++      ref={ref}
+++       {...props}
+++     />
+++   );
+++-}
++++});
++++Input.displayName = "Input";
++++
++++export { Input };
++++export default Input;
+++diff --git a/front-meta-generator/metafront/src/components/ui/select.jsx b/front-meta-generator/metafront/src/components/ui/select.jsx
+++index ffbd4b1..7ab5d17 100644
+++--- a/front-meta-generator/metafront/src/components/ui/select.jsx
++++++ b/front-meta-generator/metafront/src/components/ui/select.jsx
+++@@ -1,6 +1,12 @@
+++-import { useState, useRef, useEffect } from "react";
++++import { useState, useRef, useEffect, createContext, useContext } from "react";
+++ import { ChevronDown } from "lucide-react";
+++ 
++++// ---------------------------
++++// CONTEXT
++++// ---------------------------
++++const SelectContext = createContext();
++++const useSelect = () => useContext(SelectContext);
++++
+++ export function Select({ children, value, onValueChange }) {
+++   const [open, setOpen] = useState(false);
+++ 
+++@@ -11,14 +17,6 @@ export function Select({ children, value, onValueChange }) {
+++   );
+++ }
+++ 
+++-// ---------------------------
+++-// CONTEXT
+++-// ---------------------------
+++-import { createContext, useContext } from "react";
+++-
+++-const SelectContext = createContext();
+++-const useSelect = () => useContext(SelectContext);
+++-
+++ // ---------------------------
+++ // TRIGGER
+++ // ---------------------------
+++diff --git a/front-meta-generator/metafront/src/components/ui/switch.jsx b/front-meta-generator/metafront/src/components/ui/switch.jsx
+++index f36f9e5..898b337 100644
+++--- a/front-meta-generator/metafront/src/components/ui/switch.jsx
++++++ b/front-meta-generator/metafront/src/components/ui/switch.jsx
+++@@ -1,12 +1,12 @@
+++ import { useState } from "react";
+++ 
+++-export default function Switch({ checked, onChange, className = "" }) {
++++export function Switch({ checked, onCheckedChange, className = "" }) {
+++   const [internalChecked, setInternalChecked] = useState(checked || false);
+++ 
+++   const toggle = () => {
+++     const newValue = !internalChecked;
+++     setInternalChecked(newValue);
+++-    onChange && onChange(newValue);
++++    onCheckedChange && onCheckedChange(newValue);
+++   };
+++ 
+++   return (
+++@@ -30,3 +30,5 @@ export default function Switch({ checked, onChange, className = "" }) {
+++     </button>
+++   );
+++ }
++++
++++export default Switch;
+++diff --git a/front-meta-generator/metafront/src/home.jsx b/front-meta-generator/metafront/src/home.jsx
+++index 443bbba..8d22129 100644
+++--- a/front-meta-generator/metafront/src/home.jsx
++++++ b/front-meta-generator/metafront/src/home.jsx
+++@@ -1,11 +1,17 @@
+++ import React, { useState, useEffect } from 'react';
+++ import EntityList from './components/schema-builder/EntityList';
+++ import EntityEditor from './components/schema-builder/EntityEditor';
++++import FunctionalityList from './components/schema-builder/FunctionalityList';
++++import FunctionalityEditor from './components/schema-builder/FunctionalityEditor';
+++ import JsonPreview from './components/schema-builder/JsonPreview';
+++ import RelationDiagram from './components/schema-builder/RelationDiagram';
++++import { Tabs, TabsContent, TabsList, TabsTrigger } from './components/ui/tabs';
++++import { Database, Zap } from 'lucide-react';
+++ 
+++ const STORAGE_KEY = 'schema_builder_entities_v1';
+++ const SELECTED_ENTITY_KEY = 'schema_builder_selected_entity_v1';
++++const FUNCTIONALITIES_KEY = 'schema_builder_functionalities_v1';
++++const SELECTED_FUNCTIONALITY_KEY = 'schema_builder_selected_functionality_v1';
+++ const RIGHT_PANEL_KEY = 'schema_builder_right_panel_v1';
+++ const APP_NAME_KEY = 'schema_builder_app_name_v1';
+++ const NODE_POSITIONS_KEY = 'schema_builder_node_positions_v1';
+++@@ -69,6 +75,34 @@ const loadNodePositions = () => {
+++   return [];
+++ };
+++ 
++++const loadFunctionalities = () => {
++++  if (!storage) return [];
++++  try {
++++    const raw = storage.getItem(FUNCTIONALITIES_KEY);
++++    if (raw) {
++++      const parsed = JSON.parse(raw);
++++      if (Array.isArray(parsed)) {
++++        return parsed.map(func => ({
++++          name: func.name,
++++          input: (func.input || []).map(field => ({
++++            name: field.name,
++++            type: field.type
++++          })),
++++          output: (func.output || []).map(field => ({
++++            name: field.name,
++++            type: field.type
++++          })),
++++          entity: func.entity,
++++          exposeInBackoffice: func.exposeInBackoffice || false
++++        }));
++++      }
++++    }
++++  } catch (err) {
++++    console.warn("Erro ao ler funcionalidades:", err);
++++  }
++++  return [];
++++};
++++
+++ const SYSTEM_USER_ENTITY = {
+++   name: "User",
+++   fixed: true,
+++@@ -143,6 +177,9 @@ export default function Home() {
+++   const [selectedEntity, setSelectedEntity] = useState(null);
+++   const [rightPanel, setRightPanel] = useState('json');
+++   const [appName, setAppName] = useState('my-app');
++++  const [functionalities, setFunctionalities] = useState(loadFunctionalities);
++++  const [selectedFunctionality, setSelectedFunctionality] = useState(null);
++++  const [leftPanelTab, setLeftPanelTab] = useState('entities');
+++ 
+++   useEffect(() => {
+++     if (storage) {
+++diff --git a/front-meta-generator/metafront/src/lib/utils.jsx b/front-meta-generator/metafront/src/lib/utils.jsx
+++index ed9e2e4..10044ce 100644
+++--- a/front-meta-generator/metafront/src/lib/utils.jsx
++++++ b/front-meta-generator/metafront/src/lib/utils.jsx
+++@@ -1,4 +1,7 @@
+++-export function cn(...classes) {
+++-  return classes.filter(Boolean).join(" ");
++++import { clsx } from "clsx";
++++import { twMerge } from "tailwind-merge";
++++
++++export function cn(...inputs) {
++++  return twMerge(clsx(inputs));
+++ }
+++ 
++
++ 
+
+
+ 



 
