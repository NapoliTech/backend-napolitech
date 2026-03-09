# Documentacao da API - Napolitech Pizzaria Backend

## Visao Geral

API REST para sistema de gerenciamento de pizzaria com os seguintes modulos:
- **Usuarios** - Gerenciamento de usuarios, autenticacao e autorizacao
- **Produtos** - Cadastro e gerenciamento de produtos
- **Pedidos** - Criacao e gerenciamento de pedidos
- **Enderecos** - Gerenciamento de enderecos de entrega
- **Password Reset** - Recuperacao de senha
- **Dashboard KPIs** - Metricas e indicadores

**Base URL:** `http://localhost:8080`

---

## 1. USUARIOS (`/api`)

### 1.1 Cadastrar Usuario
```
POST /api/cadastro
```

**Request Body:**
```json
{
  "nome": "Joao Silva",
  "email": "joao.silva@exemplo.com",
  "dataNasc": "01/01/1990",
  "cpf": "123.456.789-00",
  "senha": "senha123",
  "confirmarSenha": "senha123",
  "telefone": "(11) 98765-4321"
}
```

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| nome | String | Sim | Nome completo do usuario |
| email | String | Sim | Email valido e unico |
| dataNasc | String | Sim | Data de nascimento (formato: dd/MM/yyyy) |
| cpf | String | Sim | CPF do usuario |
| senha | String | Sim | Senha do usuario |
| confirmarSenha | String | Sim | Confirmacao da senha |
| telefone | String | Sim | Telefone de contato |

**Response (201 CREATED):**
```json
{
  "usuario": {
    "id": 1,
    "nome": "Joao Silva",
    "email": "joao.silva@exemplo.com",
    "cpf": "123.456.789-00",
    "telefone": "(11) 98765-4321",
    "dataNasc": "01/01/1990"
  },
  "mensagem": "Usuario cadastrado com sucesso"
}
```

---

### 1.2 Cadastrar Atendente
```
POST /api/cadastro/atendente
```

**Request Body:** Mesmo formato do cadastro de usuario

**Response (201 CREATED):**
```json
{
  "usuario": { ... },
  "mensagem": "Atendente cadastrado com sucesso"
}
```

---

### 1.3 Cadastrar Admin
```
POST /api/cadastro/admin
```

**Request Body:** Mesmo formato do cadastro de usuario

**Response (201 CREATED):**
```json
{
  "usuario": { ... },
  "mensagem": "Admin cadastrado com sucesso"
}
```

---

### 1.4 Login
```
POST /api/login
```

**Request Body:**
```json
{
  "email": "usuario@exemplo.com",
  "senha": "senha123"
}
```

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| email | String | Sim | Email do usuario |
| senha | String | Sim | Senha do usuario |

**Response (200 OK):**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
> Retorna o token JWT para autenticacao

---

### 1.5 Listar Usuarios (Paginado)
```
GET /api
```

**Query Parameters:**
| Parametro | Tipo | Padrao | Descricao |
|-----------|------|--------|-----------|
| page | int | 0 | Numero da pagina |
| size | int | 10 | Quantidade por pagina |
| sort | String | "nome,ASC" | Campo e direcao de ordenacao |

**Exemplo:**
```
GET /api?page=0&size=10&sort=nome,ASC
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "nome": "Joao Silva",
      "email": "joao@exemplo.com",
      ...
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0
}
```

---

### 1.6 Buscar Usuario por ID
```
GET /api/{id}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| id | Long | ID do usuario |

**Response (200 OK):**
```json
{
  "usuario": {
    "id": 1,
    "nome": "Joao Silva",
    "email": "joao@exemplo.com",
    "cpf": "123.456.789-00",
    "telefone": "(11) 98765-4321",
    "dataNasc": "01/01/1990",
    "enderecos": [...]
  },
  "pedidosQuantidade": 5,
  "pedidosRealizados": [
    {
      "id": 1,
      "itens": [...],
      "dataPedido": "2023-10-01T12:30:00",
      "statusPedido": "ENTREGUE",
      "precoTotal": 89.90
    }
  ]
}
```

---

### 1.7 Buscar Usuario por Email
```
GET /api/email/{email}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| email | String | Email do usuario |

