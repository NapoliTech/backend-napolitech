# Napolitech Backend — Guia ngrok + Expo

Guia completo para rodar o backend localmente, expô-lo via ngrok e apontar o front Expo/React Native para ele sem editar código.

---

## Pré-requisitos

| Ferramenta | Versão mínima | Para quê |
|---|---|---|
| Docker Desktop | 25+ | Todos os serviços |
| Docker Compose | V2 (`docker compose`) | Orquestração |
| ngrok | Conta do dev principal | Túnel HTTPS com domínio fixo |
| Java 21 + Maven | Opcional | Build local sem Docker |

---

## Setup para novo desenvolvedor (clone do zero)

> A imagem Docker já está publicada no Docker Hub — não é necessário buildar.

```bash
# 1. Clonar e entrar na branch
git clone <url-do-repo>
cd backend-napolitech
git checkout feat/ai-upsell-ollama

# 2. Criar o .env a partir do exemplo
cp .env.example .env
```

Edite o `.env` e preencha **obrigatoriamente**:

```env
JWT_SECRET=e7c96235d3f2fdf3dc5cbf3033e4dd2321a80eb16699101cafc39276d20e2f55

NGROK_AUTHTOKEN=<token fornecido pelo dev principal>

FRONTEND_ALLOWED_ORIGINS=https://bonaripizzaria.netlify.app,https://internation-sully-kolten.ngrok-free.dev,http://localhost:3000,http://localhost:8081,http://localhost:19000,http://localhost:19006,http://10.0.2.2:8081
```

> **Importante:** o `NGROK_AUTHTOKEN` e o `JWT_SECRET` devem ser os mesmos do dev principal para que o domínio fixo `internation-sully-kolten.ngrok-free.dev` funcione e os tokens JWT sejam compatíveis.

```bash
# 3. Baixar a imagem do Docker Hub (sem rebuild)
docker compose pull backend

# 4. Subir todos os serviços
docker compose up -d

# 5. Subir o ngrok com domínio fixo
docker compose --profile ngrok up -d ngrok

# 6. Baixar o modelo de IA (primeira vez, opcional)
docker exec ollama ollama pull llama3.2:1b
```

Pronto. Backend disponível em `https://internation-sully-kolten.ngrok-free.dev`.

---

## 1. Configuração inicial (uma única vez)

### 1.1 Crie o arquivo .env

```bash
cp .env.example .env
```

Edite `.env` e preencha **obrigatoriamente**:

```env
# JWT fixo para não invalidar tokens ao reiniciar
JWT_SECRET=gere-com-openssl-rand-hex-32-resultado-aqui

# ngrok (se for usar o serviço integrado no docker-compose)
NGROK_AUTHTOKEN=seu-token-de-https://dashboard.ngrok.com
```

### 1.2 Gere um JWT_SECRET seguro

```bash
# No terminal (macOS/Linux/Git Bash)
openssl rand -hex 32
# Exemplo de saída: a3f8c2d1e4b7098765432100fedcba9876543210abcdef1234567890abcdef12
```

Cole o resultado em `JWT_SECRET` no `.env`.

---

## 2. Subir o backend localmente (sem ngrok)

```bash
# Sobe todos os serviços (backend, MySQL, RabbitMQ, Redis, Ollama)
docker compose up -d

# Acompanhar logs do backend
docker compose logs -f backend

# Verificar saúde dos serviços
docker compose ps
```

Backend disponível em: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`  
RabbitMQ dashboard: `http://localhost:15672` (admin / napolitech)  
Redis Insight: `http://localhost:5540`

### Baixar o modelo Ollama (primeira vez)

```bash
docker exec ollama ollama pull llama3.2
# Para CPU mais rápido (modelo menor, mesma qualidade para upsell)
docker exec ollama ollama pull llama3.2:1b
```

---

## 3. Expor o backend via ngrok

### Opção A — ngrok como serviço Docker (recomendado)

O serviço `ngrok` está definido no `docker-compose.yml` com o profile `ngrok`. Ative assim:

```bash
# Necessário: NGROK_AUTHTOKEN no .env
docker compose --profile ngrok up -d

# Ver URL pública gerada
docker compose logs ngrok
# Ou acesse o painel em: http://localhost:4040
```

A URL pública terá o formato: `https://abc123.ngrok-free.app`

### Opção B — ngrok instalado localmente

```bash
# Autentique uma única vez
ngrok authtoken SEU_TOKEN

# Exponha a porta 8080
ngrok http 8080
```

Copie a URL `https://` exibida no terminal.

---

## 4. Atualizar CORS para aceitar o ngrok

Depois de obter a URL do ngrok (ex: `https://abc123.ngrok-free.app`), atualize **duas** variáveis:

### No .env (para o backend):

```env
# Adicione a URL do ngrok à lista existente
FRONTEND_ALLOWED_ORIGINS=https://abc123.ngrok-free.app,http://localhost:19006,http://localhost:8081
```

Reinicie o backend para aplicar:

