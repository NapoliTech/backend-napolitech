# Como subir o projeto Napolitech Backend

## Pre-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e rodando

> Nao e necessario ter Java ou Maven instalados. A compilacao e feita dentro do container.

---

## Passo a passo

### 1. Clonar o repositorio

```bash
git clone <url-do-repositorio>
cd backend-napolitech
```

### 2. Construir a imagem do backend

```bash
docker build -t napolitech/backend-napolitech-dev:1.1.0 .
```

> Esse comando compila o projeto Java dentro do Docker e gera a imagem. Na primeira vez pode demorar alguns minutos por conta do download das dependencias.

### 3. Subir todos os containers

```bash
docker compose up -d
```

Aguarde ate que todos os servicos fiquem saudaveis. Voce pode acompanhar o status com:

```bash
docker ps
```

> **Nota:** Na primeira execucao, o RabbitMQ ou MySQL podem demorar um pouco para ficarem prontos (healthcheck). Se o backend nao subir na primeira tentativa, aguarde uns 30 segundos e execute novamente:
> ```bash
> docker compose up -d
> ```

### 4. Verificar se o backend iniciou

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
| RabbitMQ UI | rabbitmq-napolitech | 15672 | Painel de gerenciamento (admin/napolitech) |
| Redis | redis | 6379 | Cache |
| RedisInsight | redisinsight | 5540 | Painel de gerenciamento do Redis |

---

## Testando a API

Com o backend rodando, acesse:

- **Swagger:** `http://localhost:8080/swagger-ui.html`
- **Base URL:** `http://localhost:8080`

Exemplo de teste rapido (cadastrar usuario):

```bash
curl -X POST http://localhost:8080/api/cadastro \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Joao Silva",
    "email": "joao@exemplo.com",
    "dataNasc": "01/01/1990",
    "cpf": "123.456.789-00",
    "senha": "Senha@123",
    "confirmarSenha": "Senha@123",
    "telefone": "(11) 98765-4321"
  }'
```

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

---

## Reconstruir apos alteracoes no codigo

Se voce alterar o codigo fonte, precisa reconstruir a imagem:

```bash
docker compose down
docker build -t napolitech/backend-napolitech-dev:1.1.0 .
docker compose up -d
```

---

## Problemas comuns

**Backend nao sobe (fica como "Created")**
- Os servicos de dependencia (MySQL, RabbitMQ, Redis) ainda nao estavam prontos. Aguarde e execute `docker compose up -d` novamente.

**Porta ja em uso**
- Verifique se nao tem outro servico usando as portas 8080, 3306, 5672, 6379 ou 5540.
- Para verificar: `netstat -ano | findstr :8080` (Windows) ou `lsof -i :8080` (Linux/Mac).

**Erro de conexao com o banco**
- Certifique-se de que o container do MySQL esta healthy: `docker ps`.
- Se necessario, remova os volumes e suba novamente: `docker compose down -v && docker compose up -d`.