**Response (200 OK):**
```json
{
  "usuario": {
    "id": 1,
    "nome": "Joao Silva",
    "email": "joao@exemplo.com",
    ...
  }
}
```

---

### 1.8 Atualizar Usuario
```
PUT /api/{id}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| id | Long | ID do usuario |

**Request Body:**
```json
{
  "nome": "Joao Silva Atualizado",
  "email": "joao.novo@exemplo.com",
  "senha": "novaSenha123",
  "cpf": "123.456.789-00",
  "telefone": "(11) 98765-4321",
  "dataNasc": "01/01/1990"
}
```

> Todos os campos sao opcionais. Envie apenas os campos que deseja atualizar.

**Response (200 OK):**
```json
{
  "usuario": {
    "id": 1,
    "nome": "Joao Silva Atualizado",
    ...
  },
  "mensagem": "Usuario Atualizado com sucesso"
}
```

---

### 1.9 Deletar Usuario
```
DELETE /api/{id}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| id | Long | ID do usuario |

**Response (200 OK):**
```json
{
  "mensagem": "Usuario deletado com sucesso"
}
```

---

## 2. PRODUTOS (`/api/produtos`)

### 2.1 Cadastrar Produto
```
POST /api/produtos
```

**Request Body:**
```json
{
  "nome": "Pizza de Calabresa",
  "preco": 45.90,
  "quantidade": 10,
  "ingredientes": "Calabresa, queijo, molho de tomate",
  "categoriaProduto": "PIZZA"
}
```

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| nome | String | Sim | Nome do produto |
| preco | Double | Sim | Preco unitario |
| quantidade | Integer | Sim | Quantidade em estoque |
| ingredientes | String | Nao | Lista de ingredientes |
| categoriaProduto | String | Sim | Categoria do produto (ver enum) |

**Valores para `categoriaProduto`:**
- `PIZZA`
- `PORCAO`
- `SOBREMESA`
- `PIZZA_DOCE`
- `ESFIHA`
- `ESFIHA_DOCE`
- `BEBIDAS`

**Response (201 CREATED):**
```json
{
  "produtoId": 1,
  "nome": "Pizza de Calabresa",
  "preco": 45.90,
  "quantidade": 10,
  "ingredientes": "Calabresa, queijo, molho de tomate",
  "categoriaProduto": "PIZZA",
  "mensagem": "Produto cadastrado com sucesso!"
}
```

---

### 2.2 Listar Produtos (Paginado)
```
GET /api/produtos
```

**Query Parameters:**
| Parametro | Tipo | Padrao | Descricao |
|-----------|------|--------|-----------|
| page | int | 0 | Numero da pagina |
| size | int | 10 | Quantidade por pagina |
| sort | String | "id,DESC" | Campo e direcao de ordenacao |

**Exemplo:**
```
GET /api/produtos?page=0&size=20&sort=nome,ASC
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "nome": "Pizza de Calabresa",
      "preco": 45.90,
      "quantidade": 10,
      "ingredientes": "Calabresa, queijo, molho de tomate",
      "categoriaProduto": "PIZZA"
    }
  ],
  "totalElements": 30,
  "totalPages": 3,
  "size": 10,
  "number": 0
}
```

---

### 2.3 Buscar Produto por ID
```
GET /api/produtos/{id}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| id | Integer | ID do produto |

**Response (200 OK):**
```json
{
  "produto": {
    "id": 1,
    "nome": "Pizza de Calabresa",
    "preco": 45.90,
    "quantidade": 10,
    "ingredientes": "Calabresa, queijo, molho de tomate",
    "categoriaProduto": "PIZZA"
  }
}
```

---

### 2.4 Deletar Produto
```
DELETE /api/produtos/{id}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| id | Integer | ID do produto |