```bash
docker compose restart backend
```

**Alternativa sem reiniciar:** use o wildcard de domínio ngrok, que já funciona sem update:

```env
FRONTEND_ALLOWED_ORIGINS=https://*.ngrok-free.app,http://localhost:19006,http://localhost:8081
```

---

## 5. Apontar o front Expo para o backend

O front lê a URL base de `EXPO_PUBLIC_API_BASE_URL`. **Nunca altere o código** — só a variável de ambiente.

### No projeto Expo (arquivo `.env` ou `.env.local`):

```env
# URL do ngrok + /api obrigatório
EXPO_PUBLIC_API_BASE_URL=https://abc123.ngrok-free.app/api
```

### Reiniciar o Expo após mudar a variável:

```bash
# Limpa o cache do Metro e reinicia
npx expo start --clear
```

> **Nota ngrok gratuito:** a URL muda a cada nova sessão do ngrok. Atualize `EXPO_PUBLIC_API_BASE_URL` e `FRONTEND_ALLOWED_ORIGINS` a cada vez. Com um plano pago (domínio estático), a URL é fixa.

---

## 6. Contratos de API

Todos os endpoints estão sob o prefixo `/api`. O front deve usar `EXPO_PUBLIC_API_BASE_URL` que já inclui `/api`.

| Área | Endpoint | Método | Auth |
|---|---|---|---|
| Cadastro | `/api/cadastro` | POST | Público |
| Login | `/api/login` | POST | Público |
| Usuários | `/api/{id}` | GET/PUT/DELETE | Bearer JWT |
| Produtos | `/api/produtos` | GET/POST/DELETE | Bearer JWT |
| Pedidos | `/api/pedidos` | GET/POST | Bearer JWT |
| Pedido status | `/api/pedidos/{id}/status` | PUT | Bearer JWT |
| Endereços | `/api/enderecos` | GET/POST/PUT/DELETE | Bearer JWT |
| Reset senha | `/api/password-reset/request` | POST | Público |
| Upsell IA | `/api/upsell/{clienteId}` | POST | Bearer JWT |
| Dashboard | `/api/dashboard/kpis` | GET | Bearer JWT |

O JWT deve ser enviado no header: `Authorization: Bearer <token>`

---

## 7. Checklist de validação

Execute na ordem após subir tudo:

- [ ] `docker compose ps` — todos os serviços `healthy`
- [ ] `curl http://localhost:8080/api/login -X POST` — retorna 400 (não 404, não 503)
- [ ] Swagger abre em `http://localhost:8080/swagger-ui/index.html`
- [ ] ngrok URL responde: `curl https://abc123.ngrok-free.app/api/login -X POST`
- [ ] Front faz login sem erro de CORS (inspecione o console do Expo)
- [ ] Header `Access-Control-Allow-Origin` presente na resposta OPTIONS
- [ ] Redis: `docker exec redis redis-cli ping` → `PONG`
- [ ] RabbitMQ: criar um pedido e verificar o log do consumer
- [ ] Upsell: `POST /api/upsell/{id}` retorna sugestões (ou fallback se Ollama não tiver modelo)

---

## 8. Variáveis de ambiente — referência rápida

| Variável | Onde usar | Descrição |
|---|---|---|
| `JWT_SECRET` | `.env` backend | Secret JWT, mín. 32 chars |
| `FRONTEND_ALLOWED_ORIGINS` | `.env` backend | Origens CORS separadas por vírgula |
| `FRONTEND_BASE_URL` | `.env` backend | URL base do front (links de email) |
| `NGROK_AUTHTOKEN` | `.env` backend | Token do ngrok (docker profile) |
| `EXPO_PUBLIC_API_BASE_URL` | `.env` frontend | URL base da API com `/api` |

---

## 9. Problemas comuns

### CORS bloqueando o front

1. Verifique se a URL do ngrok está em `FRONTEND_ALLOWED_ORIGINS`
2. Reinicie o backend após alterar a variável
3. Confirme que `EXPO_PUBLIC_API_BASE_URL` termina em `/api` (sem barra final)
4. Inspecione os headers da requisição OPTIONS: deve ter `Access-Control-Allow-Origin`

### Tokens invalidados ao reiniciar

Defina `JWT_SECRET` com valor fixo no `.env`. Sem ele, o backend gera uma chave efêmera e todos os tokens expiram ao reiniciar.

### Ollama retornando erro 500 no upsell

O serviço tem fallback automático — vai retornar sugestões baseadas em regras. Para ativar a IA, baixe o modelo:

```bash
docker exec ollama ollama pull llama3.2:1b
```

### ngrok "tunnel session failed"

Verifique `NGROK_AUTHTOKEN` no `.env`. Token incorreto ou conta sem sessão ativa.

### Backend não sobe (erro de healthcheck)

MySQL demora ~30s para ficar pronto. Aguarde e verifique:

```bash
docker compose logs db | tail -20
docker compose logs backend | tail -30
```
