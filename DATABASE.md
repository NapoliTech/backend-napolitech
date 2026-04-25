# Napolitech — Documentação do Banco de Dados

MySQL 8.0 rodando em Docker. Database: `pizzaria_db`.

---

## Conexão

| Parâmetro | Valor |
|---|---|
| Host | `localhost` (externo) / `db` (dentro da rede Docker) |
| Porta | `3306` |
| Database | `pizzaria_db` |
| Usuário | `napolitech` |
| Senha | `napolitech_dev` |
| Root senha | `root` |

**Acesso direto via terminal:**
```bash
docker exec -it mysql-napolitech mysql -u napolitech -pnapolitech_dev pizzaria_db
```

**String JDBC:**
```
jdbc:mysql://localhost:3306/pizzaria_db?useSSL=false&allowPublicKeyRetrieval=true
```

---

## Diagrama de Relacionamentos

```
usuarios_cadastrados (1) ──── (1) endereco
usuarios_cadastrados (1) ──── (N) pedidos
pedidos              (1) ──── (N) item_pedido
estoque_produtos     (1) ──── (N) item_pedido
usuarios_cadastrados (1) ──── (1) password_reset_token
```

---

## Tabelas

### `usuarios_cadastrados`
Usuários do sistema.

| Coluna | Tipo | Obrigatório | Observação |
|---|---|---|---|
| `id_usuario` | BIGINT PK AUTO_INCREMENT | — | Gerado automaticamente |
| `nome` | VARCHAR(255) | Sim | |
| `email` | VARCHAR(255) | Sim | Único |
| `senha` | VARCHAR(255) | Sim | **Hash BCrypt — nunca texto plano** |
| `cpf` | VARCHAR(255) | Sim | |
| `telefone` | VARCHAR(255) | Sim | |
| `pedidos` | BIGINT | Sim | Contador, inicia em `0` |
| `data_nasc` | VARCHAR(255) | Sim | Formato `dd/MM/yyyy` |
| `tipo_usuario` | VARCHAR(255) | Sim | `CLIENTE` \| `ATENDENTE` \| `ADMIN` |
| `id_usuario` (FK) | BIGINT | Não | FK → `endereco.id_usuario` |

```sql
INSERT INTO usuarios_cadastrados
  (nome, email, senha, cpf, telefone, pedidos, data_nasc, tipo_usuario)
VALUES
  ('João Silva', 'joao@email.com', '$2a$10$HASH_BCRYPT', '123.456.789-00', '11999999999', 0, '15/03/1990', 'CLIENTE');
```

---

### `endereco`
Endereço vinculado a um usuário (OneToOne).

| Coluna | Tipo | Obrigatório | Observação |
|---|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | — | |
| `rua` | VARCHAR(255) | Sim | |
| `bairro` | VARCHAR(255) | Sim | |
| `numero` | INT | Sim | |
| `complemento` | VARCHAR(255) | Não | |
| `cidade` | VARCHAR(255) | Sim | |
| `estado` | VARCHAR(255) | Sim | Sigla, ex: `SP` |
| `cep` | VARCHAR(255) | Sim | Ex: `01001-000` |
| `id_usuario` (FK) | BIGINT | Não | FK → `usuarios_cadastrados.id_usuario` |

```sql
INSERT INTO endereco (rua, bairro, numero, complemento, cidade, estado, cep)
VALUES ('Rua das Flores', 'Centro', 123, 'Apto 4', 'São Paulo', 'SP', '01310-100');
```

---

### `estoque_produtos`
Catálogo de produtos.

| Coluna | Tipo | Obrigatório | Observação |
|---|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | — | |
| `nome` | VARCHAR(255) | Sim | Único na prática |
| `preco` | DOUBLE | Sim | |
| `quantidade_estoque` | INT | Sim | |
| `ingredientes` | VARCHAR(255) | Não | |
| `categoria_produto` | VARCHAR(255) | Sim | Enum como String (ver abaixo) |

**Valores válidos para `categoria_produto`:**
`PIZZA` | `PIZZA_DOCE` | `BEBIDAS` | `SOBREMESA` | `PORCAO` | `ESFIHA` | `ESFIHA_DOCE`

```sql
INSERT INTO estoque_produtos (nome, preco, quantidade_estoque, ingredientes, categoria_produto)
VALUES ('Pizza de Frango com Catupiry', 44.90, 10, 'Frango, Catupiry, Mussarela', 'PIZZA');
```

> O mock popula esta tabela automaticamente no boot com o cardápio completo.

---

### `pedidos`
Pedidos realizados.