**Response (200 OK):**
```json
{
  "mensagem": "Produto deletado com sucesso!"
}
```

**Response (404 NOT FOUND):**
```json
{
  "erro": "Produto nao encontrado!"
}
```

---

## 3. PEDIDOS (`/api/pedidos`)

### 3.1 Criar Pedido
```
POST /api/pedidos
```

**Request Body:**
```json
{
  "clienteId": 1,
  "nomeCliente": "Joao Silva",
  "valorTotal": 89.90,
  "enderecoId": 1,
  "telefone": "(11) 98765-4321",
  "observacao": "Sem cebola",
  "statusPedido": "RECEBIDO",
  "tipoEntrega": "DELIVERY",
  "bordaRecheada": "CATUPIRY",
  "dataPedido": "2023-10-01T12:30:00",
  "itens": [
    {
      "produto": [1, 2],
      "quantidade": 2,
      "tamanhoPizza": "GRANDE",
      "bordaRecheada": "CATUPIRY"
    }
  ]
}
```

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| clienteId | Long | Sim | ID do cliente |
| nomeCliente | String | Sim | Nome do cliente |
| valorTotal | Double | Sim | Valor total do pedido |
| enderecoId | Long | Condicional | ID do endereco (obrigatorio para DELIVERY) |
| telefone | String | Sim | Telefone de contato |
| observacao | String | Nao | Observacoes do pedido |
| statusPedido | String | Sim | Status inicial do pedido |
| tipoEntrega | String | Sim | Tipo de entrega |
| bordaRecheada | String | Nao | Tipo de borda recheada |
| dataPedido | String | Sim | Data/hora do pedido (ISO 8601) |
| itens | Array | Sim | Lista de itens do pedido |

**Estrutura do Item:**
| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| produto | Array[Long] | Sim | Lista de IDs de produtos |
| quantidade | Integer | Sim | Quantidade de unidades |
| tamanhoPizza | String | Sim | Tamanho da pizza |
| bordaRecheada | String | Nao | Tipo de borda para este item |

**Valores para `statusPedido`:**
- `RECEBIDO`
- `EM_PREPARO`
- `ENTREGUE`
- `ENCERRADO`
- `CANCELADO`

**Valores para `tipoEntrega`:**
- `RETIRADA`
- `DELIVERY`
- `ENCOMENDA`

**Valores para `bordaRecheada`:**
| Valor | Acrescimo |
|-------|-----------|
| `NORMAL` | R$ 0,00 |
| `CHEDDAR` | R$ 8,00 |
| `CATUPIRY` | R$ 8,00 |
| `CHOCOLATE` | R$ 10,00 |

**Valores para `tamanhoPizza`:**
| Valor | Fatias |
|-------|--------|
| `BROTO` | 2 fatias |
| `GRANDE` | 1 fatia |
| `TREM` | 4 fatias |
| `MEIO_A_MEIO` | 2 fatias |

**Response (201 CREATED):**
```json
{
  "pedidoId": 1,
  "dataPedido": "2023-10-01T12:30:00",
  "valorTotal": 89.90,
  "itens": [...],
  "nomeCliente": "Joao Silva",
  "telefone": "(11) 98765-4321",
  "tipoEntrega": "DELIVERY",
  "endereco": {
    "id": 1,
    "rua": "Rua das Flores",
    "numero": 123,
    ...
  },
  "observacao": "Sem cebola",
  "status": "RECEBIDO"
}
```

---

### 3.2 Listar Pedidos (Paginado)
```
GET /api/pedidos
```

**Query Parameters:**
| Parametro | Tipo | Padrao | Descricao |
|-----------|------|--------|-----------|
| page | int | 0 | Numero da pagina |
| size | int | 10 | Quantidade por pagina |
| sort | String | "id,DESC" | Campo e direcao de ordenacao |

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "dataPedido": "2023-10-01T12:30:00",
      "precoTotal": 89.90,
      "statusPedido": "ENTREGUE",
      "tipoEntrega": "DELIVERY",
      ...
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

