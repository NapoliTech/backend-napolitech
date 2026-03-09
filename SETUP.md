# Napolitech Backend — Setup e Documentacao da API

## Pre-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e rodando

> Nao e necessario ter Java ou Maven instalados. A compilacao e feita dentro do container.

---

## Passo a passo para subir o projeto

### 1. Clonar o repositorio

```bash
git clone <url-do-repositorio>
cd backend-napolitech
```

### 2. Construir a imagem do backend

```bash
docker build -t napolitech/backend-napolitech-dev:1.1.0 .
```

> Na primeira vez pode demorar alguns minutos pelo download das dependencias Maven.

### 3. Subir todos os containers

```bash
docker compose up -d
```

Acompanhe o status ate todos ficarem `healthy`:

```bash
docker ps
```

> **Nota:** Na primeira execucao MySQL e RabbitMQ podem demorar para ficarem prontos. Se o backend nao subir, aguarde 30 segundos e execute `docker compose up -d` novamente.

### 4. Baixar o modelo de IA (uma unica vez)

O Ollama precisa baixar o modelo na primeira vez. Os dados ficam no volume `ollama_data` e persistem entre reinicializacoes.

```bash
# Modelo padrao (3B parametros, ~2GB) — boa qualidade
docker exec ollama ollama pull llama3.2

# Alternativa mais rapida em CPU (1B parametros, ~800MB)
docker exec ollama ollama pull llama3.2:1b
```

> Se usar `llama3.2:1b`, adicione `OLLAMA_MODEL: llama3.2:1b` no environment do backend no `docker-compose.yml`.

### 5. Verificar se o backend iniciou

```bash
docker logs backend-napolitech
```

Procure pela mensagem:
```
Started BackendPizzariaApplication in X seconds
```

---

## Servicos e portas

| Servico | Container | Porta | Descricao |
|---------|-----------|-------|-----------|
| Backend API | backend-napolitech | 8080 | API REST principal |
| MySQL | mysql-napolitech | 3306 | Banco de dados |
| RabbitMQ | rabbitmq-napolitech | 5672 | Mensageria |
| RabbitMQ UI | rabbitmq-napolitech | 15672 | Painel (admin / napolitech) |
| Redis | redis | 6379 | Cache |
| RedisInsight | redisinsight | 5540 | Painel de gerenciamento do Redis |
| Ollama | ollama | 11434 | LLM local para upsell no checkout |

---

## Comandos uteis

| Comando | Descricao |
|---------|-----------|
| `docker compose up -d` | Subir todos os containers |
| `docker compose down` | Parar e remover os containers |
| `docker compose down -v` | Parar, remover containers e **apagar volumes** (dados do banco) |
| `docker logs backend-napolitech` | Ver logs do backend |
| `docker logs -f backend-napolitech` | Acompanhar logs em tempo real |
| `docker ps` | Ver status dos containers |
| `docker exec ollama ollama pull llama3.2` | Baixar modelo de IA |
| `docker exec ollama ollama list` | Listar modelos baixados |

---

## Reconstruir apos alteracoes no codigo

```bash
docker compose down
docker build -t napolitech/backend-napolitech-dev:1.1.0 .
docker compose up -d
```

---

## Problemas comuns

**Backend nao sobe (fica como "Created")**
- Os servicos dependentes ainda nao estavam prontos. Execute `docker compose up -d` novamente.

**Porta ja em uso**
- Windows: `netstat -ano | findstr :8080`
- Linux/Mac: `lsof -i :8080`

**Erro de conexao com o banco**
- Verifique se o MySQL esta healthy: `docker ps`
- Se necessario: `docker compose down -v && docker compose up -d`

**Upsell retornando fallback (sem sugestoes da IA)**
- Verifique se o modelo foi baixado: `docker exec ollama ollama list`
- Verifique se o Ollama esta rodando: `docker logs ollama`

---
---

# Documentacao da API — Guia para o Front-end React Native

**Base URL:** `http://<seu-servidor>:8080`
**Swagger interativo:** `http://localhost:8080/swagger-ui.html`

