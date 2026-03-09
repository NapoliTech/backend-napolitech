# IA de Upsell Personalizado — Explicação Técnica Completa

> Projeto: Napolitech Backend
> Branch: `feat/ai-upsell-ollama`
> Tecnologias envolvidas: Ollama, Llama 3.2, Spring AI, Spring Boot 3.4, MySQL

---

## 1. Contextualização

O sistema implementa um mecanismo de **upsell inteligente e personalizado** na tela de checkout do aplicativo React Native da pizzaria Napolitech. Upsell é a prática de sugerir produtos complementares ao cliente no momento em que ele está finalizando um pedido, aumentando o ticket médio da venda.

A diferença deste sistema em relação a abordagens tradicionais (como "quem comprou X também comprou Y") é o uso de um **modelo de linguagem grande (LLM)** capaz de raciocinar sobre o contexto de cada cliente de forma individualizada, gerando sugestões com justificativas personalizadas em linguagem natural.

---

## 2. O que é Ollama

**Ollama** é uma plataforma open-source que permite executar modelos de linguagem grandes (LLMs) localmente, sem depender de APIs pagas externas como OpenAI ou Anthropic. Ele funciona como um servidor HTTP que expõe os modelos de IA via uma API REST compatível com o padrão OpenAI.

Características relevantes:
- **100% gratuito e local** — os dados não saem do servidor
- Compatível com dezenas de modelos open-source (Llama, Mistral, Gemma, etc.)
- Gerencia automaticamente download, quantização e execução dos modelos
- Funciona via Docker, facilitando a portabilidade

No projeto, o Ollama roda como um container Docker na porta `11434` e é acessado exclusivamente pelo container do backend via rede interna Docker.

---

## 3. O que é Llama 3.2 (o modelo utilizado)

**Llama 3.2** é um modelo de linguagem desenvolvido pela Meta (empresa do Facebook), lançado em setembro de 2024. É um modelo open-source disponível gratuitamente para uso comercial e acadêmico.

| Característica | Valor |
|---|---|
| Desenvolvedor | Meta AI |
| Versão utilizada | Llama 3.2 (3B parâmetros) |
| Tamanho em disco | ~2GB (quantizado em 4-bit) |
| Contexto máximo | 128.000 tokens |
| Licença | Meta Llama 3.2 Community License (uso livre) |
| Modalidade | Texto → Texto |

**O que são os 3 bilhões de parâmetros?**
Parâmetros são os valores numéricos ajustados durante o treinamento do modelo. Quanto mais parâmetros, maior a capacidade de compreensão e geração de texto. O Llama 3.2 3B é considerado um modelo leve — adequado para rodar em CPU — mas com boa capacidade para tarefas de raciocínio simples como a geração de sugestões comerciais.

**Opção alternativa:** `llama3.2:1b` (1 bilhão de parâmetros, ~800MB) para máquinas com recursos mais limitados.

---

## 4. Arquitetura de Integração

```
[React Native App]
       |
       | POST /api/upsell/{clienteId}
       | body: { "produtosIds": [1, 3] }
       |
[Spring Boot Backend]
       |
       |-- 1. Consulta MySQL --> produtos disponíveis no cardápio
       |-- 2. Consulta MySQL --> histórico dos últimos 5 pedidos do cliente
       |-- 3. Consulta MySQL --> combinações populares (SQL nativo)
       |-- 4. Monta o prompt com todos esses dados
       |
       | HTTP POST http://ollama:11434/api/chat
       |
[Ollama Container] --> [Modelo Llama 3.2]
       |
       | resposta em JSON
       |
[Spring Boot Backend]
       |-- 5. Parseia e valida os IDs sugeridos contra o banco
       |-- 6. Retorna até 3 sugestões com motivo personalizado
       |
[React Native App]
       |-- Exibe cards de upsell no checkout
```

A integração entre o backend Spring Boot e o Ollama é feita via **Spring AI**, uma abstração do ecossistema Spring que padroniza a comunicação com diferentes provedores de LLM (Ollama, OpenAI, Anthropic, etc.) sem necessidade de trocar o código da aplicação ao mudar de provedor.

