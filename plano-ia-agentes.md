# Plano: Implementação de Funcionalidades via Agente de IA

## Problema

Atualmente, o `FunctionalityServiceImplGenerator` gera o seguinte stub para cada funcionalidade definida no JSON:

```java
@Override
public CalcularDescontoResponseDto calcularDesconto(CalcularDescontoRequestDto request) {
    throw new UnsupportedOperationException("Not implemented yet");
}
```

A lógica de negócio precisa ser escrita manualmente. O JSON já contém informações suficientes para que um agente de IA gere uma implementação funcional — ou pelo menos uma implementação com lógica real baseada no contexto.

---

## Objetivo

Substituir o stub por código Java real gerado por um modelo de linguagem (ex: Qwen2.5-Coder), usando como contexto:
- O schema completo da aplicação (entidades, atributos, relações)
- A definição da funcionalidade (nome, inputs, outputs, entidade vinculada)
- O código já gerado (interface, DTOs, entidade relacionada)

---

## Arquitetura

```
GenerationController (POST /generate)
        │
        ▼
CodeGeneratorService
        │
        ├── [geradores existentes...]
        │
        └── FunctionalityAiImplGenerator  ← NOVO
                │
                ├── Monta o contexto (schema + código gerado)
                ├── Chama LlmClient (Ollama / API externa)
                │       └── modelo: qwen2.5-coder:7b (ou maior)
                ├── Extrai o código Java da resposta
                ├── Valida estrutura básica (classe, método, imports)
                └── Escreve o arquivo .java (com fallback para stub se falhar)
```

---

## Etapas de Implementação

### Etapa 1 — Configuração do cliente LLM

Adicionar dependência no `pom.xml` do meta_backend:

```xml
<!-- Para chamada HTTP ao Ollama ou API compatível com OpenAI -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

Criar `LlmClient.java` que abstrai a chamada ao modelo:

```java
@Component
public class LlmClient {
    // Suporta dois modos via application.properties:
    // llm.mode=ollama  → POST http://localhost:11434/api/generate
    // llm.mode=openai  → POST para qualquer API compatível (Groq, Together, etc.)

    public String generate(String systemPrompt, String userPrompt) { ... }
}
```

`application.properties` (novos campos):

```properties
llm.enabled=false          # desligado por padrão, habilitado via flag no payload
llm.mode=ollama
llm.url=http://localhost:11434/api/generate
llm.model=qwen2.5-coder:7b
llm.api-key=               # usado no modo openai
llm.timeout-seconds=60
```

---

### Etapa 2 — Flag de habilitação no payload

Adicionar campo opcional `useAi` no payload de geração:

```json
{
  "appName": "SistemaPedidos",
  "useAi": true,
  "entities": [...],
  "functionalities": [...]
}
```

No `GenerationController`, passar o flag para o `CodeGeneratorService`, que repassa para o `FunctionalityAiImplGenerator`.

Se `useAi` for `false` ou ausente, usa o gerador atual (stub).

---

### Etapa 3 — Construção do contexto (prompt)

O contexto enviado ao modelo deve ser rico o suficiente para gerar código coerente com o projeto. Estrutura do prompt:

**System prompt:**
```
Você é um desenvolvedor Java especialista em Spring Boot.
Gere APENAS o corpo do método Java solicitado — sem explicações, sem markdown,
sem blocos de código. Retorne somente o código Java puro.
O código deve ser compilável, usar os DTOs e repositórios já definidos,
e implementar a lógica de negócio descrita.
```

**User prompt (montado em `ContextBuilder.java`):**

```
=== CONTEXTO DO PROJETO ===
App: SistemaPedidos

=== ENTIDADES DISPONÍVEIS ===
- Pedido: id, valorTotal (number), status (enum: PENDENTE/PAGO/CANCELADO), createdAt
  Relações: N:1 com Cliente; 1:N com ItemPedido
- Cliente: id, nome (string), email (string, unique)
- ItemPedido: id, quantidade (number), precoUnitario (number)
  Relações: N:1 com Pedido; N:1 com Produto
- Produto: id, nome (string), preco (number), estoque (number)

=== REPOSITÓRIOS DISPONÍVEIS (injetáveis via @Autowired) ===
- PedidoRepository extends JpaRepository<Pedido, Long>
- ClienteRepository extends JpaRepository<Cliente, Long>
- ItemPedidoRepository extends JpaRepository<ItemPedido, Long>
- ProdutoRepository extends JpaRepository<Produto, Long>

=== INTERFACE A IMPLEMENTAR ===
public interface CalcularDescontoService {
    CalcularDescontoResponseDto calcularDesconto(CalcularDescontoRequestDto request);
}

