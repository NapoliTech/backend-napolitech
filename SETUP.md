# Napolitech Backend — Como Rodar o Projeto

> Branch: `feat/ai-upsell-ollama`

---

## Pre-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e rodando

Nao e necessario ter Java, Maven ou qualquer outra ferramenta instalada.
A compilacao do projeto acontece dentro do proprio container Docker.

---

## Primeira vez (build + subida completa)

### 1. Clonar o repositorio e entrar na branch

```bash
git clone https://github.com/NapoliTech/backend-napolitech.git
cd backend-napolitech
git checkout feat/ai-upsell-ollama
```

### 2. Buildar a imagem e subir todos os containers

```bash
docker compose up -d --build
```

O `--build` compila o projeto Java e cria a imagem do backend automaticamente.
Na primeira vez pode demorar alguns minutos pelo download das dependencias Maven.

Acompanhe ate todos os containers ficarem `healthy`:

```bash
docker ps
```

Resultado esperado:

```
NAMES                STATUS
backend-napolitech   Up X minutes
mysql-napolitech     Up X minutes (healthy)
rabbitmq-napolitech  Up X minutes (healthy)
redis                Up X minutes (healthy)
redisinsight         Up X minutes
ollama               Up X minutes
```

> Se o backend aparecer como `Exited`, aguarde 30 segundos e execute `docker compose up -d` novamente.
> O MySQL e o RabbitMQ podem demorar para ficarem prontos.

### 3. Baixar o modelo de IA (uma unica vez)

O Ollama precisa baixar o modelo na primeira execucao. Os dados ficam no volume
`ollama_data` e persistem entre reinicializacoes — so precisa fazer isso uma vez.

```bash
docker exec ollama ollama pull llama3.2
```

> Tamanho: ~2GB. Aguarde o download completar.

Se preferir um modelo menor e mais rapido (menos qualidade):

```bash
docker exec ollama ollama pull llama3.2:1b
```

Para confirmar que o modelo foi baixado:

```bash
docker exec ollama ollama list
```

### 4. Verificar se o backend iniciou corretamente

```bash
docker logs backend-napolitech
```

Procure pela linha:

```
Started BackendPizzariaApplication in X seconds
```

Tambem verifique o cardapio mock que e carregado automaticamente:

```
INFO  DadosMock : Produto cadastrado: Pizza de Calabresa
INFO  DadosMock : Produto cadastrado: Coca-Cola Lata 350ml
...
INFO  DadosMock : Carga do cardapio mock concluida.
```

> O mock e idempotente — se os produtos ja existirem no banco, eles sao ignorados.
> Nao ha risco de duplicatas em reinicializacoes.

---

## Usos do dia a dia (apos a primeira vez)

### Subir os containers normalmente

```bash
docker compose up -d
```

### Derrubar os containers

```bash
docker compose down
```

### Rebuildar apos alterar o codigo

```bash
docker compose down
docker compose up -d --build
```

---

## Se apagou os volumes (`docker compose down -v`)

Os volumes guardam os dados do banco, Redis e o modelo do Ollama.
Se foram apagados, siga:

```bash
docker compose up -d
docker exec ollama ollama pull llama3.2
```

O banco e o cardapio sao recriados automaticamente pelo Spring e pelo mock de dados.

---

## Servicos e portas

| Servico        | Container           | Porta | Acesso                          |
|----------------|---------------------|-------|---------------------------------|
| Backend API    | backend-napolitech  | 8080  | http://localhost:8080           |
| MySQL          | mysql-napolitech    | 3306  | usuario: napolitech / napolitech_dev |
| RabbitMQ       | rabbitmq-napolitech | 5672  | —                               |
| RabbitMQ UI    | rabbitmq-napolitech | 15672 | http://localhost:15672 (admin / napolitech) |
| Redis          | redis               | 6379  | —                               |
| RedisInsight   | redisinsight        | 5540  | http://localhost:5540           |
| Ollama (IA)    | ollama              | 11434 | http://localhost:11434          |

---

## Comandos uteis

| Comando | Descricao |
|---------|-----------|
| `docker compose up -d` | Subir todos os containers |
| `docker compose up -d --build` | Rebuildar e subir |
| `docker compose down` | Parar e remover containers |
| `docker compose down -v` | Parar, remover containers e apagar volumes |
| `docker ps` | Ver status de todos os containers |
| `docker logs backend-napolitech` | Ver logs do backend |
| `docker logs -f backend-napolitech` | Acompanhar logs em tempo real |
| `docker exec ollama ollama pull llama3.2` | Baixar modelo de IA |
| `docker exec ollama ollama list` | Listar modelos baixados |
| `docker exec redis redis-cli FLUSHALL` | Limpar cache Redis |

---

## Problemas comuns

**Backend aparece como `Exited` ou nao responde**
- MySQL ou RabbitMQ ainda estavam inicializando. Execute `docker compose up -d` novamente.

**Tela de produtos retorna erro 500**
- Limpe o cache Redis: `docker exec redis redis-cli FLUSHALL`
- Reinicie o backend: `docker compose restart backend`

**Upsell retornando lista vazia (sem sugestoes da IA)**
- Verifique se o modelo foi baixado: `docker exec ollama ollama list`
- Verifique logs do Ollama: `docker logs ollama`
- Tente baixar novamente: `docker exec ollama ollama pull llama3.2`

**Porta ja em uso**
- Windows: `netstat -ano | findstr :8080`
- Linux/Mac: `lsof -i :8080`

**`docker compose up --build` muito lento**
- Normal na primeira vez (download das dependencias Maven ~500MB).
- A partir da segunda vez o Docker usa cache e e rapido.

---

## O que sobe automaticamente

Ao iniciar o backend, o sistema carrega automaticamente um cardapio completo com:

- 12 pizzas salgadas (Calabresa, Portuguesa, Frango com Catupiry, etc.)
- 6 pizzas doces (Chocolate, Brigadeiro, Banana com Canela, etc.)
- 11 bebidas (Coca-Cola, Guarana, agua, sucos, cervejas)

Nenhum usuario e cadastrado automaticamente — crie sua conta pelo endpoint `POST /api/cadastro`.

---

## Documentacao da API

Consulte os arquivos na pasta `docs/`:

- `docs/API_DOCUMENTATION.md` — documentacao completa de todos os endpoints
- `docs/UPSELL_FRONTEND.md` — guia de integracao do upsell no React Native
