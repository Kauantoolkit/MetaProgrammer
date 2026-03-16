# Fluxos do Sistema MetaProgrammer

## Visão Geral
O MetaProgrammer é um sistema de metaprogramação que gera automaticamente aplicações web completas (backend Spring Boot + frontend Thymeleaf) a partir de uma definição em JSON.

---

## FLUXO 1: GERAÇÃO DE PROJETO (Main Flow)

### 1.1 Entry Point
- **GenerationController** (`/generate` - POST)
  - Recebe payload JSON com: `appName`, `entities`, `functionalities`
  - Chama `CodeGeneratorService.generateApplication()`

### 1.2 Estrutura Base do Projeto
| Componente | Descrição |
|------------|-----------|
| BaseStructureGenerator | Cria diretórios base do projeto |
| PomGenerator | Gera pom.xml com dependências |
| MainClassGenerator | Gera classe principal Spring Boot |
| GitignoreGenerator | Gera .gitignore |
| ApplicationPropertiesGenerator | Gera application.properties |

---

## FLUXO 2: SEGURANÇA E AUTENTICAÇÃO

### 2.1 Sistema de Usuários
| Componente | Descrição |
|------------|-----------|
| EntityGenerator (Users) | Gera entidade Users com username, password, roles |
| RepositoryGenerator (Users) | Gera UsersRepository |

### 2.2 Configurações de Segurança
| Componente | Descrição | Status |
|------------|-----------|--------|
| SecurityGenerator | Gera SecurityConfig.java com autenticação via formulários | ✅ Implementado |
| SecurityGenerator (JWT) | Gera configuração JWT para API REST/mobile | ❌ Pendente |
| @PreAuthorize | Gera anotações de role por endpoint | ❌ Pendente |

### 2.3 Inicialização Admin
| Componente | Descrição |
|------------|-----------|
| AdminUserInitializerGenerator | Gera classe para criar usuário admin automaticamente no startup |

### 2.4 Tratamento de Exceções
| Componente | Descrição |
|------------|-----------|
| GlobalExceptionHandlerGenerator | Gera GlobalExceptionHandler para erros |

---

## FLUXO 3: DOMÍNIO - ENTIDADES

### 3.1 Geração de Entidades
| Componente | Descrição |
|------------|-----------|
| EntityGenerator | Gera classes Entity JPA com: |
| | - Anotações @Entity, @Table |
| | - Campos base (id, timestamps, soft delete, versioning) |
| | - Relacionamentos (1:1, 1:N, N:1, N:N) |
| | - Suporte a behaviors: timestamps, soft_delete, versioning |

### 3.2 Behaviors de Entidade
```
- timestamps: createdAt, updatedAt (LocalDateTime)
- soft_delete: deleted_at com @SQLDelete + @SQLRestriction
- versioning: @Version para controle de concorrência
```

### 3.3 Tipos de Relacionamento Suportados
| Notação | JPA Equivalent | Descrição |
|---------|---------------|------------|
| 1:1 | @OneToOne | Um para Um |
| 1:N | @OneToMany | Um para Muitos |
| N:1 | @ManyToOne | Muitos para Um |
| N:N | @ManyToMany | Muitos para Muitos |

---

## FLUXO 4: CAMADA DE DADOS (Repository)

| Componente | Descrição |
|------------|-----------|
| RepositoryGenerator | Gera interfaces Repository JPA para cada entidade |
| | Estende JpaRepository<Entity, Long> |
| | Suporte a Pageable para paginação |

---

## FLUXO 5: CAMADA DTO

### 5.1 Geração de DTOs
| Componente | Descrição | Status |
|------------|-----------|--------|
| DtoGenerator | Gera Request e Response DTOs | ✅ Básico |
| DtoGenerator (Validation) | Gera DTOs com Bean Validation (@NotNull, @Size, @Email) | ❌ Pendente |

### 5.2 Validações Planejadas
```
java
// Exemplo de DTO com validações que serão geradas
public class ProductRequestDto {
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    private BigDecimal price;

    @Email(message = "Email deve ser válido")
    private String email;

    @NotNull(message = "Categoria é obrigatória")
    private Long categoryId;  // Para relacionamentos N:1
}
```

---

## FLUXO 6: CAMADA DE SERVIÇO (Service)

| Componente | Descrição | Status |
|------------|-----------|--------|
| ServiceGenerator | Gera classes de serviço com métodos condicionais | ✅ Básico |
| ServiceGenerator (Relations) | Service faz findById para relacionamentos antes de salvar | ❌ Pendente |