---

## 5. O Padrão RAG (Retrieval-Augmented Generation)

O sistema utiliza uma variação simplificada do padrão **RAG** (Geração Aumentada por Recuperação). Esse padrão consiste em **enriquecer o prompt enviado ao LLM com dados reais recuperados do banco de dados**, em vez de depender apenas do conhecimento que o modelo adquiriu no treinamento.

Isso resolve um problema fundamental: o modelo Llama 3.2 não tem nenhum conhecimento sobre a pizzaria Napolitech, seus produtos, seus clientes ou seus pedidos. Ao injetar essas informações no prompt, o modelo passa a raciocinar com dados reais e atualizados.

**Dados injetados no prompt a cada requisição:**

| Dado | Fonte | Como é injetado |
|---|---|---|
| Carrinho atual | Enviado pelo app via request body | Lista de nomes e preços |
| Histórico do cliente | `pedido` + `item_pedido` no MySQL | Nomes dos últimos 10 itens distintos pedidos |
| Combinações populares | Query SQL nativa de co-ocorrência | Nomes dos produtos mais pedidos junto com o carrinho |
| Cardápio disponível | `estoque_produtos` no MySQL | Lista com ID, nome, preço e categoria de cada item |

---

## 6. A Query de Combinações Populares

Um dos pilares do sistema é a consulta SQL que identifica quais produtos são mais frequentemente pedidos juntos com os itens do carrinho atual. Essa query utiliza um **self-join** na tabela `item_pedido`:

```sql
SELECT ip2.produto_id
FROM item_pedido ip1
JOIN item_pedido ip2
  ON ip1.pedido_id = ip2.pedido_id
  AND ip1.produto_id != ip2.produto_id
JOIN estoque_produtos p
  ON ip2.produto_id = p.id
WHERE ip1.produto_id IN (:produtoIds)
  AND p.quantidade_estoque > 0
GROUP BY ip2.produto_id
ORDER BY COUNT(*) DESC
LIMIT 5
```

**Como funciona:**
- `ip1` representa um item que já está no carrinho do cliente
- `ip2` representa qualquer outro item que apareceu **no mesmo pedido** que `ip1`
- O `GROUP BY + COUNT + ORDER BY` classifica os itens co-ocorrentes pela frequência
- Retorna os 5 produtos mais pedidos juntos com os itens do carrinho, com estoque disponível

Essa abordagem é chamada de **filtragem colaborativa baseada em itens** — o mesmo conceito usado por plataformas como Netflix e Amazon em sistemas de recomendação.

---

## 7. A Engenharia de Prompt

O prompt enviado ao modelo é construído dinamicamente a cada requisição com os dados do banco. Sua estrutura define com precisão o papel do modelo, os dados disponíveis e o formato esperado da resposta:

```
Você é um assistente de vendas de uma pizzaria chamada Napolitech.
Analise os dados abaixo e sugira até 3 itens de upsell personalizados
para exibir no checkout.

CLIENTE: [nome do cliente]

CARRINHO ATUAL:
[lista de produtos no carrinho com preços]

HISTÓRICO DE PEDIDOS DO CLIENTE (itens já pedidos antes):
[itens distintos dos últimos 5 pedidos]

COMBINAÇÕES POPULARES COM OS ITENS DO CARRINHO:
[resultado da query de co-ocorrência]

PRODUTOS DISPONÍVEIS PARA SUGERIR (use APENAS estes IDs):
[id:1] Pizza de Calabresa - R$39,90 (PIZZA)
[id:2] Coca-Cola Lata 350ml - R$5,90 (BEBIDAS)
...

REGRAS:
- Sugira no máximo 3 produtos usando SOMENTE os IDs da lista acima
- Priorize combinações populares
- Para carrinho com pizzas, prefira bebidas e sobremesas como complemento
- O motivo deve ser curto, pessoal e convincente (máximo 60 caracteres)
- Não repita produtos que já estão no carrinho

Retorne APENAS um array JSON válido, sem texto adicional:
[{"id": 123, "motivo": "motivo personalizado aqui"}]
```

