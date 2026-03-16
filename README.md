# MetaProgrammer

Gerador de aplicações Spring Boot completas a partir de um schema JSON. Você define entidades, relacionamentos e funcionalidades — o MetaProgrammer gera o código.

---

## O que ele faz

A partir de um payload JSON, o MetaProgrammer gera um projeto Java pronto para rodar com:

- Entidades JPA com relacionamentos (1:1, 1:N, N:1, N:N)
- Migrations Flyway (SQL) para criação do banco
- DTOs de request/response
- Repositórios Spring Data JPA
- Services e Controllers REST com endpoints configuráveis
- Autenticação via Spring Security (form-based, com usuário admin gerado automaticamente)
- Painel backoffice com Thymeleaf
- Funcionalidades customizadas (interfaces de serviço + DTOs gerados, implementação manual)

---

## Estrutura do Projeto

```
MetaProgrammer/
├── back-meta-generator/        # Gerador backend (Spring Boot)
│   └── meta_backend/
│       └── src/.../service/generator/
│           ├── base/           # Estrutura base, pom.xml, migrations, app principal
│           ├── entity/         # Entities, Repositories, DTOs
│           ├── layer/          # Services, Controllers, Backoffice
│           ├── config/         # application.properties, exception handler, admin init
│           ├── security/       # Spring Security config
│           └── functionality/  # DTOs, interfaces e impls de funcionalidades
│
├── front-meta-generator/       # Schema builder (React + Vite + Tailwind)
│   └── metafront/
│       └── src/components/schema-builder/
│           ├── EntityEditor, AttributesEditor, RelationsEditor
│           ├── BehaviorsEditor, ApiConfigEditor, DtoEditor
│           ├── FunctionalityEditor, InputEditor, OutputEditor
│           ├── RelationDiagram, JsonPreview
│           └── home.jsx        # Tela principal
│
├── payloadSistemaPedidos.json  # Exemplo: sistema de pedidos (7 entidades, 10 funcionalidades)
├── payloadConversionSystem.json
├── json_base.json
└── fluxos-meta-programmer.md   # Documentação dos fluxos de geração
```

---

## Como usar

### 1. Interface visual (recomendado)

Abra o frontend React e monte o schema pelo builder:

```bash
cd front-meta-generator/metafront
npm install
npm run dev
```

Defina entidades, atributos, relacionamentos, comportamentos e funcionalidades. O JSON é gerado em tempo real no painel lateral.

### 2. Enviar para geração

Com o backend rodando, envie o JSON para o endpoint de geração:

```bash
POST http://localhost:8080/generate
Content-Type: application/json

{ ...payload... }
```

O sistema retorna um `.zip` com o projeto gerado pronto para uso.

### 3. Rodar o backend gerador

```bash
cd back-meta-generator/meta_backend
./mvnw spring-boot:run
```

Requer PostgreSQL configurado (ver `application.properties`).

---

## Schema JSON

### Estrutura raiz

```json
{
  "appName": "NomeDoApp",
  "entities": [...],
  "functionalities": [...]
}
```

### Entidade

```json
{
  "name": "Produto",
  "attributes": [
    {
      "name": "nome",
      "type": "string",
      "constraints": ["required", "max_length:100"]
    },
    {
      "name": "status",
      "type": "enum",
      "values": ["ATIVO", "INATIVO"]
    }
  ],
  "relations": [
    {
      "target": "Categoria",
      "type": "N:1",
      "required": true,
      "cascade": "restrict"
    }
  ],
  "behaviors": ["timestamps", "soft_delete"],
  "api": {
    "endpoints": ["crud", "crud_paged", "search", "export_csv"],
    "auth": "required",
    "roles": ["admin", "user"]
  }
}
```

**Tipos suportados:** `string`, `number`, `boolean`, `date`, `datetime`, `enum`, `text`

**Behaviors:** `timestamps` (createdAt/updatedAt), `soft_delete` (deletedAt), `versioning` (@Version)

**Endpoints:** `crud`, `crud_paged`, `search`, `filter_by_date_range`, `bulk_create`, `bulk_delete`, `export_csv`, `import_csv`

### Funcionalidade

```json
{
  "name": "CalcularDesconto",
  "input": [
    { "name": "valorTotal", "type": "number" },
    { "name": "cupom", "type": "string" }
  ],
  "output": [
    { "name": "valorFinal", "type": "number" },
    { "name": "percentualDesconto", "type": "number" }
  ],
  "entity": "Pedido",
  "exposeInBackoffice": false
}
```

Funcionalidades geram automaticamente DTOs de input/output, interface de serviço e controller REST. A **implementação da lógica** fica como stub para preenchimento manual — ou via agente de IA (ver seção abaixo).

---

## O que é gerado

Para cada aplicação definida no JSON, o gerador produz:

```
{appName}/
├── pom.xml
└── src/main/
    ├── java/com/metagen/backend/generated/
    │   ├── entity/           (JPA entities com relacionamentos)
    │   ├── repository/       (JpaRepository interfaces)
    │   ├── dto/              (Request/Response DTOs)
    │   │   └── functionality/
    │   ├── service/          (Business logic + interfaces de funcionalidades)
    │   │   └── functionality/
    │   ├── controller/       (REST controllers)
    │   ├── security/         (SecurityConfig, UserDetailsService)
    │   ├── exception/        (GlobalExceptionHandler)
    │   └── {App}Application.java
    ├── resources/
    │   ├── application.properties  (com senha admin gerada automaticamente)
    │   └── db/migration/V1__Initial_schema.sql
    └── templates/            (Thymeleaf: list, form, backoffice)
```

---

## Status de Implementação

| Feature | Status |
|---|---|
| Estrutura base do projeto | ✅ |
| Entities JPA com relations | ✅ |
| Repositories | ✅ |
| DTOs básicos | ✅ |
| Services e Controllers | ✅ |
| Migrations Flyway | ✅ |
| Spring Security (form-based) | ✅ |
| Admin inicial gerado | ✅ |
| Backoffice Thymeleaf | ✅ |
| Funcionalidades (interface + DTOs) | ✅ |
| Bean Validation nos DTOs | 🔶 |
| Paginação nos controllers | 🔶 |
| JWT auth | ❌ |
| Swagger / OpenAPI | ❌ |
| Testes unitários/integração | ❌ |
| Docker Compose | ❌ |
| Implementação de funcionalidades via IA | 📋 Planejado |

---

## Stack

**Gerador (meta_backend):** Java 17, Spring Boot 3.2.5, PostgreSQL, Flyway, Spring Security, JPA/Hibernate

**Interface de schema (metafront):** React 18, Vite, Tailwind CSS 3, Radix UI

**Aplicações geradas:** Java 17, Spring Boot, PostgreSQL, Flyway, Spring Security, Thymeleaf