### 6.1 Métodos Disponíveis por Endpoint
| Endpoint | Métodos Gerados |
|----------|-----------------|
| crud | findAll(), findById(), save(), delete() |
| crud_paged | findAll(Pageable), findById() |
| search | search(query) |
| filter_by_date_range | filterByDateRange(startDate, endDate) |
| bulk_create | saveAll(List) |
| bulk_delete | deleteAll(List<Long>) |
| export_csv | exportCsv() |
| import_csv | importCsv(MultipartFile) |

---

## FLUXO 7: CAMADA DE CONTROLLER (API REST)

### 7.1 Geração de Controllers
| Componente | Descrição | Status |
|------------|-----------|--------|
| ControllerGenerator | Gera controladores REST com endpoints condicionais | ✅ Básico |
| ControllerGenerator (Pagination) | Endpoints com Page<T> e Pageable | ❌ Pendente |

### 7.2 Endpoints por Configuração
| Endpoint | Métodos HTTP | Path |
|----------|--------------|------|
| crud | GET, POST, DELETE | /api/{entity}, /api/{entity}/{id} |
| crud_paged | GET | /api/{entity}?page=0&size=10&sort=field,asc |
| search | GET | /api/{entity}/search?query= |
| filter_by_date_range | GET | /api/{entity}/filter?startDate=&endDate= |
| bulk_create | POST | /api/{entity}/bulk |
| bulk_delete | DELETE | /api/{entity}/bulk |
| export_csv | GET | /api/{entity}/export |
| import_csv | POST | /api/{entity}/import |

---

## FLUXO 8: FUNCIONALIDADES CUSTOMIZADAS

### 8.1 Estrutura de Funcionalidade
Cada funcionalidade no JSON tem:
- `name`: Nome da funcionalidade
- `input`: Lista de campos de entrada
- `output`: Lista de campos de saída

### 8.2 Fluxo de Geração de Funcionalidades

```
FunctionalityDtoGenerator
    ├── gera {Funcionalidade}RequestDto.java
    └── gera {Funcionalidade}ResponseDto.java

FunctionalityServiceInterfaceGenerator
    └── gera {Funcionalidade}Service.java (interface)

FunctionalityServiceImplGenerator
    └── gera {Funcionalidade}ServiceImpl.java (implementação com throw UnsupportedOperationException)

FunctionalityControllerGenerator
    └── gera {Funcionalidade}Controller.java
        └── endpoint POST /api/{funcionalidade}
```

### 8.3 Exemplo de Funcionalidade
```
json
{
  "name": "CalculateDiscount",
  "input": [
    {"name": "productId", "type": "long"},
    {"name": "percentage", "type": "double"}
  ],
  "output": [
    {"name": "finalPrice", "type": "double"},
    {"name": "discountAmount", "type": "double"}
  ]
}
```

Gera:
- CalculateDiscountRequestDto
- CalculateDiscountResponseDto
- CalculateDiscountService (interface)
- CalculateDiscountServiceImpl
- CalculateDiscountController → POST /api/calculate-discount

---

## FLUXO 9: MIGRAÇÕES (Flyway)

| Componente | Descrição |
|------------|-----------|
| FlywayMigrationGenerator | Gera scripts SQL de migração |
| | Cria tabelas base: users, + entidades definidas |
| | Localização: db/migration/ |

---

## FLUXO 10: FRONTEND (Thymeleaf)

### 10.1 Templates Gerados
| Componente | Descrição | Status |
|------------|-----------|--------|
| ThymeleafFrontGenerator | Templates básicos (list, form, dialog) | ✅ Básico |
| ThymeleafFrontGenerator (Pagination) | Paginação de resultados | ❌ Pendente |
| ThymeleafFrontGenerator (Sorting) | Ordenação de colunas | ❌ Pendente |
| ThymeleafFrontGenerator (Masks) | Máscaras em formulários | ❌ Pendente |
| ThymeleafFrontGenerator (Feedback) | Alertas de sucesso/erro | ❌ Pendente |
| ThymeleafFrontGenerator (Search) | Busca inline | ❌ Pendente |

### 10.2 Funcionalidades Planejadas para Frontend
```
- 📄 Paginação: Botões Anterior/Próximo com Page<T>
- 🔄 Ordenação: Clique no cabeçalho da tabela
- 🎭 Máscaras: Input masks para CPF, CNPJ, telefone, data
- ✅ Feedback: Toast/SweetAlert para operações
- 🔍 Busca: Campo de busca inline com debounce
- 📱 Selects: Dropdowns populados para N:1
```