Todos os endpoints (exceto `/api/cadastro` e `/api/login`) exigem o header:
```
Authorization: Bearer <token_jwt>
```

---

## Autenticacao

### Cadastrar usuario

```
POST /api/cadastro
```

**Body:**
```json
{
  "nome": "Joao Silva",
  "email": "joao@exemplo.com",
  "cpf": "123.456.789-00",
  "telefone": "(11) 98765-4321",
  "dataNasc": "01/01/1990",
  "senha": "Senha@123",
  "confirmarSenha": "Senha@123"
}
```

**Resposta 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### Login

```
POST /api/login
```

**Body:**
```json
{
  "email": "joao@exemplo.com",
  "senha": "Senha@123"
}
```

**Resposta 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "nome": "Joao Silva",
  "email": "joao@exemplo.com",
  "tipoUsuario": "USUARIO_COMUM"
}
```

> Salve o `token` e o `id` no AsyncStorage. O `id` e o `clienteId` usado nos outros endpoints.

---

## Produtos (Cardapio)

### Listar cardapio (paginado)

```
GET /api/produtos?page=0&size=20
Authorization: Bearer <token>
```

**Resposta 200:**
```json
{
  "content": [
    {
      "id": 1,
      "nome": "Pizza Margherita",
      "preco": 45.90,
      "quantidadeEstoque": 50,
      "ingredientes": "Molho de tomate, mussarela, manjericao",
      "categoriaProduto": "PIZZA"
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "number": 0
}
```

**Valores de `categoriaProduto`:** `PIZZA` `PIZZA_DOCE` `PORCAO` `SOBREMESA` `ESFIHA` `ESFIHA_DOCE` `BEBIDAS`

---

## Pedidos

### Criar pedido (Checkout)

```
POST /api/pedidos
Authorization: Bearer <token>
```

**Body:**
```json
{
  "clienteId": 1,
  "enderecoId": 2,
  "tipoEntrega": "DELIVERY",
  "observacao": "Sem cebola",
  "itens": [
    {
      "produtosIds": [1],
      "quantidade": 1,
      "tamanhoPizza": "GRANDE",
      "bordaRecheada": "CATUPIRY"
    },
    {
      "produtosIds": [3],
      "quantidade": 2,
      "tamanhoPizza": "BROTO",
      "bordaRecheada": "NORMAL"
    }
  ]
}
```

**Valores de `tipoEntrega`:** `DELIVERY` `RETIRADA` `ENCOMENDA`

**Valores de `tamanhoPizza`:** `BROTO` `GRANDE` `TREM` `MEIO_A_MEIO`

> Para `MEIO_A_MEIO` passe exatamente 2 IDs em `produtosIds`: `[1, 2]`

**Valores de `bordaRecheada`:** `NORMAL` (+R$0) `CHEDDAR` (+R$8) `CATUPIRY` (+R$8) `CHOCOLATE` (+R$10)

**Resposta 201:**
```json
{
  "pedidoId": 42,
  "dataPedido": "2026-03-08T14:30:00",
  "valorTotal": 61.90,
  "status": "RECEBIDO",
  "nomeCliente": "Joao Silva",
  "tipoEntrega": "DELIVERY",
  "itens": [ ... ]
}
```

---

### Buscar pedido por ID

```
GET /api/pedidos/{id}
Authorization: Bearer <token>
```

---

### Listar pedidos do cliente

> Use a listagem geral com ordenacao por id DESC:

```
GET /api/pedidos?page=0&size=10
Authorization: Bearer <token>
```

---

## Upsell — Sugestoes personalizadas no Checkout (IA)

Este endpoint e chamado **na tela de checkout**, depois que o cliente montou o carrinho mas antes de confirmar o pedido. Ele retorna ate 3 sugestoes personalizadas geradas pelo modelo Ollama com base no historico real do cliente e nas combinacoes mais pedidas na pizzaria.

```
POST /api/upsell/{clienteId}
Authorization: Bearer <token>
```

**Parametro de rota:** `clienteId` — o `id` retornado no login

**Body:**
```json
{
  "produtosIds": [1, 3]
}
```

> Envie os IDs dos produtos que estao no carrinho no momento. Pode ser lista vazia `[]` se o cliente ainda nao escolheu nada.

**Resposta 200:**
```json
[
  {
    "id": 7,
    "nome": "Coca-Cola 2L",
    "preco": 12.90,
    "categoriaProduto": "BEBIDAS",
    "motivo": "Combina muito com sua pizza grande!"
  },
  {
    "id": 12,
    "nome": "Petit Gateau",
    "preco": 18.00,
    "categoriaProduto": "SOBREMESA",
    "motivo": "Voce pediu sobremesa na sua ultima visita!"
  }
]
```

> Se nao houver sugestoes disponiveis, retorna array vazio `[]`. Nunca retorna erro 500 — em caso de falha da IA, o fallback automatico retorna combinacoes populares do banco.

---

### Exemplo de uso no React Native

```javascript
// checkoutScreen.js

const fetchUpsell = async (clienteId, produtosNoCarrinho, token) => {
  try {
    const produtosIds = produtosNoCarrinho.map(item => item.produtoId);

    const response = await fetch(
      `${BASE_URL}/api/upsell/${clienteId}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({ produtosIds }),
      }
    );

    if (!response.ok) return [];

    const sugestoes = await response.json();
    return sugestoes; // [{ id, nome, preco, categoriaProduto, motivo }]
  } catch (error) {
    return []; // nao bloqueia o checkout em caso de erro
  }
};
```

**Quando chamar:** assim que o usuario abre a tela de checkout, em paralelo com o carregamento da tela.

**Como exibir:** cards horizontais ou secao "Adicionar ao pedido" acima do botao de confirmar. Use o campo `motivo` como legenda do card.

---

## Enderecos

### Cadastrar endereco

```
POST /api/enderecos
Authorization: Bearer <token>
```

**Body:**
```json
{
  "rua": "Rua das Flores",
  "bairro": "Centro",
  "numero": 123,
  "complemento": "Apto 4",
  "cidade": "Sao Paulo",
  "estado": "SP",
  "cep": "01310-100",
  "usuarioId": 1
}
```

### Buscar endereco por email do usuario

```
GET /api/enderecos/email/{email}
Authorization: Bearer <token>
```

### Atualizar endereco

```
PUT /api/enderecos/{usuarioId}/{enderecoId}
Authorization: Bearer <token>
```

Body: mesmos campos do cadastro.

---

## Usuarios

### Buscar dados do usuario logado

```
GET /api/{id}
Authorization: Bearer <token>
```

**Resposta 200:**
```json
{
  "idUsuario": 1,
  "nome": "Joao Silva",
  "email": "joao@exemplo.com",
  "telefone": "(11) 98765-4321",
  "cpf": "123.456.789-00",
  "dataNasc": "01/01/1990",
  "pedidos": 5,
  "tipoUsuario": "USUARIO_COMUM"
}
```

### Atualizar dados do usuario

```
PUT /api/{id}
Authorization: Bearer <token>
```

**Body (campos opcionais — envie apenas o que for alterar):**
```json
{
  "nome": "Joao Souza",
  "email": "joao.novo@exemplo.com",
  "telefone": "(11) 91234-5678",
  "senha": "NovaSenha@123"
}
```

---

## Fluxo completo no Checkout (React Native)

```
1. Usuario abre a tela de checkout com o carrinho montado
         |
         +---> GET /api/produtos (para exibir nomes/precos no resumo)
         |
         +---> POST /api/upsell/{clienteId}   <-- chama em paralelo
                     body: { produtosIds: [1, 3] }
                     exibe cards de sugestao enquanto usuario revisa o pedido
         |
2. Usuario confirma o pedido
         |
         +---> POST /api/pedidos
                     body: { clienteId, enderecoId, tipoEntrega, itens: [...] }
         |
3. Resposta com pedidoId e valorTotal -> tela de confirmacao
```