---

### 3.3 Buscar Pedido por ID
```
GET /api/pedidos/{id}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| id | Long | ID do pedido |

**Response (200 OK):**
```json
{
  "id": 1,
  "cliente": {
    "nome": "Joao Silva",
    "email": "joao@exemplo.com",
    "telefone": "(11) 98765-4321",
    "quantidadePedidos": 5
  },
  "endereco": {
    "id": 1,
    "rua": "Rua das Flores",
    "numero": 123,
    "bairro": "Centro",
    "cidade": "Sao Paulo",
    "estado": "SP",
    "cep": "01001-000"
  },
  "nomeCliente": "Joao Silva",
  "statusPedido": "ENTREGUE",
  "precoTotal": 89.90,
  "observacao": "Sem cebola",
  "tipoEntrega": "DELIVERY",
  "itens": [
    {
      "id": 1,
      "produto": {...},
      "quantidade": 2,
      "tamanhoPizza": "GRANDE",
      "bordaRecheada": "CATUPIRY"
    }
  ],
  "dataPedido": "2023-10-01T12:30:00"
}
```

---

### 3.4 Atualizar Status do Pedido
```
PUT /api/pedidos/{id}/status
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| id | Long | ID do pedido |

**Request Body:**
```json
{
  "status": "EM_PREPARO"
}
```

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| status | String | Sim | Novo status do pedido |

**Response (200 OK):**
```json
{
  "id": 1,
  "statusPedido": "EM_PREPARO",
  ...
}
```

---

## 4. ENDERECOS (`/api/enderecos`)

### 4.1 Cadastrar Endereco
```
POST /api/enderecos
```

**Request Body:**
```json
{
  "rua": "Rua das Flores",
  "bairro": "Centro",
  "numero": 123,
  "complemento": "Apartamento 101",
  "cidade": "Sao Paulo",
  "estado": "SP",
  "cep": "01001-000",
  "usuarioId": 1
}
```

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| rua | String | Sim | Nome da rua |
| bairro | String | Sim | Nome do bairro |
| numero | Integer | Sim | Numero do endereco |
| complemento | String | Nao | Complemento (apto, bloco, etc) |
| cidade | String | Sim | Nome da cidade |
| estado | String | Sim | Sigla do estado (UF) |
| cep | String | Sim | CEP do endereco |
| usuarioId | Long | Sim | ID do usuario proprietario |

**Response (201 CREATED):**
```json
{
  "enderecoId": 1,
  "rua": "Rua das Flores",
  "numero": 123,
  "bairro": "Centro",
  "complemento": "Apartamento 101",
  "cidade": "Sao Paulo",
  "estado": "SP",
  "cep": "01001-000",
  "mensagem": "Endereco cadastrado com sucesso!"
}
```

---

### 4.2 Listar Todos os Enderecos
```
GET /api/enderecos
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "rua": "Rua das Flores",
    "numero": 123,
    "bairro": "Centro",
    "complemento": "Apartamento 101",
    "cidade": "Sao Paulo",
    "estado": "SP",
    "cep": "01001-000"
  }
]
```

---

### 4.3 Buscar Endereco por ID
```
GET /api/enderecos/{id}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| id | Integer | ID do endereco |

**Response (200 OK):**
```json
{
  "endereco": {
    "id": 1,
    "rua": "Rua das Flores",
    "numero": 123,
    "bairro": "Centro",
    "complemento": "Apartamento 101",
    "cidade": "Sao Paulo",
    "estado": "SP",
    "cep": "01001-000"
  },
  "usuario": {
    "id": 1,
    "nome": "Joao Silva",
    "email": "joao@exemplo.com"
  }
}
```

---

### 4.4 Buscar Endereco por Email do Usuario
```
GET /api/enderecos/email/{email}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| email | String | Email do usuario |

**Response (200 OK):**
```json
{
  "endereco": {...},
  "usuario": {...}
}
```