---

## FLUXO 11: BACKOFFICE

| Componente | Descrição |
|------------|-----------|
| BackofficeControllerGenerator | Gera controlador para área administrativa |
| | - Endpoints para gerenciar todas as entidades |
| | - Acesso autenticado via Spring Security |

---

## FLUXO 12: DOCUMENTAÇÃO (Swagger/SpringDoc)

| Componente | Descrição | Status |
|------------|-----------|--------|
| SwaggerGenerator | Gera configuração SpringDoc/OpenAPI | ❌ Pendente |

### 12.1 Configuração Planejada
```
yaml
# Dependência no pom.xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

# Configuração gerada
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---

## FLUXO 13: TESTES

| Componente | Descrição | Status |
|------------|-----------|--------|
| TestGenerator (Unit) | Gera testes unitários para Service | ❌ Pendente |
| TestGenerator (Integration) | Gera testes de integração para Controller | ❌ Pendente |

### 13.1 Testes Planejados
```
src/test/java/com/metagen/backend/generated/
├── service/
│   └── {Entity}ServiceTest.java        # Testes unitários
└── controller/
    └── {Entity}ControllerIT.java       # Testes de integração
```

---

## FLUXO 14: INFRAESTRUTURA

### 14.1 Docker Compose
| Componente | Descrição | Status |
|------------|-----------|--------|
| DockerComposeGenerator | Gera docker-compose.yml com banco | ❌ Pendente |

### 14.2 Application Profiles
| Componente | Descrição | Status |
|------------|-----------|--------|
| ProfileGenerator | Gera application-dev.properties | ❌ Pendente |
| ProfileGenerator | Gera application-prod.properties | ❌ Pendente |
| ProfileGenerator | Gera application-test.properties | ❌ Pendente |

### 14.3 Configurações Planejadas
```
yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    depends_on:
      - db
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: appdb
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
```

```
properties
# application-dev.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/appdb
spring.datasource.username=user
spring.datasource.password=password

# application-prod.properties  
spring.datasource.url=jdbc:postgresql://db:5432/appdb
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

---

## FLUXO 15: INFRAESTRUTURA COMPLETA

### Arquitetura Gerada
```
projeto-gerado/
├── pom.xml
├── docker-compose.yml                  # [NOVO] Container orchestration
├── src/main/
│   ├── java/com/metagen/backend/generated/
│   │   ├── entity/
│   │   │   ├── Users.java
│   │   │   └── {Entidade}.java
│   │   ├── repository/
│   │   │   ├── UsersRepository.java
│   │   │   └── {Entidade}Repository.java
│   │   ├── dto/
│   │   │   ├── {Entidade}RequestDto.java      # [ATUALIZADO] Com validações
│   │   │   ├── {Entidade}ResponseDto.java
│   │   │   └── functionality/
│   │   │       ├── {Funcionalidade}RequestDto.java
│   │   │       └── {Funcionalidade}ResponseDto.java
│   │   ├── service/
│   │   │   ├── {Entidade}Service.java          # [ATUALIZADO] Com findById para relations
│   │   │   └── functionality/
│   │   │       ├── {Funcionalidade}Service.java
│   │   │       └── impl/
│   │   │           └── {Funcionalidade}ServiceImpl.java
│   │   ├── controller/
│   │   │   ├── {Entidade}Controller.java        # [ATUALIZADO] Com Page<T>
│   │   │   ├── {Funcionalidade}Controller.java
│   │   │   └── BackofficeController.java
│   │   ├── security/
│   │   │   ├── SecurityConfig.java              # [ATUALIZADO] JWT option
│   │   │   └── JwtAuthenticationFilter.java     # [NOVO]
│   │   ├── config/
│   │   │   ├── AdminUserInitializer.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── OpenApiConfig.java               # [NOVO]
│   │   └── {AppName}Application.java
│   ├── resources/
│   │   ├── application.properties
│   │   ├── application-dev.properties          # [NOVO]
│   │   ├── application-prod.properties         # [NOVO]
│   │   └── templates/
│   │       └── (arquivos Thymeleaf)            # [ATUALIZADO] Com paginação
├── src/test/
│   ├── java/.../service/
│   │   └── {Entity}ServiceTest.java            # [NOVO]
│   └── java/.../controller/
│       └── {Entity}ControllerIT.java           # [NOVO]
└── db/migration/
    └── V1__initial_schema.sql
```

