# 📦 Documentação - Tela de Controle de Estoque

## 📋 Índice
1. [Visão Geral](#visão-geral)
2. [Autenticação](#autenticação)
3. [Endpoints Disponíveis](#endpoints-disponíveis)
4. [Estrutura de Dados](#estrutura-de-dados)
5. [Fluxo de Implementação](#fluxo-de-implementação)
6. [Exemplos de Código](#exemplos-de-código)
7. [Tratamento de Erros](#tratamento-de-erros)
8. [Paginação](#paginação)

---

## 🎯 Visão Geral

A tela de controle de estoque permitirá gerenciar todos os produtos da pizzaria Napolitech, incluindo:
- Listar todos os produtos com paginação
- Visualizar detalhes de um produto específico
- Cadastrar novos produtos
- Deletar produtos existentes

**Base URL:** `{{baseUrl}}/api/produtos`

> **⚠️ Importante:** Todos os endpoints requerem autenticação via token JWT, exceto onde indicado.

---

## 🔐 Autenticação

### 1. Login (Obter Token)

Antes de fazer qualquer requisição aos endpoints de produtos, você precisa obter um token JWT.

**Endpoint:** `POST {{baseUrl}}/api/login`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "email": "marcos@gmail.com",
  "senha": "P@ssw0rd"
}
```

**Resposta de Sucesso (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "usuario": {
    "id": 1,
    "nome": "Marcos Paulo",
    "email": "marcos@gmail.com"
  }
}
```

**Como Usar o Token:**

Após obter o token, você deve incluí-lo em todas as requisições subsequentes no header `Authorization`:

```
Authorization: Bearer {token}
```

### Exemplo com Axios:
```javascript
// 1. Fazer login e obter o token
const login = async (email, senha) => {
  try {
    const response = await axios.post(`${baseUrl}/api/login`, {
      email,
      senha
    });
    
    // Salvar o token (localStorage, sessionStorage, ou context)
    localStorage.setItem('token', response.data.token);
    return response.data;
  } catch (error) {
    console.error('Erro no login:', error.response?.data);
    throw error;
  }
};

// 2. Configurar axios para usar o token automaticamente
const api = axios.create({
  baseURL: baseUrl
});

// Interceptor para adicionar o token em todas as requisições
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
```

---

## 🔌 Endpoints Disponíveis

### 1. 📝 Listar Todos os Produtos (com Paginação)

Retorna uma lista paginada de todos os produtos cadastrados no sistema.

**Endpoint:** `GET /api/produtos`

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters (Opcionais):**
| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `page` | number | 0 | Número da página (começa em 0) |
| `size` | number | 10 | Quantidade de itens por página |
| `sort` | string | id,DESC | Campo e direção de ordenação |

**Exemplos de URLs:**
```
GET /api/produtos
GET /api/produtos?page=0&size=20
GET /api/produtos?page=1&size=10&sort=nome,ASC
GET /api/produtos?page=0&size=15&sort=preco,DESC
```

**Resposta de Sucesso (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "nome": "Pizza de Calabresa",
      "preco": 45.90,
      "quantidadeEstoque": 25,
      "ingredientes": "Calabresa, queijo, molho de tomate, orégano",
      "categoriaProduto": "PIZZA"
    },
    {
      "id": 2,
      "nome": "Pizza de Mussarela",
      "preco": 42.90,
      "quantidadeEstoque": 30,
      "ingredientes": "Mussarela, molho de tomate, orégano",
      "categoriaProduto": "PIZZA"
    },
    {
      "id": 3,
      "nome": "Coca-Cola 2L",
      "preco": 12.00,
      "quantidadeEstoque": 50,
      "ingredientes": "Refrigerante",
      "categoriaProduto": "BEBIDAS"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 3,
  "totalElements": 25,
  "last": false,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 10,
  "first": true,
  "empty": false
}
```

---

### 2. 🔍 Buscar Produto por ID

Retorna os detalhes completos de um produto específico.

**Endpoint:** `GET /api/produtos/{id}`

**Headers:**
```
Authorization: Bearer {token}
```

**Parâmetros de URL:**
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | number | ID do produto a ser buscado |

**Exemplo:**
```
GET /api/produtos/1
```

**Resposta de Sucesso (200 OK):**
```json
{
  "produto": {
    "id": 1,
    "nome": "Pizza de Calabresa",
    "preco": 45.90,
    "quantidadeEstoque": 25,
    "ingredientes": "Calabresa, queijo, molho de tomate, orégano",
    "categoriaProduto": "PIZZA"
  }
}
```

**Resposta de Erro (404 NOT FOUND):**
```json
{
  "erro": "Produto não encontrado!"
}
```

---

### 3. ➕ Cadastrar Novo Produto

Registra um novo produto no sistema de estoque.

**Endpoint:** `POST /api/produtos`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**Body:**
```json
{
  "nome": "Pizza Margherita",
  "preco": 48.90,
  "quantidade": 20,
  "ingredientes": "Mussarela de búfala, tomate, manjericão, azeite",
  "categoriaProduto": "PIZZA"
}
```

**Campos Obrigatórios:**
| Campo | Tipo | Descrição | Exemplo |
|-------|------|-----------|---------|
| `nome` | string | Nome do produto | "Pizza Margherita" |
| `preco` | number | Preço do produto | 48.90 |
| `quantidade` | number | Quantidade em estoque | 20 |
| `ingredientes` | string | Ingredientes do produto | "Mussarela, tomate..." |
| `categoriaProduto` | enum | Categoria do produto | "PIZZA" |

**Categorias Disponíveis:**
- `PIZZA`
- `PORCAO`
- `SOBREMESA`
- `PIZZA_DOCE`
- `ESFIHA`
- `ESFIHA_DOCE`
- `BEBIDAS`

**Resposta de Sucesso (201 CREATED):**
```json
{
  "produtoId": 15,
  "nome": "Pizza Margherita",
  "preco": 48.90,
  "quantidade": 20,
  "ingredientes": "Mussarela de búfala, tomate, manjericão, azeite",
  "categoriaProduto": "PIZZA",
  "mensagem": "Produto cadastrado com sucesso!"
}
```

**Resposta de Erro (400 BAD REQUEST):**
```json
{
  "erro": "Erro ao cadastrar produto: Campo nome é obrigatório"
}
```

---

### 4. 🗑️ Deletar Produto

Remove um produto do sistema pelo ID.

**Endpoint:** `DELETE /api/produtos/{id}`

**Headers:**
```
Authorization: Bearer {token}
```

**Parâmetros de URL:**
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | number | ID do produto a ser deletado |

**Exemplo:**
```
DELETE /api/produtos/15
```

**Resposta de Sucesso (200 OK):**
```json
{
  "mensagem": "Produto deletado com sucesso!"
}
```

**Resposta de Erro (404 NOT FOUND):**
```json
{
  "erro": "Produto não encontrado!"
}
```

---

## 📊 Estrutura de Dados

### Produto (Entidade Completa)

```typescript
interface Produto {
  id: number;
  nome: string;
  preco: number;
  quantidadeEstoque: number;
  ingredientes: string;
  categoriaProduto: CategoriaProduto;
}
```

### CategoriaProduto (Enum)

```typescript
enum CategoriaProduto {
  PIZZA = 'PIZZA',
  PORCAO = 'PORCAO',
  SOBREMESA = 'SOBREMESA',
  PIZZA_DOCE = 'PIZZA_DOCE',
  ESFIHA = 'ESFIHA',
  ESFIHA_DOCE = 'ESFIHA_DOCE',
  BEBIDAS = 'BEBIDAS'
}
```

### Resposta Paginada

```typescript
interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: Sort;
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  size: number;
  number: number;
  sort: Sort;
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}

interface Sort {
  sorted: boolean;
  unsorted: boolean;
  empty: boolean;
}
```

---

## 🚀 Fluxo de Implementação

### Passo 1: Configurar a Autenticação

1. Criar uma tela de login
2. Implementar função de login que obtém o token
3. Armazenar o token (localStorage/sessionStorage)
4. Configurar interceptor do axios para adicionar o token automaticamente

### Passo 2: Criar o Serviço de API

Criar um arquivo `produtoService.js` ou `produtoService.ts`:

```javascript
import axios from 'axios';

const baseUrl = 'http://localhost:8080'; // Ajustar conforme ambiente

const api = axios.create({
  baseURL: baseUrl,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para adicionar token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const produtoService = {
  // Listar produtos com paginação
  listarProdutos: (page = 0, size = 10, sort = 'id,DESC') => {
    return api.get('/api/produtos', {
      params: { page, size, sort }
    });
  },

  // Buscar produto por ID
  buscarPorId: (id) => {
    return api.get(`/api/produtos/${id}`);
  },

  // Cadastrar novo produto
  cadastrar: (produto) => {
    return api.post('/api/produtos', produto);
  },

  // Deletar produto
  deletar: (id) => {
    return api.delete(`/api/produtos/${id}`);
  }
};
```

### Passo 3: Criar a Interface da Tela

A tela deve conter:

1. **Tabela de Produtos:**
   - Colunas: ID, Nome, Preço, Quantidade em Estoque, Categoria, Ações
   - Paginação (anterior, próximo, seleção de página)
   - Opção de ordenação por coluna

2. **Barra de Ações:**
   - Botão "Adicionar Produto"
   - Campo de busca (opcional, para filtrar)
   - Filtro por categoria

3. **Formulário de Cadastro/Edição:**
   - Modal ou página separada
   - Campos: nome, preço, quantidade, ingredientes, categoria
   - Validação de campos obrigatórios

4. **Indicadores:**
   - Total de produtos
   - Produtos com estoque baixo (opcional)
   - Total por categoria (opcional)

### Passo 4: Implementar Funcionalidades

```javascript
// Exemplo de componente React (adaptável para Vue, Angular, etc.)
import React, { useState, useEffect } from 'react';
import { produtoService } from './services/produtoService';

const ControleEstoque = () => {
  const [produtos, setProdutos] = useState([]);
  const [paginacao, setPaginacao] = useState({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0
  });
  const [loading, setLoading] = useState(false);

  // Carregar produtos
  const carregarProdutos = async (page = 0, size = 10) => {
    setLoading(true);
    try {
      const response = await produtoService.listarProdutos(page, size);
      setProdutos(response.data.content);
      setPaginacao({
        page: response.data.number,
        size: response.data.size,
        totalPages: response.data.totalPages,
        totalElements: response.data.totalElements
      });
    } catch (error) {
      console.error('Erro ao carregar produtos:', error);
      alert('Erro ao carregar produtos!');
    } finally {
      setLoading(false);
    }
  };

  // Carregar ao montar o componente
  useEffect(() => {
    carregarProdutos();
  }, []);

  // Função para deletar produto
  const deletarProduto = async (id) => {
    if (!confirm('Deseja realmente deletar este produto?')) return;
    
    try {
      await produtoService.deletar(id);
      alert('Produto deletado com sucesso!');
      carregarProdutos(paginacao.page, paginacao.size);
    } catch (error) {
      console.error('Erro ao deletar produto:', error);
      alert(error.response?.data?.erro || 'Erro ao deletar produto!');
    }
  };

  // Render da tabela (simplificado)
  return (
    <div>
      <h1>Controle de Estoque</h1>
      
      {loading ? (
        <p>Carregando...</p>
      ) : (
        <>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Preço</th>
                <th>Estoque</th>
                <th>Categoria</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {produtos.map(produto => (
                <tr key={produto.id}>
                  <td>{produto.id}</td>
                  <td>{produto.nome}</td>
                  <td>R$ {produto.preco.toFixed(2)}</td>
                  <td>{produto.quantidadeEstoque}</td>
                  <td>{produto.categoriaProduto}</td>
                  <td>
                    <button onClick={() => deletarProduto(produto.id)}>
                      Deletar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Paginação */}
          <div>
            <button 
              disabled={paginacao.page === 0}
              onClick={() => carregarProdutos(paginacao.page - 1, paginacao.size)}
            >
              Anterior
            </button>
            <span>
              Página {paginacao.page + 1} de {paginacao.totalPages}
            </span>
            <button 
              disabled={paginacao.page >= paginacao.totalPages - 1}
              onClick={() => carregarProdutos(paginacao.page + 1, paginacao.size)}
            >
              Próxima
            </button>
          </div>
        </>
      )}
    </div>
  );
};
```

---

## 🎨 Exemplos de Código

### Exemplo 1: Formulário de Cadastro de Produto

```javascript
const FormularioProduto = ({ onSucesso }) => {
  const [formData, setFormData] = useState({
    nome: '',
    preco: '',
    quantidade: '',
    ingredientes: '',
    categoriaProduto: 'PIZZA'
  });

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      // Validação básica
      if (!formData.nome || !formData.preco || !formData.quantidade) {
        alert('Preencha todos os campos obrigatórios!');
        return;
      }

      const produtoData = {
        nome: formData.nome,
        preco: parseFloat(formData.preco),
        quantidade: parseInt(formData.quantidade),
        ingredientes: formData.ingredientes,
        categoriaProduto: formData.categoriaProduto
      };

      const response = await produtoService.cadastrar(produtoData);
      alert(response.data.mensagem);
      
      // Limpar formulário
      setFormData({
        nome: '',
        preco: '',
        quantidade: '',
        ingredientes: '',
        categoriaProduto: 'PIZZA'
      });

      // Callback de sucesso
      if (onSucesso) onSucesso();

    } catch (error) {
      console.error('Erro ao cadastrar:', error);
      alert(error.response?.data?.erro || 'Erro ao cadastrar produto!');
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>Nome do Produto *</label>
        <input
          type="text"
          name="nome"
          value={formData.nome}
          onChange={handleChange}
          required
        />
      </div>

      <div>
        <label>Preço (R$) *</label>
        <input
          type="number"
          name="preco"
          step="0.01"
          min="0"
          value={formData.preco}
          onChange={handleChange}
          required
        />
      </div>

      <div>
        <label>Quantidade em Estoque *</label>
        <input
          type="number"
          name="quantidade"
          min="0"
          value={formData.quantidade}
          onChange={handleChange}
          required
        />
      </div>

      <div>
        <label>Ingredientes</label>
        <textarea
          name="ingredientes"
          value={formData.ingredientes}
          onChange={handleChange}
          rows="3"
        />
      </div>

      <div>
        <label>Categoria *</label>
        <select
          name="categoriaProduto"
          value={formData.categoriaProduto}
          onChange={handleChange}
          required
        >
          <option value="PIZZA">Pizza</option>
          <option value="PORCAO">Porção</option>
          <option value="SOBREMESA">Sobremesa</option>
          <option value="PIZZA_DOCE">Pizza Doce</option>
          <option value="ESFIHA">Esfiha</option>
          <option value="ESFIHA_DOCE">Esfiha Doce</option>
          <option value="BEBIDAS">Bebidas</option>
        </select>
      </div>

      <button type="submit">Cadastrar Produto</button>
    </form>
  );
};
```

### Exemplo 2: Visualizar Detalhes do Produto

```javascript
const DetalhesProduto = ({ produtoId }) => {
  const [produto, setProduto] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const carregarProduto = async () => {
      try {
        const response = await produtoService.buscarPorId(produtoId);
        setProduto(response.data.produto);
      } catch (error) {
        console.error('Erro ao carregar produto:', error);
        alert('Erro ao carregar detalhes do produto!');
      } finally {
        setLoading(false);
      }
    };

    carregarProduto();
  }, [produtoId]);

  if (loading) return <p>Carregando...</p>;
  if (!produto) return <p>Produto não encontrado!</p>;

  return (
    <div className="detalhes-produto">
      <h2>{produto.nome}</h2>
      <div className="info-grupo">
        <strong>ID:</strong> {produto.id}
      </div>
      <div className="info-grupo">
        <strong>Preço:</strong> R$ {produto.preco.toFixed(2)}
      </div>
      <div className="info-grupo">
        <strong>Quantidade em Estoque:</strong> {produto.quantidadeEstoque}
      </div>
      <div className="info-grupo">
        <strong>Categoria:</strong> {produto.categoriaProduto}
      </div>
      <div className="info-grupo">
        <strong>Ingredientes:</strong> {produto.ingredientes}
      </div>
    </div>
  );
};
```

### Exemplo 3: Filtro por Categoria

```javascript
const FiltroCategoria = ({ onFiltrar }) => {
  const categorias = [
    { valor: 'TODOS', label: 'Todas as Categorias' },
    { valor: 'PIZZA', label: 'Pizza' },
    { valor: 'PORCAO', label: 'Porção' },
    { valor: 'SOBREMESA', label: 'Sobremesa' },
    { valor: 'PIZZA_DOCE', label: 'Pizza Doce' },
    { valor: 'ESFIHA', label: 'Esfiha' },
    { valor: 'ESFIHA_DOCE', label: 'Esfiha Doce' },
    { valor: 'BEBIDAS', label: 'Bebidas' }
  ];

  const handleChange = (e) => {
    const categoria = e.target.value;
    onFiltrar(categoria === 'TODOS' ? null : categoria);
  };

  return (
    <div className="filtro-categoria">
      <label>Filtrar por Categoria:</label>
      <select onChange={handleChange}>
        {categorias.map(cat => (
          <option key={cat.valor} value={cat.valor}>
            {cat.label}
          </option>
        ))}
      </select>
    </div>
  );
};
```

---

## ⚠️ Tratamento de Erros

### Erros Comuns e Como Tratá-los

#### 1. Erro 401 - Não Autorizado

**Causa:** Token inválido, expirado ou ausente.

**Solução:**
```javascript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token inválido ou expirado
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

#### 2. Erro 404 - Não Encontrado

**Causa:** Produto com o ID especificado não existe.

**Tratamento:**
```javascript
const buscarProduto = async (id) => {
  try {
    const response = await produtoService.buscarPorId(id);
    return response.data.produto;
  } catch (error) {
    if (error.response?.status === 404) {
      alert('Produto não encontrado!');
    } else {
      alert('Erro ao buscar produto!');
    }
    return null;
  }
};
```

#### 3. Erro 400 - Requisição Inválida

**Causa:** Dados enviados estão incorretos ou faltando campos obrigatórios.

**Tratamento:**
```javascript
const cadastrarProduto = async (produto) => {
  try {
    const response = await produtoService.cadastrar(produto);
    return response.data;
  } catch (error) {
    if (error.response?.status === 400) {
      const mensagemErro = error.response.data.erro;
      alert(`Dados inválidos: ${mensagemErro}`);
    } else {
      alert('Erro ao cadastrar produto!');
    }
    throw error;
  }
};
```

#### 4. Erro 500 - Erro Interno do Servidor

**Causa:** Problema no servidor backend.

**Tratamento:**
```javascript
const handleErro = (error) => {
  if (error.response?.status === 500) {
    alert('Erro no servidor. Por favor, tente novamente mais tarde.');
    console.error('Erro 500:', error);
  } else {
    alert('Erro inesperado. Por favor, contate o suporte.');
  }
};
```

### Sistema de Notificações Recomendado

```javascript
// Usar biblioteca de toast notifications (react-toastify, etc.)
import { toast } from 'react-toastify';

const exibirNotificacao = (tipo, mensagem) => {
  switch(tipo) {
    case 'sucesso':
      toast.success(mensagem);
      break;
    case 'erro':
      toast.error(mensagem);
      break;
    case 'aviso':
      toast.warning(mensagem);
      break;
    case 'info':
      toast.info(mensagem);
      break;
  }
};

// Uso:
try {
  await produtoService.cadastrar(produto);
  exibirNotificacao('sucesso', 'Produto cadastrado com sucesso!');
} catch (error) {
  exibirNotificacao('erro', error.response?.data?.erro || 'Erro ao cadastrar');
}
```

---

## 📄 Paginação

### Entendendo a Paginação

A API retorna os produtos de forma paginada. Aqui está como trabalhar com a paginação:

#### Parâmetros de Paginação

- **page**: Número da página (começa em 0)
- **size**: Quantidade de itens por página
- **sort**: Campo e direção de ordenação (ex: `nome,ASC` ou `preco,DESC`)

#### Exemplo de Componente de Paginação Completo

```javascript
const Paginacao = ({ paginaAtual, totalPaginas, onMudarPagina }) => {
  const gerarPaginas = () => {
    const paginas = [];
    const maxPaginasVisiveis = 5;
    
    let inicio = Math.max(0, paginaAtual - Math.floor(maxPaginasVisiveis / 2));
    let fim = Math.min(totalPaginas, inicio + maxPaginasVisiveis);
    
    if (fim - inicio < maxPaginasVisiveis) {
      inicio = Math.max(0, fim - maxPaginasVisiveis);
    }
    
    for (let i = inicio; i < fim; i++) {
      paginas.push(i);
    }
    
    return paginas;
  };

  return (
    <div className="paginacao">
      <button 
        onClick={() => onMudarPagina(0)}
        disabled={paginaAtual === 0}
      >
        Primeira
      </button>
      
      <button 
        onClick={() => onMudarPagina(paginaAtual - 1)}
        disabled={paginaAtual === 0}
      >
        Anterior
      </button>
      
      {gerarPaginas().map(pagina => (
        <button
          key={pagina}
          onClick={() => onMudarPagina(pagina)}
          className={paginaAtual === pagina ? 'ativo' : ''}
        >
          {pagina + 1}
        </button>
      ))}
      
      <button 
        onClick={() => onMudarPagina(paginaAtual + 1)}
        disabled={paginaAtual >= totalPaginas - 1}
      >
        Próxima
      </button>
      
      <button 
        onClick={() => onMudarPagina(totalPaginas - 1)}
        disabled={paginaAtual >= totalPaginas - 1}
      >
        Última
      </button>
      
      <span className="info-paginacao">
        Página {paginaAtual + 1} de {totalPaginas}
      </span>
    </div>
  );
};
```

#### Seletor de Itens por Página

```javascript
const SeletorTamanhoPagina = ({ tamanhoAtual, onMudar }) => {
  const opcoes = [5, 10, 20, 50];

  return (
    <div className="seletor-tamanho">
      <label>Itens por página:</label>
      <select 
        value={tamanhoAtual} 
        onChange={(e) => onMudar(parseInt(e.target.value))}
      >
        {opcoes.map(opcao => (
          <option key={opcao} value={opcao}>
            {opcao}
          </option>
        ))}
      </select>
    </div>
  );
};
```

---

## 🎯 Checklist de Implementação

Use este checklist para garantir que implementou todas as funcionalidades:

### Autenticação
- [ ] Tela de login implementada
- [ ] Token JWT sendo salvo após login
- [ ] Token sendo enviado em todas as requisições
- [ ] Interceptor configurado para adicionar token automaticamente
- [ ] Redirecionamento para login quando token expira (401)

### Listagem de Produtos
- [ ] Tabela exibindo todos os produtos
- [ ] Paginação funcionando (anterior, próximo)
- [ ] Seletor de quantidade de itens por página
- [ ] Loading state durante carregamento
- [ ] Mensagem quando não há produtos

### Visualização de Detalhes
- [ ] Modal/página de detalhes do produto
- [ ] Exibindo todos os campos do produto

### Cadastro de Produtos
- [ ] Formulário com todos os campos obrigatórios
- [ ] Validação de campos no front-end
- [ ] Seletor de categoria funcional
- [ ] Feedback visual de sucesso/erro
- [ ] Limpeza do formulário após cadastro bem-sucedido

### Exclusão de Produtos
- [ ] Botão de deletar em cada produto
- [ ] Confirmação antes de deletar
- [ ] Feedback visual após exclusão
- [ ] Atualização da lista após exclusão

### Tratamento de Erros
- [ ] Mensagens de erro claras para o usuário
- [ ] Tratamento de erro 401 (não autorizado)
- [ ] Tratamento de erro 404 (não encontrado)
- [ ] Tratamento de erro 400 (requisição inválida)
- [ ] Tratamento de erro 500 (erro do servidor)

### UX/UI
- [ ] Interface responsiva
- [ ] Loading states visíveis
- [ ] Feedback visual para ações do usuário
- [ ] Mensagens de confirmação
- [ ] Design consistente

---

## 🔧 Variáveis de Ambiente

Configure as seguintes variáveis de ambiente no seu projeto front-end:

```env
# Desenvolvimento
REACT_APP_API_BASE_URL=http://localhost:8080

# Produção
REACT_APP_API_BASE_URL=https://api.napolitech.com.br
```

**Uso no código:**
```javascript
const baseUrl = process.env.REACT_APP_API_BASE_URL;
```

---

## 📱 Exemplo de Tela Completa (React)

```javascript
import React, { useState, useEffect } from 'react';
import { produtoService } from './services/produtoService';
import './ControleEstoque.css';

const ControleEstoque = () => {
  // Estados
  const [produtos, setProdutos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [paginacao, setPaginacao] = useState({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0
  });
  const [modalAberto, setModalAberto] = useState(false);
  const [produtoSelecionado, setProdutoSelecionado] = useState(null);

  // Carregar produtos
  const carregarProdutos = async (page = 0, size = 10) => {
    setLoading(true);
    try {
      const response = await produtoService.listarProdutos(page, size);
      setProdutos(response.data.content);
      setPaginacao({
        page: response.data.number,
        size: response.data.size,
        totalPages: response.data.totalPages,
        totalElements: response.data.totalElements
      });
    } catch (error) {
      console.error('Erro ao carregar produtos:', error);
      alert('Erro ao carregar produtos!');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    carregarProdutos();
  }, []);

  // Deletar produto
  const deletarProduto = async (id) => {
    if (!window.confirm('Deseja realmente deletar este produto?')) return;
    
    try {
      await produtoService.deletar(id);
      alert('Produto deletado com sucesso!');
      carregarProdutos(paginacao.page, paginacao.size);
    } catch (error) {
      alert(error.response?.data?.erro || 'Erro ao deletar produto!');
    }
  };

  // Visualizar detalhes
  const visualizarDetalhes = async (id) => {
    try {
      const response = await produtoService.buscarPorId(id);
      setProdutoSelecionado(response.data.produto);
      setModalAberto(true);
    } catch (error) {
      alert('Erro ao carregar detalhes do produto!');
    }
  };

  // Render
  return (
    <div className="controle-estoque">
      <header className="header">
        <h1>Controle de Estoque</h1>
        <button 
          className="btn-adicionar"
          onClick={() => {/* Abrir formulário de cadastro */}}
        >
          + Adicionar Produto
        </button>
      </header>

      <div className="info-totais">
        <div className="card-info">
          <h3>Total de Produtos</h3>
          <p className="numero">{paginacao.totalElements}</p>
        </div>
      </div>

      {loading ? (
        <div className="loading">Carregando...</div>
      ) : (
        <>
          <div className="tabela-container">
            <table className="tabela-produtos">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nome</th>
                  <th>Preço</th>
                  <th>Estoque</th>
                  <th>Categoria</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {produtos.map(produto => (
                  <tr key={produto.id}>
                    <td>{produto.id}</td>
                    <td>{produto.nome}</td>
                    <td>R$ {produto.preco.toFixed(2)}</td>
                    <td>
                      <span className={produto.quantidadeEstoque < 10 ? 'estoque-baixo' : ''}>
                        {produto.quantidadeEstoque}
                      </span>
                    </td>
                    <td>{produto.categoriaProduto}</td>
                    <td className="acoes">
                      <button 
                        className="btn-visualizar"
                        onClick={() => visualizarDetalhes(produto.id)}
                      >
                        Ver
                      </button>
                      <button 
                        className="btn-deletar"
                        onClick={() => deletarProduto(produto.id)}
                      >
                        Deletar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="paginacao">
            <button 
              disabled={paginacao.page === 0}
              onClick={() => carregarProdutos(paginacao.page - 1, paginacao.size)}
            >
              Anterior
            </button>
            <span>
              Página {paginacao.page + 1} de {paginacao.totalPages}
            </span>
            <button 
              disabled={paginacao.page >= paginacao.totalPages - 1}
              onClick={() => carregarProdutos(paginacao.page + 1, paginacao.size)}
            >
              Próxima
            </button>
          </div>
        </>
      )}

      {/* Modal de Detalhes */}
      {modalAberto && produtoSelecionado && (
        <div className="modal-overlay" onClick={() => setModalAberto(false)}>
          <div className="modal-conteudo" onClick={(e) => e.stopPropagation()}>
            <button 
              className="btn-fechar"
              onClick={() => setModalAberto(false)}
            >
              ×
            </button>
            <h2>{produtoSelecionado.nome}</h2>
            <div className="detalhes">
              <p><strong>ID:</strong> {produtoSelecionado.id}</p>
              <p><strong>Preço:</strong> R$ {produtoSelecionado.preco.toFixed(2)}</p>
              <p><strong>Estoque:</strong> {produtoSelecionado.quantidadeEstoque}</p>
              <p><strong>Categoria:</strong> {produtoSelecionado.categoriaProduto}</p>
              <p><strong>Ingredientes:</strong> {produtoSelecionado.ingredientes}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ControleEstoque;
```

---

## 🎨 Exemplo de CSS

```css
/* ControleEstoque.css */

.controle-estoque {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h1 {
  color: #333;
  font-size: 28px;
}

.btn-adicionar {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-adicionar:hover {
  background-color: #45a049;
}

.info-totais {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.card-info {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  flex: 1;
}

.card-info h3 {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.card-info .numero {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.loading {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #666;
}

.tabela-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.tabela-produtos {
  width: 100%;
  border-collapse: collapse;
}

.tabela-produtos thead {
  background-color: #f5f5f5;
}

.tabela-produtos th {
  padding: 15px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #ddd;
}

.tabela-produtos td {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.tabela-produtos tbody tr:hover {
  background-color: #f9f9f9;
}

.estoque-baixo {
  color: #f44336;
  font-weight: bold;
}

.acoes {
  display: flex;
  gap: 10px;
}

.btn-visualizar,
.btn-deletar {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: opacity 0.3s;
}

.btn-visualizar {
  background-color: #2196F3;
  color: white;
}

.btn-visualizar:hover {
  opacity: 0.8;
}

.btn-deletar {
  background-color: #f44336;
  color: white;
}

.btn-deletar:hover {
  opacity: 0.8;
}

.paginacao {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
  padding: 20px;
}

.paginacao button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.paginacao button:hover:not(:disabled) {
  background-color: #f5f5f5;
}

.paginacao button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-conteudo {
  background: white;
  padding: 30px;
  border-radius: 8px;
  min-width: 400px;
  max-width: 600px;
  position: relative;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.btn-fechar {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.btn-fechar:hover {
  color: #333;
}

.detalhes p {
  margin: 10px 0;
  font-size: 16px;
}

.detalhes strong {
  color: #666;
}
```

---

## 📞 Suporte

Em caso de dúvidas ou problemas:

1. **Verifique a documentação da API no Swagger:**
   ```
   http://localhost:8080/swagger-ui.html
   ```

2. **Consulte os logs do backend** para erros detalhados

3. **Verifique se o token JWT está válido** e não expirou

4. **Confirme que o backend está rodando** na URL configurada

---

## ✅ Conclusão

Esta documentação fornece tudo o que é necessário para implementar a tela de controle de estoque:

- ✅ Todos os endpoints documentados
- ✅ Exemplos de código completos
- ✅ Tratamento de erros
- ✅ Paginação
- ✅ Autenticação
- ✅ Interface de usuário sugerida

Siga os exemplos e adapte conforme o framework que estiver usando (React, Vue, Angular, etc.). A estrutura da API é consistente e RESTful, facilitando a integração.

**Boa sorte com a implementação! 🚀**
