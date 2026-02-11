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