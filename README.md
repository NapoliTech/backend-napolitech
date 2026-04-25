## Back-End Napolitech 🎯

Bem-vindo ao motor da aplicação **Napolitech**, desenvolvido com **Spring Boot** e alimentado pelo banco de dados **MySQL**! 🚀  
Este projeto é totalmente containerizado com **Docker**, garantindo uma execução simples e eficiente. 🐳

---

### 🔧 Pré-requisitos

Antes de começar, certifique-se de ter os seguintes itens instalados no seu sistema:

- **Docker** 🐳
- **Docker Compose** ⚙️
- **Maven** 📦

---

### 🚀 Como executar o projeto

#### 1️⃣ Clone o repositório 📂
```bash
git clone https://github.com/seu-usuario/back-end-napolitech.git
cd back-end-napolitech
```

#### 2️⃣ Gere o artefato JAR com Maven 🏗️
Antes de subir os containers, é necessário gerar o JAR do projeto:
```bash
mvn clean package
```

> ⚠️ Este passo é importante para que a aplicação dentro do container tenha o arquivo JAR pronto para execução.

#### 3️⃣ Construa e inicie os containers com Docker Compose 🐳
```bash
docker-compose up --build
```

#### 4️⃣ Acesse a aplicação 🌐
A API estará disponível na porta **8080**! 🎯  
[http://localhost:8080](http://localhost:8080)

---

### 🛠️ Tecnologias utilizadas

- **Spring Boot** 3.4.2 🚀
- **MySQL** 8.0 🐳
- **Docker** ⚙️
- **Docker Compose** 🔁
- **Maven** 📦

---

### 🐳 Configuração do Docker

#### ⚙️ Dockerfile
O **Dockerfile** cria uma imagem baseada no **OpenJDK 21**, instala o **Maven** e empacota o código fonte.

#### ⚙️ docker-compose.yml
Define dois serviços principais:

- **backend** 🖥️ → A aplicação Java que se comunica com o banco de dados.
- **db** 🗄️ → Um container **MySQL** que hospeda o banco `pizzaria_db`.

> ✅ A network utilizada é `network-napolitech`.

---

### 🔐 Variáveis de ambiente

As credenciais do banco de dados e outras configurações já estão configuradas no `docker-compose.yml`.  
Os valores padrão são os seguintes:

```dotenv
# Spring Boot
SPRING_PROFILES_ACTIVE=prod
SPRING_APPLICATION_NAME=back-end-napolitech

# Banco de dados
DATASOURCE_URL=jdbc:mysql://db:3306/pizzaria_db?createDatabaseIfNotExist=true
DATASOURCE_USERNAME=napolitech
DATASOURCE_PASSWORD=napolitech_dev
DATASOURCE_DRIVER=com.mysql.cj.jdbc.Driver

# H2 (dev)
H2_CONSOLE_ENABLED=true
SQL_PLATFORM=h2
JPA_DATABASE_PLATFORM=org.hibernate.dialect.H2Dialect

# JPA / Hibernate
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect

# Servidor
SERVER_PORT=8080
SERVER_ADDRESS=0.0.0.0
PUBLIC_API_BASE_URL=http://localhost:8080/api
FRONTEND_ALLOWED_ORIGINS=http://localhost:19006
JWT_SECRET=defina-uma-chave-com-32-caracteres-ou-mais

# Logging
LOGGING_SQL=DEBUG
LOGGING_BINDER=TRACE

# OAuth2 / Email (opcional)
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

# Redis
REDIS_URL=

# RabbitMQ
RABBITMQ_HOST=
RABBITMQ_PORT=
RABBITMQ_USERNAME=
RABBITMQ_PASSWORD=

# Llama Agent
LLAMA_API_URL=
LLAMA_API_KEY=
```

> 💡 Todos esses valores podem ser sobrescritos via variáveis de ambiente do sistema ou no `docker-compose.yml`.

---

### 🧠 Redis / Cache

O Redis é utilizado apenas para cache de leitura em produtos:

| Cache | Chave | TTL | Comportamento |
|-------|-------|-----|---------------|
| `produtoPorId` | `app::produtoPorId::{id}` | 10 min | Cache por ID de produto |
| `listaProdutos` | `app::listaProdutos::{page}-{size}-{sort}` | 5 min | Cache de listagem paginada |

TTL padrão: **5 minutos**. As entradas são invalidadas quando um produto é criado ou removido.

---

### 🐰 RabbitMQ

Eventos de pedidos sao publicados de forma assíncrona para evitar bloqueio da API.

| Recurso | Nome | Observação |
|---------|------|-----------|
| Exchange | `pedidos.exchange` | Exchange principal (direct) |
| Queue | `pedidos.queue` | Recebe eventos de pedido criado |
| Routing Key | `pedidos.v1.pedido-criado` | Chave de roteamento principal |
| DLQ Exchange | `pedidos.exchange.dlq` | Dead-letter para falhas |
| DLQ Queue | `pedidos.queue.dlq` | Mensagens que falham após retries |
| DLQ Routing Key | `pedidos.v1.pedido-criado.dlq` | Chave do DLQ |

O listener possui retry habilitado e, em caso de falha, a mensagem é direcionada ao DLQ para análise.

---

### 🤖 Agente Llama

O backend expõe `POST /api/llama` como proxy para o agente configurado em `LLAMA_API_URL`.
O payload enviado pelo cliente é repassado integralmente ao agente, com `Authorization: Bearer <LLAMA_API_KEY>` se a chave estiver definida.
Se `LLAMA_API_URL` não estiver configurada, o endpoint responde `503` sem afetar o fluxo principal da API.