**Técnicas de engenharia de prompt aplicadas:**

| Técnica | Aplicação |
|---|---|
| **Role prompting** | "Você é um assistente de vendas de uma pizzaria" — define o papel e o contexto |
| **Few-shot implícito** | O formato de saída esperado (`[{"id": ..., "motivo": ...}]`) é especificado claramente |
| **Restrição de domínio** | "use APENAS os IDs da lista acima" — impede o modelo de inventar produtos inexistentes |
| **Injeção de contexto** | Dados reais do banco são injetados no prompt a cada chamada |
| **Formato de saída estruturado** | O modelo é instruído a retornar JSON puro, facilitando o parse programático |

---

## 8. Validação da Resposta da IA

Após receber a resposta do modelo, o backend executa uma etapa de **validação e sanitização**:

1. **Extração do JSON:** A função `extractJson()` busca o primeiro `[` e o último `]` na resposta, isolando o array mesmo que o modelo tenha retornado texto antes ou depois (o que pode ocorrer com modelos menores).

2. **Validação dos IDs:** Cada ID sugerido pelo modelo é verificado contra a lista real de produtos do banco. Se o modelo inventar um ID inexistente, ele é simplesmente ignorado.

3. **Validação dos campos:** Apenas sugestões com `id` e `motivo` presentes são aceitas.

Essa camada de validação é fundamental porque modelos de linguagem podem ocasionalmente desobedecer instruções de formato ou gerar dados inválidos — fenômeno conhecido como **alucinação**.

---

## 9. Sistema de Fallback

O sistema foi projetado para **nunca retornar erro 500 ao cliente** em caso de falha na IA. Há dois níveis de proteção:

**Nível 1 — Fallback lógico no `UpsellService`:**
Se a chamada ao Ollama falhar (timeout, modelo não disponível, resposta malformada), o sistema executa automaticamente uma lógica de fallback baseada em regras:

```
SE há combinações populares no banco:
    Retorna os 3 produtos mais co-ocorrentes com motivo genérico

SE o carrinho contém pizzas:
    Sugere 2 bebidas + 1 sobremesa disponíveis

SENÃO:
    Sugere os 3 primeiros produtos disponíveis no cardápio
```

**Nível 2 — CacheErrorHandler no Redis:**
O sistema de cache Redis tem um tratador de erros que captura falhas de serialização/deserialização e instrui o Spring a re-executar a consulta ao banco, evitando propagação do erro ao cliente.

---

## 10. Configuração e Parâmetros

### Parâmetros do modelo (application-prod.properties)

```properties
spring.ai.ollama.base-url=http://ollama:11434
spring.ai.ollama.chat.options.model=llama3.2
```

| Parâmetro | Valor | Significado |
|---|---|---|
| `base-url` | `http://ollama:11434` | Endereço do servidor Ollama na rede Docker |
| `model` | `llama3.2` | Modelo a ser usado para inferência |

### Parâmetros do algoritmo de sugestão

| Parâmetro | Valor | Justificativa |
|---|---|---|
| Máximo de sugestões | 3 | Estudos de UX mostram que mais de 3 opções causam paralisia de escolha |
| Pedidos no histórico | últimos 5 | Equilíbrio entre relevância recente e volume de contexto |
| Itens distintos no histórico | 10 | Limita o tamanho do prompt para não exceder o contexto do modelo |
| Combinações populares | top 5 | Cobertura suficiente para o volume de pedidos de uma pizzaria pequena |
| Limite do motivo | 60 caracteres | Adequado para exibição em cards mobile sem quebra de layout |

---

## 11. Fluxo Completo de Dados