---

## RESUMO: FLUXOS DO SISTEMA

| # | Fluxo | Componentes Principais | Status |
|---|-------|------------------------|--------|
| 1 | Geração de Projeto | BaseStructureGenerator, PomGenerator, MainClassGenerator | ✅ |
| 2 | Segurança | SecurityGenerator, EntityGenerator(Users), AdminUserInitializer | 🔶 Parcial |
| 3 | Entidades | EntityGenerator (Entity, Relations, Behaviors) | ✅ |
| 4 | Repository | RepositoryGenerator | ✅ |
| 5 | DTOs | DtoGenerator | 🔶 Parcial |
| 6 | Service | ServiceGenerator | 🔶 Parcial |
| 7 | Controller API | ControllerGenerator | 🔶 Parcial |
| 8 | Funcionalidades | FunctionalityDtoGenerator, FunctionalityServiceInterfaceGenerator, FunctionalityServiceImplGenerator, FunctionalityControllerGenerator | ✅ |
| 9 | Migrações | FlywayMigrationGenerator | ✅ |
| 10 | Frontend | ThymeleafFrontGenerator | 🔶 Parcial |
| 11 | Backoffice | BackofficeControllerGenerator | ✅ |
| 12 | Infraestrutura | GlobalExceptionHandlerGenerator, ApplicationPropertiesGenerator | 🔶 Parcial |
| 13 | Documentação | SwaggerGenerator | ❌ Pendente |
| 14 | Testes | TestGenerator (Unit/Integration) | ❌ Pendente |
| 15 | Docker | DockerComposeGenerator | ❌ Pendente |

**Legenda:** ✅ Completo | 🔶 Parcial | ❌ Pendente

---

## CONFIGURAÇÃO DE ENDPOINTS POR ENTIDADE

No JSON de entrada, cada entidade pode ter:
```
json
{
  "name": "Product",
  "api": {
    "endpoints": ["crud", "search", "bulk_create", "import_csv", "export_csv"]
  }
}
```

### Endpoints Disponíveis
| Endpoint | Descrição |
|----------|-----------|
| `crud` | Create, Read, Update, Delete (básico) |
| `crud_paged` | CRUD com paginação (Page<T>) |
| `search` | Busca por query |
| `filter_by_date_range` | Filtragem por período |
| `bulk_create` | Criação em massa |
| `bulk_delete` | Exclusão em massa |
| `export_csv` | Exportação CSV |
| `import_csv` | Importação CSV |

---

## CONFIGURAÇÃO DE SEGURANÇA

No JSON de entrada, é possível configurar o tipo de autenticação:
```
json
{
  "appName": "MyApp",
  "security": {
    "type": "form",        // ou "jwt"
    "roles": ["ADMIN", "USER"]
  }
}
```

### Tipos de Segurança
| Tipo | Descrição |
|------|-----------|
| `form` | Autenticação via formulário (atual) |
| `jwt` | Autenticação JWT para API REST/mobile (pendente) |

---

## ROADMAP DE IMPLEMENTAÇÃO

### Fase 1: Validações e Qualidade (Prioridade Alta)
- [ ] Adicionar validações Bean Validation nos DTOs (@NotNull, @Size, @Email, etc)
- [ ] Gerar testes unitários para Service
- [ ] Gerar testes de integração para Controllers

### Fase 2: Frontend Rico (Prioridade Alta)
- [ ] Adicionar paginação nos templates Thymeleaf
- [ ] Adicionar ordenação de colunas
- [ ] Adicionar máscaras nos formulários
- [ ] Adicionar feedback visual (toast/alerts)
- [ ] Adicionar busca inline

### Fase 3: Relacionamentos (Prioridade Alta)
- [ ] Service fazer findById antes de salvar entidades relacionadas
- [ ] Popular selects com dados da API

### Fase 4: Segurança Avançada (Prioridade Média)
- [ ] Adicionar suporte a autenticação JWT
- [ ] Adicionar @PreAuthorize por endpoint

### Fase 5: Documentação (Prioridade Média)
- [ ] Gerar configuração SpringDoc/OpenAPI

### Fase 6: Infraestrutura (Prioridade Média)
- [ ] Gerar docker-compose.yml
- [ ] Gerar application-{dev,prod,test}.properties
- [ ] Adicionar paginação nos endpoints findAll