---

### 4.5 Atualizar Endereco
```
PUT /api/enderecos/{usuarioId}/{enderecoId}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| usuarioId | Long | ID do usuario |
| enderecoId | Long | ID do endereco |

**Request Body:**
```json
{
  "rua": "Rua Nova",
  "bairro": "Jardins",
  "numero": 456,
  "complemento": "Casa",
  "cidade": "Sao Paulo",
  "estado": "SP",
  "cep": "01002-000"
}
```

**Response (200 OK):**
```json
{
  "enderecoId": 1,
  "rua": "Rua Nova",
  "numero": 456,
  "bairro": "Jardins",
  "complemento": "Casa",
  "cidade": "Sao Paulo",
  "estado": "SP",
  "cep": "01002-000",
  "mensagem": "Endereco atualizado com sucesso!"
}
```

---

### 4.6 Deletar Endereco
```
DELETE /api/enderecos/{id}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| id | Integer | ID do endereco |

**Response (200 OK):**
```json
{
  "mensagem": "Endereco deletado com sucesso!"
}
```

**Response (404 NOT FOUND):**
```json
{
  "erro": "Endereco nao encontrado!"
}
```

---

## 5. RECUPERACAO DE SENHA (`/api/password-reset`)

### 5.1 Solicitar Recuperacao de Senha
```
POST /api/password-reset/request
```

**Request Body:**
```json
{
  "email": "usuario@exemplo.com"
}
```

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| email | String | Sim | Email cadastrado do usuario |

**Response (202 ACCEPTED):**
```json
{
  "mensagem": "E-mail enviado com sucesso."
}
```

---

### 5.2 Redefinir Senha
```
POST /api/password-reset/reset
```

**Request Body:**
```json
{
  "token": "token-recebido-por-email",
  "newPassword": "novaSenha123"
}
```

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| token | String | Sim | Token recebido por email |
| newPassword | String | Sim | Nova senha desejada |

**Response (200 OK):**
```json
{
  "mensagem": "Senha redefinida com sucesso!"
}
```

---

## 6. DASHBOARD KPIs (`/api/dashboard/kpis`)

### 6.1 Listar Todos os KPIs
```
GET /api/dashboard/kpis
```

**Response (200 OK):**
```json
{
  "faturamentoMensal": 50000.00,
  "totalPedidos": 250,
  "ticketMedio": 200.00,
  ...
}
```

---

### 6.2 Faturamento Anual
```
GET /api/dashboard/kpis/faturamento/{ano}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| ano | int | Ano desejado (ex: 2024) |

**Exemplo:**
```
GET /api/dashboard/kpis/faturamento/2024
```

**Response (200 OK):**
```json
{
  "janeiro": 15000.00,
  "fevereiro": 18000.00,
  "marco": 22000.00,
  ...
}
```

---

### 6.3 Faturamento Mensal
```
GET /api/dashboard/kpis/faturamento/{ano}/{mes}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| ano | int | Ano desejado |
| mes | int | Mes desejado (1-12) |

**Exemplo:**
```
GET /api/dashboard/kpis/faturamento/2024/3
```

**Response (200 OK):**
```json
{
  "valor": 22000.00
}
```

---

### 6.4 Vendas dos Ultimos 7 Dias
```
GET /api/dashboard/kpis/vendas/ultimos-sete-dias
```

**Response (200 OK):**
```json
{
  "2024-03-01": 15,
  "2024-03-02": 22,
  "2024-03-03": 18,
  "2024-03-04": 25,
  "2024-03-05": 30,
  "2024-03-06": 28,
  "2024-03-07": 20
}
```

---

### 6.5 Vendas por Categoria
```
GET /api/dashboard/kpis/vendas/categoria/{ano}/{mes}
```

**Path Variables:**
| Parametro | Tipo | Descricao |
|-----------|------|-----------|
| ano | int | Ano desejado |
| mes | int | Mes desejado (1-12) |