```
Requisição do app:
POST /api/upsell/42
{ "produtosIds": [1, 3] }

                    ┌─────────────────────────────────┐
                    │         UpsellService            │
                    │                                   │
  MySQL ──────────► │ 1. Busca cardápio completo        │
  MySQL ──────────► │ 2. Filtra produtos disponíveis    │
  MySQL ──────────► │ 3. Busca histórico cliente        │
  MySQL ──────────► │ 4. Query co-ocorrência SQL        │
  MySQL ──────────► │ 5. Busca nome do cliente          │
                    │                                   │
                    │ 6. buildPrompt() → texto rico     │
                    │    com todos os dados acima       │
                    └──────────────┬────────────────────┘
                                   │ HTTP POST
                                   ▼
                    ┌─────────────────────────────────┐
                    │     Ollama (Docker container)    │
                    │        Modelo Llama 3.2          │
                    │                                   │
                    │  Processa o prompt (inferência)   │
                    │  Gera resposta JSON com IDs       │
                    └──────────────┬────────────────────┘
                                   │ JSON: [{"id":7,"motivo":"..."}]
                                   ▼
                    ┌─────────────────────────────────┐
                    │         UpsellService            │
                    │                                   │
                    │ 7. parseAiResponse()              │
                    │    - extrai JSON da resposta      │
                    │    - valida IDs contra o banco    │
                    │    - filtra campos inválidos      │
                    │                                   │
                    │ 8. Retorna List<UpsellSugestaoDTO>│
                    └─────────────────────────────────┘

Resposta para o app:
[
  { "id": 7, "nome": "Coca-Cola Lata 350ml",
    "preco": 5.90, "categoriaProduto": "BEBIDAS",
    "motivo": "Combina perfeitamente com sua pizza!" },
  ...
]
```

---

## 12. Comparativo de Abordagens

| Critério | Regras fixas | Filtragem colaborativa | LLM (este projeto) |
|---|---|---|---|
| Personalização | Baixa | Média | Alta |
| Justificativa em linguagem natural | Não | Não | Sim |
| Precisa de grande volume de dados | Não | Sim | Não |
| Custo por requisição | Zero | Zero | Zero (local) |
| Complexidade de implementação | Baixa | Média | Média |
| Funciona bem com cardápio pequeno | Sim | Parcialmente | Sim |

O uso de LLM local (Ollama) é especialmente vantajoso neste contexto porque o cardápio de uma pizzaria tem poucos itens — modelos de filtragem colaborativa precisam de grande volume de dados históricos para funcionar bem. O LLM consegue raciocinar com poucos dados usando conhecimento geral sobre gastronomia e comportamento do consumidor.

---

## 13. Privacidade e Segurança

- **Nenhum dado sai do servidor:** o modelo roda localmente via Ollama. O histórico do cliente, seu nome e seu carrinho não são enviados a APIs externas.
- **Sem custo por requisição:** ao contrário de APIs como OpenAI (cobradas por token), o Ollama é completamente gratuito.
- **Isolamento via Docker:** o container Ollama comunica-se apenas com o backend via rede Docker interna, sem exposição pública.

---

## 14. Limitações Conhecidas

| Limitação | Causa | Mitigação implementada |
|---|---|---|
| Latência da inferência | Modelos LLM são computacionalmente custosos, especialmente em CPU | Fallback imediato se a IA demorar |
| Qualidade do modelo 3B | Modelos menores podem não seguir o formato JSON corretamente | Extração robusta de JSON + validação de IDs |
| Histórico vazio (cliente novo) | Sem pedidos anteriores, o contexto é limitado | O prompt informa "Nenhum pedido anterior" e o modelo usa combinações populares |
| GPU necessária para alta performance | Sem GPU, a inferência é lenta em pico de acessos | Para produção em escala, recomenda-se GPU ou API externa |

---

## 15. Resumo Executivo

O sistema de upsell da Napolitech combina três fontes de inteligência:

1. **Dados históricos individuais** — o que *este cliente específico* já pediu antes
2. **Inteligência coletiva** — o que clientes em geral pedem junto com os itens do carrinho (filtragem colaborativa via SQL)
3. **Raciocínio do LLM** — o modelo sintetiza essas informações e gera uma sugestão em linguagem natural, coerente com o contexto gastronômico

Tudo isso rodando **gratuitamente, localmente e de forma privada** via Ollama, com fallback automático garantindo que o checkout nunca seja bloqueado por falha da IA.
