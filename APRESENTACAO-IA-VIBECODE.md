# IA Generativa como Motor de Desenvolvimento
## Do Vibe Code à Arquitetura Multi-Agente — Projeto Napolitech

---

## SLIDE 1 — TÍTULO

**IA Generativa como Motor de Desenvolvimento**
*Do Vibe Code à Arquitetura Multi-Agente — Projeto Napolitech*

---

## SLIDE 2 — O PROBLEMA QUE RESOLVEMOS

Desenvolvimento moderno exige velocidade sem perder qualidade técnica.

- Onboarding de novos devs leva dias
- Documentação fica desatualizada
- Decisões de arquitetura se perdem no tempo
- Integração entre equipes (frontend, backend, dados) gera ruído

**Nossa resposta:** IA generativa integrada ao fluxo de desenvolvimento como um membro sênior da equipe

---

## SLIDE 3 — A FERRAMENTA: CLAUDE CODE

**Claude Code** é o CLI/extensão IDE da Anthropic — IA generativa diretamente no terminal e no VS Code.

| Capacidade | Como usamos |
|---|---|
| Leitura e escrita de código | Refatorações, novas features, configs |
| Execução de comandos | Docker, Git, curl — tudo via linguagem natural |
| Memória persistente | Contexto do projeto entre sessões |
| Multi-arquivo | Entende arquitetura completa, não só um arquivo |

> *"Não é autocomplete. É um engenheiro que lê todo o repositório antes de responder."*

---

## SLIDE 4 — MEMÓRIA PERSISTENTE

O maior diferencial do nosso uso: **Claude com memória entre sessões**.

```
.claude/
  memory/
    user_profile.md      → quem é o dev, nível técnico
    project_context.md   → decisões de arquitetura
    feedback.md          → o que funcionou, o que evitar
    references.md        → onde estão os recursos externos
```

**Na prática:**
- Claude lembra que o ngrok usa domínio fixo
- Sabe que JWT_SECRET não pode ser efêmero
- Conhece a URL do Netlify, as credenciais do banco, os enums do sistema
- Não precisa ser re-explicado a cada conversa

---

## SLIDE 5 — ARQUITETURA MULTI-AGENTE

Criamos agentes especializados com contexto completo — cada um é um expert em sua área.

```
prompts/
  agente-rodar-projeto.json          → DevOps: sobe o ambiente do zero
  agente-tutorial-rodar-projeto.json → Onboarding: guia novos devs
  frontend-integration-agent.json    → Frontend: contrato completo da API
  fix-cors-ngrok-warning.json        → Troubleshooting: resolve CORS
  etl/agente-bi-etl.json             → Dados: ETL + star schema + queries BI
```

**Cada agente recebe:**
- Identidade e objetivo claro
- Contexto técnico completo (schema, endpoints, enums, credenciais)
- Árvore de decisão para erros
- Critérios explícitos de sucesso

---

## SLIDE 6 — VIBE CODING NA PRÁTICA

**Vibe Coding** = desenvolvimento guiado por intenção, não por sintaxe.

O dev descreve o que quer. A IA entende o contexto e executa.

```
Dev: "meu backend no docker não estou conseguindo restartar"
→ Claude verifica containers, lê logs, identifica causa
→ Executa docker restart, confirma que subiu

Dev: "adicione o domínio do Netlify no CORS"
→ Claude lê .env, identifica variável, atualiza, rebuild

Dev: "crie um ETL em Python para BI"
→ Claude lê todas as entidades Java, mapeia schema
→ Gera star schema completo com 7 tabelas e pipeline
```

---

## SLIDE 7 — IA DENTRO DO PRODUTO

Além de ferramenta de desenvolvimento, IA generativa **dentro da aplicação**:

**Feature: Upsell Inteligente no Checkout**

```
Cliente monta carrinho
        ↓
POST /api/upsell/{clienteId}
        ↓
Ollama (Llama 3.2 — LLM local)
        ↓
"Você pediu Pizza Calabresa.
 Que tal adicionar uma Cerveja Heineken?"
        ↓
Até 3 sugestões personalizadas com motivo
```

**Diferencial técnico:**
- LLM roda **100% local** via Ollama — zero custo por request
- Fallback automático por regras se modelo não disponível
- Latência média < 2s

---

## SLIDE 8 — STACK COMPLETA DO PROJETO

```
DESENVOLVIMENTO          PRODUTO               DADOS
─────────────────        ──────────────────    ─────────────────
Claude Code (IA)         Spring Boot 3         MySQL 8 (operacional)
Memória persistente      JWT Auth              Star Schema (analítico)
Agentes especializados   Redis Cache           ETL Python
Docker / Docker Hub      RabbitMQ              dim_tempo, dim_produto
ngrok (domínio fixo)     Ollama / Llama 3.2    fato_pedido, resumos
Netlify (frontend)       REST API              BI direto no banco
```

---

## SLIDE 9 — RESULTADOS CONCRETOS

| Métrica | Resultado |
|---|---|
| Tempo de onboarding | De horas para **< 10 minutos** |
| Documentação gerada | 6 arquivos MD + 6 prompts JSON automaticamente |
| Imagem Docker Hub | Publicada e versionada (`1.1.0` + `latest`) |
| Agentes criados | 6 agentes especializados com contexto completo |
| Features de IA no produto | Upsell com LLM local sem custo por uso |
| Camada analítica | Star schema com 7 tabelas prontas para BI |

---

## SLIDE 10 — CONCLUSÃO

> *IA generativa não substituiu o desenvolvedor. Multiplicou o que um desenvolvedor consegue entregar.*

**O que mudou no processo:**
- Arquitetura decidida e documentada em conversa
- Código gerado com contexto real do projeto
- Problemas diagnosticados em segundos
- Novos membros integrados com um arquivo JSON

**O que não mudou:**
- Decisões estratégicas são humanas
- Revisão de código é humana
- Direção do produto é humana

A IA executa. O engenheiro dirige.