**Exemplo:**
```
GET /api/dashboard/kpis/vendas/categoria/2024/3
```

**Response (200 OK):**
```json
{
  "PIZZA": 150,
  "BEBIDAS": 80,
  "SOBREMESA": 45,
  "PORCAO": 30,
  "ESFIHA": 25
}
```

---

### 6.6 Cards Principais do Dashboard
```
GET /api/dashboard/kpis/cards
```

**Response (200 OK):**
```json
{
  "faturamentoMensal": 50000.00,
  "totalPedidosMes": 250,
  "ticketMedio": 200.00,
  "pedidosHoje": 15,
  "clientesNovos": 20
}
```

---

## RESUMO DE ENDPOINTS

### Usuarios (9 endpoints)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/api/cadastro` | Cadastrar usuario |
| POST | `/api/cadastro/atendente` | Cadastrar atendente |
| POST | `/api/cadastro/admin` | Cadastrar admin |
| POST | `/api/login` | Fazer login |
| GET | `/api` | Listar usuarios |
| GET | `/api/{id}` | Buscar usuario por ID |
| GET | `/api/email/{email}` | Buscar usuario por email |
| PUT | `/api/{id}` | Atualizar usuario |
| DELETE | `/api/{id}` | Deletar usuario |

### Produtos (4 endpoints)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/api/produtos` | Cadastrar produto |
| GET | `/api/produtos` | Listar produtos |
| GET | `/api/produtos/{id}` | Buscar produto por ID |
| DELETE | `/api/produtos/{id}` | Deletar produto |

### Pedidos (4 endpoints)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/api/pedidos` | Criar pedido |
| GET | `/api/pedidos` | Listar pedidos |
| GET | `/api/pedidos/{id}` | Buscar pedido por ID |
| PUT | `/api/pedidos/{id}/status` | Atualizar status |

### Enderecos (6 endpoints)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/api/enderecos` | Cadastrar endereco |
| GET | `/api/enderecos` | Listar enderecos |
| GET | `/api/enderecos/{id}` | Buscar endereco por ID |
| GET | `/api/enderecos/email/{email}` | Buscar por email |
| PUT | `/api/enderecos/{usuarioId}/{enderecoId}` | Atualizar endereco |
| DELETE | `/api/enderecos/{id}` | Deletar endereco |

### Password Reset (2 endpoints)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/api/password-reset/request` | Solicitar reset |
| POST | `/api/password-reset/reset` | Redefinir senha |

### Dashboard KPIs (6 endpoints)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/api/dashboard/kpis` | Listar todos os KPIs |
| GET | `/api/dashboard/kpis/faturamento/{ano}` | Faturamento anual |
| GET | `/api/dashboard/kpis/faturamento/{ano}/{mes}` | Faturamento mensal |
| GET | `/api/dashboard/kpis/vendas/ultimos-sete-dias` | Vendas ultimos 7 dias |
| GET | `/api/dashboard/kpis/vendas/categoria/{ano}/{mes}` | Vendas por categoria |
| GET | `/api/dashboard/kpis/cards` | Cards do dashboard |

---

## CODIGOS DE RESPOSTA HTTP

| Codigo | Descricao |
|--------|-----------|
| 200 | OK - Requisicao bem sucedida |
| 201 | Created - Recurso criado com sucesso |
| 202 | Accepted - Requisicao aceita para processamento |
| 400 | Bad Request - Dados invalidos |
| 401 | Unauthorized - Nao autenticado |
| 403 | Forbidden - Sem permissao |
| 404 | Not Found - Recurso nao encontrado |
| 500 | Internal Server Error - Erro interno do servidor |

---

## AUTENTICACAO

A API utiliza **JWT (JSON Web Token)** para autenticacao.

1. Faca login em `POST /api/login`
2. Receba o token JWT na resposta
3. Inclua o token no header de todas as requisicoes protegidas:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

**Total de Endpoints: 31**

*Documentacao gerada em 07/03/2026*