=== REQUEST DTO ===
public class CalcularDescontoRequestDto {
    private Double valorTotal;
    private String cupom;
}

=== RESPONSE DTO ===
public class CalcularDescontoResponseDto {
    private Double valorFinal;
    private Double percentualDesconto;
}

=== ENTIDADE VINCULADA ===
Pedido (ver definição acima)

=== TAREFA ===
Implemente o método calcularDesconto(CalcularDescontoRequestDto request)
da classe CalcularDescontoServiceImpl.
Escreva APENAS o corpo do método (entre as chaves), sem a assinatura.
```

---

### Etapa 4 — `FunctionalityAiImplGenerator.java`

Substitui/estende `FunctionalityServiceImplGenerator`. Fluxo por funcionalidade:

```
1. Monta o contexto com ContextBuilder
2. Chama LlmClient.generate(systemPrompt, userPrompt)
3. Extrai o bloco de código da resposta (regex ou delimitadores)
4. Valida:
   - Contém `return` ou ao menos uma instrução
   - Não contém `throw new UnsupportedOperationException`
   - Não tem markdown/blocos de código sobrando
5. Injeta o corpo no template do método
6. Se validação falhar: usa stub + loga aviso
7. Escreve o arquivo .java
```

---

### Etapa 5 — Validação e fallback

```java
private String extractAndValidate(String llmResponse, String funcName) {
    String body = extractCodeBlock(llmResponse); // remove ```java ... ``` se houver
    if (body.isBlank() || body.contains("UnsupportedOperationException")) {
        log.warn("AI falhou para {}, usando stub", funcName);
        return "throw new UnsupportedOperationException(\"Not implemented yet\");";
    }
    return body;
}
```

---

### Etapa 6 — Endpoint alternativo (opcional, futuro)

Expor um endpoint separado para re-gerar apenas as implementações de funcionalidades de um projeto já gerado:

```
POST /generate/functionalities
{
  "appName": "SistemaPedidos",
  "useAi": true,
  "outputDir": "/path/to/existing/project",
  "functionalities": [...]
}
```

Útil para não re-gerar todo o projeto ao iterar nas implementações.

---

## Modelos Recomendados

| Modelo | Forma de usar | Qualidade código Java | Custo |
|---|---|---|---|
| `qwen2.5-coder:7b` | Ollama local | Boa | Grátis (local) |
| `qwen2.5-coder:14b` | Ollama local | Muito boa | Grátis (local) |
| `qwen/qwen-2.5-coder-32b-instruct` | Together AI / Groq | Excelente | API paga |
| `deepseek-coder-v2` | Ollama / API | Excelente | Varia |
| `claude-sonnet-4-6` | API Anthropic | Excelente | API paga |

Para desenvolvimento local, recomendado: **Qwen2.5-Coder 7B via Ollama**.

Instalação:
```bash
ollama pull qwen2.5-coder:7b
ollama serve
```

---

## Arquivos a criar/modificar

| Arquivo | Ação |
|---|---|
| `LlmClient.java` | Criar — abstrai chamada HTTP ao modelo |
| `ContextBuilder.java` | Criar — monta o prompt com todo o contexto |
| `FunctionalityAiImplGenerator.java` | Criar — orquestra geração com IA |
| `FunctionalityServiceImplGenerator.java` | Modificar — delegar para FunctionalityAiImplGenerator quando `useAi=true` |
| `CodeGeneratorService.java` | Modificar — passar flag `useAi` para os geradores |
| `application.properties` | Modificar — adicionar configs `llm.*` |
| `pom.xml` | Modificar — adicionar WebFlux ou RestClient |

---

## Resultado esperado

Com `useAi: true` no payload, ao invés de:

```java
public CalcularDescontoResponseDto calcularDesconto(CalcularDescontoRequestDto request) {
    throw new UnsupportedOperationException("Not implemented yet");
}
```

O gerador produzirá algo como:

```java
public CalcularDescontoResponseDto calcularDesconto(CalcularDescontoRequestDto request) {
    double desconto = 0.0;
    if ("PROMO10".equalsIgnoreCase(request.getCupom())) {
        desconto = 0.10;
    } else if ("PROMO20".equalsIgnoreCase(request.getCupom())) {
        desconto = 0.20;
    }
    double valorFinal = request.getValorTotal() * (1 - desconto);
    CalcularDescontoResponseDto response = new CalcularDescontoResponseDto();
    response.setValorFinal(valorFinal);
    response.setPercentualDesconto(desconto * 100);
    return response;
}
```

O código não será perfeito para todos os casos, mas será compilável e funcionará como ponto de partida real ao invés de um stub.
