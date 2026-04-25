# Napolitech Backend — Guia para Novo Desenvolvedor

Tudo que você precisa para ter o backend rodando do zero em menos de 10 minutos.

---

## Pré-requisitos

| Ferramenta | Como instalar |
|---|---|
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) | Instale e abra antes de começar |
| Git | Já instalado na maioria das máquinas |

Não precisa de Java, Maven nem nada mais. Tudo roda dentro do Docker.

---

## Credenciais necessárias

Peça ao dev principal (Alejandro) antes de começar:

| O que | Para que serve |
|---|---|
| `JWT_SECRET` | Assina os tokens de login. Deve ser igual em todos os ambientes |
| `NGROK_AUTHTOKEN` | Autentica no ngrok para usar o domínio fixo |

---

## Passo a passo

### 1. Clonar o repositório e entrar na branch

```bash
git clone <url-do-repositorio>
cd backend-napolitech
git checkout feat/ai-upsell-ollama
```

### 2. Criar o arquivo .env

```bash
cp .env.example .env
```

Abra o `.env` e preencha os dois valores que você recebeu do Alejandro:

```env
JWT_SECRET=<valor fornecido pelo Alejandro>
NGROK_AUTHTOKEN=<valor fornecido pelo Alejandro>
```

O resto do `.env` já está configurado e não precisa alterar.

### 3. Baixar a imagem e subir os serviços

```bash
docker compose pull backend
docker compose up -d
```

Aguarde ~30 segundos enquanto o MySQL e o RabbitMQ ficam prontos. Você pode acompanhar com:

```bash
docker compose logs -f backend
```

O backend está pronto quando aparecer no log:
```
Carga do cardápio mock concluída.
```

### 4. Subir o ngrok (túnel HTTPS)

```bash
docker compose --profile ngrok up -d ngrok
```

O backend ficará acessível publicamente em:
```
https://internation-sully-kolten.ngrok-free.dev
```

### 5. (Opcional) Baixar o modelo de IA

Necessário apenas para o recurso de sugestões no checkout:

```bash
docker exec ollama ollama pull llama3.2:1b
```

---

## Verificar se tudo está funcionando

```bash
# Ver status de todos os containers (todos devem estar "Up")
docker compose ps

# Testar o backend diretamente
curl -X POST https://internation-sully-kolten.ngrok-free.dev/api/login \
  -H "Content-Type: application/json" \
  -H "ngrok-skip-browser-warning: true" \
  -d '{"email":"teste@teste.com","senha":"123"}'
# Esperado: resposta 400 (não 404, não 502) — significa que chegou no backend
```

Swagger com todos os endpoints:
```
http://localhost:8080/swagger-ui/index.html
```

---

## Parar e reiniciar

```bash
# Parar tudo
docker compose --profile ngrok down

# Subir tudo de novo
docker compose pull backend
docker compose up -d
docker compose --profile ngrok up -d ngrok
```

---

## Se precisar rebuildar a imagem (após mudar código)

```bash
docker compose build backend && docker compose up -d backend
```

Depois de rebuildar, publique no Docker Hub para os outros devs:

```bash
docker push napolitech/backend-napolitech-dev:1.1.0
docker push napolitech/backend-napolitech-dev:latest
```

---

## Arquitetura dos serviços

| Container | Porta | O que é |
|---|---|---|
| `backend-napolitech` | 8080 | Spring Boot — API REST |
| `mysql-napolitech` | 3306 | Banco de dados |
| `redis` | 6379 | Cache de produtos |
| `rabbitmq-napolitech` | 5672 / 15672 | Fila de pedidos |
| `ollama` | 11434 | IA local (upsell no checkout) |
| `redisinsight` | 5540 | Interface visual do Redis |
| `ngrok-napolitech` | 4040 | Túnel HTTPS (profile ngrok) |

---

## Problema: CORS bloqueado no browser

Se o frontend no Netlify mostrar erro de CORS, o frontend precisa enviar o header abaixo em **todas** as requisições:

```
ngrok-skip-browser-warning: true
```

Isso é uma limitação do ngrok — ele exibe uma página de aviso para browsers que não inclui os headers CORS. O backend está correto.

---

## Problema: tokens inválidos após reiniciar

O `JWT_SECRET` no `.env` deve ser o mesmo valor fixo fornecido pelo Alejandro. Se estiver diferente, os tokens gerados em uma sessão não funcionam em outra.

---

## Referências

- [prompts/frontend-integration-agent.json](prompts/frontend-integration-agent.json) — contrato completo da API para o agente do frontend
- [prompts/fix-cors-ngrok-warning.json](prompts/fix-cors-ngrok-warning.json) — fix específico do erro de CORS do ngrok
- [NGROK_SETUP.md](NGROK_SETUP.md) — documentação detalhada do ngrok
- Docker Hub: `napolitech/backend-napolitech-dev:1.1.0`




JWT_SECRET=e7c96235d3f2fdf3dc5cbf3033e4dd2321a80eb16699101cafc39276d20e2f55



NGROK_AUTHTOKEN=31LeTTBVo1nSF7PJ4fYc5xXK9Tf_6L5s8V2xVWsiSi3T8qRE8


front end :::

EXPO_PUBLIC_API_BASE_URL=https://internation-sully-kolten.ngrok-free.dev/api
