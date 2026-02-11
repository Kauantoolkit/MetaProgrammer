# TODO: Implementar Funcionalidades de Negócio no Meta Backend Generator

## 1. Modelo de Configuração (BASE DO GERADOR)
- [ ] Permitir que cada entidade tenha um bloco "functionalities"
- [ ] Cada funcionalidade deve conter: name, input (campos do DTO de entrada), output (campos do DTO de saída)
- [ ] Garantir que o parser do gerador reconheça esse novo bloco
- [ ] Manter compatibilidade total com entidades sem funcionalidades

## 2. Geração de DTOs de Funcionalidade
- [ ] Criar novo gerador: FunctionalityDtoGenerator
- [ ] Para cada funcionalidade: Gerar DTO de entrada (<FunctionalityName>RequestDto)
- [ ] Para cada funcionalidade: Gerar DTO de saída (<FunctionalityName>ResponseDto)
- [ ] Colocar em: generated/dto/functionality/
- [ ] Regras: NÃO reutilizar CreateDto ou UpdateDto, campos vêm da definição da funcionalidade

## 3. Geração de Interfaces de Serviço (NOVA CAMADA)
- [ ] Criar novo gerador: FunctionalityServiceInterfaceGenerator
- [ ] Para cada entidade com funcionalidades: Criar interface <EntityName>FunctionalityService
- [ ] Um método por funcionalidade: e.g., OrderApprovalResponseDto approveOrder(ApproveOrderRequestDto request);
- [ ] Salvar em: generated/service/functionality/

## 4. Implementação Padrão dos Services
- [ ] Criar novo gerador: FunctionalityServiceImplGenerator
- [ ] Classe: <EntityName>FunctionalityServiceImpl
- [ ] Anotada com @Service, implementa a interface
- [ ] Injeta Repository da entidade e Mapper (se necessário)
- [ ] Métodos com corpo padrão: throw new UnsupportedOperationException("Not implemented yet");
- [ ] Salvar em: generated/service/functionality/impl/

## 5. Controllers de Funcionalidade
- [ ] Atualizar ControllerGenerator para incluir endpoints de funcionalidade
- [ ] Para cada funcionalidade: Criar endpoint POST /api/<entity>/<functionality>
- [ ] Exemplo: @PostMapping("/approve-order") public OrderApprovalResponseDto approveOrder(@RequestBody ApproveOrderRequestDto request)
- [ ] Controller deve injetar a interface, não a implementação
- [ ] Pode ser mesma classe ou separada (preferível separada)

## 6. Ajuste no CodeGeneratorService Principal
- [ ] Atualizar CodeGeneratorService para orquestrar nova geração
- [ ] Para cada entidade: Gerar Entity/Repository/Mapper/DTOs CRUD/Service CRUD (como já faz)
- [ ] NOVO: Se houver funcionalidades: Gerar DTOs de funcionalidade, interface, implementação, endpoints

## 7. Backoffice Orientado a Funcionalidades (Preparação Futura)
- [ ] Permitir flag "exposeInBackoffice": true nas funcionalidades
- [ ] Preparar arquitetura para gerar telas (NÃO implementar agora)

## 8. Testes e Validação
- [ ] Verificar que código gerado compila
- [ ] Testar com entidades com e sem funcionalidades
- [ ] Garantir que CRUD continua funcionando