| Coluna | Tipo | Obrigatório | Observação |
|---|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | — | |
| `cliente_id` | BIGINT FK | Sim | FK → `usuarios_cadastrados.id_usuario` |
| `endereco_id` | BIGINT FK | Sim | FK → `endereco.id` |
| `nome_cliente` | VARCHAR(255) | Não | |
| `status_pedido` | VARCHAR(255) | Sim | Enum como String (ver abaixo) |
| `preco_total` | DOUBLE | Não | Soma dos itens |
| `observacao` | VARCHAR(255) | Não | |
| `tipo_entrega` | VARCHAR(255) | Não | Enum como String (ver abaixo) |
| `data_pedido` | DATETIME | Não | |

**Valores válidos para `status_pedido`:**
`RECEBIDO` | `EM_PREPARO` | `ENTREGUE` | `ENCERRADO` | `CANCELADO`

**Valores válidos para `tipo_entrega`:**
`RETIRADA` | `DELIVERY` | `ENCOMENDA`

```sql
INSERT INTO pedidos (cliente_id, endereco_id, nome_cliente, status_pedido, preco_total, tipo_entrega, data_pedido)
VALUES (1, 1, 'João Silva', 'RECEBIDO', 89.80, 'DELIVERY', NOW());
```

---

### `item_pedido`
Itens de cada pedido.

| Coluna | Tipo | Obrigatório | Observação |
|---|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | — | |
| `pedido_id` | BIGINT FK | Sim | FK → `pedidos.id` |
| `produto_id` | BIGINT FK | Não | FK → `estoque_produtos.id` |
| `quantidade` | INT | Não | |
| `tamanho_pizza` | VARCHAR(255) | Não | Só para pizzas |
| `borda_recheada` | VARCHAR(255) | Não | |
| `preco_total` | DOUBLE | Não | Calculado pelo backend |

**Valores válidos para `tamanho_pizza`:**
`BROTO` | `GRANDE` | `TREM` | `MEIO_A_MEIO`

**Valores válidos para `borda_recheada`:**

| Valor | Adicional |
|---|---|
| `NORMAL` | +R$0,00 |
| `CHEDDAR` | +R$8,00 |
| `CATUPIRY` | +R$8,00 |
| `CHOCOLATE` | +R$10,00 |

```sql
INSERT INTO item_pedido (pedido_id, produto_id, quantidade, tamanho_pizza, borda_recheada, preco_total)
VALUES (1, 3, 1, 'GRANDE', 'CATUPIRY', 52.90);
```

---

### `password_reset_token`
Tokens temporários de reset de senha.

| Coluna | Tipo | Observação |
|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | |
| `token` | VARCHAR(255) | UUID único |
| `user_id` | BIGINT FK | FK → `usuarios_cadastrados.id_usuario` |
| `expiry_date` | DATETIME | Normalmente NOW() + 1 hora |

> Gerado automaticamente pela API — não precisa inserir manualmente.

---

## Ordem correta para INSERTs

Para não violar chaves estrangeiras, sempre inserir nesta ordem:

```
1. endereco
2. usuarios_cadastrados  (FK → endereco)
3. estoque_produtos
4. pedidos               (FK → usuarios_cadastrados, endereco)
5. item_pedido           (FK → pedidos, estoque_produtos)
```

---

## Queries úteis

```sql
-- Listar pedidos recentes com cliente
SELECT p.id, p.nome_cliente, p.status_pedido, p.preco_total, p.data_pedido
FROM pedidos p
JOIN usuarios_cadastrados u ON p.cliente_id = u.id_usuario
ORDER BY p.data_pedido DESC LIMIT 20;

-- Itens de um pedido específico
SELECT ep.nome, ip.quantidade, ip.tamanho_pizza, ip.borda_recheada, ip.preco_total
FROM item_pedido ip
JOIN estoque_produtos ep ON ip.produto_id = ep.id
WHERE ip.pedido_id = 1;

-- Faturamento total (excluindo cancelados)
SELECT SUM(preco_total) AS faturamento FROM pedidos
WHERE status_pedido NOT IN ('CANCELADO');

-- Produtos mais vendidos
SELECT ep.nome, SUM(ip.quantidade) AS total_vendido
FROM item_pedido ip
JOIN estoque_produtos ep ON ip.produto_id = ep.id
GROUP BY ep.nome ORDER BY total_vendido DESC LIMIT 10;

-- Pedidos por status
SELECT status_pedido, COUNT(*) AS total FROM pedidos GROUP BY status_pedido;
```

---

## Avisos importantes

- **Senha sempre BCrypt** — nunca inserir texto plano em `usuarios_cadastrados.senha`
- **Hibernate ddl-auto=update** — novas colunas nas entidades Java são criadas automaticamente no próximo boot
- **Não renomear tabelas/colunas no banco** sem alterar as entidades Java correspondentes
- Dados inseridos diretamente no banco **bypassam validações do Spring** — garantir consistência manualmente
