# Upsell com IA — Guia de Integracao para o Front-end React Native

## O que e essa feature

Na tela de checkout, antes do cliente confirmar o pedido, o backend consulta um modelo de IA local (Ollama + llama3.2) que analisa:

- Os produtos que estao no carrinho do cliente
- O historico real de pedidos anteriores desse cliente (banco de dados)
- As combinacoes de produtos mais pedidas juntas na pizzaria (banco de dados)

Com base nisso, retorna ate 3 sugestoes personalizadas com um motivo gerado pela IA para cada uma.

Se a IA falhar por qualquer motivo, o backend retorna automaticamente um fallback com produtos complementares — o front nunca recebe erro.

---

## Endpoint

```
POST /api/upsell/{clienteId}
```

| Campo | Valor |
|-------|-------|
| Metodo | `POST` |
| Autenticacao | `Authorization: Bearer <token_jwt>` |
| Content-Type | `application/json` |
| Parametro de rota | `clienteId` — o `id` retornado no login |

### Request Body

```json
{
  "produtosIds": [1, 3]
}
```

| Campo | Tipo | Descricao |
|-------|------|-----------|
| `produtosIds` | `number[]` | IDs dos produtos atualmente no carrinho. Pode ser `[]` se o carrinho estiver vazio. |

### Response 200

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

| Campo | Tipo | Descricao |
|-------|------|-----------|
| `id` | `number` | ID do produto — use para adicionar ao pedido |
| `nome` | `string` | Nome do produto |
| `preco` | `number` | Preco atual do produto (sempre vem do banco, nunca da IA) |
| `categoriaProduto` | `string` | `PIZZA` `PIZZA_DOCE` `BEBIDAS` `SOBREMESA` `PORCAO` `ESFIHA` `ESFIHA_DOCE` |
| `motivo` | `string` | Texto personalizado gerado pela IA — use como legenda do card |

Retorna `[]` se nao houver sugestoes. **Nunca retorna erro 5xx.**

---

## Quando chamar

Chame assim que o usuario **abre a tela de checkout**, em paralelo com o carregamento dos outros dados da tela. Nao espere o usuario fazer nada — as sugestoes ja devem estar prontas quando ele rolar a tela.

```
Usuario clica em "Ir para o Checkout"
        |
        +---> monta a tela de checkout          (sincrono)
        +---> POST /api/upsell/{clienteId}       (async, em paralelo)
                    |
                    enquanto carrega: exibe skeleton/spinner nos cards
                    quando chega: exibe os cards de sugestao
```

---

## Tipos TypeScript

```typescript
// types/upsell.ts

export type CategoriaProduto =
  | 'PIZZA'
  | 'PIZZA_DOCE'
  | 'BEBIDAS'
  | 'SOBREMESA'
  | 'PORCAO'
  | 'ESFIHA'
  | 'ESFIHA_DOCE';

export interface UpsellSugestao {
  id: number;
  nome: string;
  preco: number;
  categoriaProduto: CategoriaProduto;
  motivo: string;
}

export interface UpsellRequest {
  produtosIds: number[];
}
```

---

## Service

```typescript
// services/upsellService.ts

import { UpsellSugestao, UpsellRequest } from '../types/upsell';

const BASE_URL = 'http://10.0.2.2:8080'; // Android emulator
// const BASE_URL = 'http://localhost:8080'; // iOS simulator
// const BASE_URL = 'https://api.suaempresa.com'; // producao

export const buscarSugestoesUpsell = async (
  clienteId: number,
  produtosIds: number[],
  token: string
): Promise<UpsellSugestao[]> => {
  try {
    const body: UpsellRequest = { produtosIds };

    const response = await fetch(`${BASE_URL}/api/upsell/${clienteId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) return [];

    const data: UpsellSugestao[] = await response.json();
    return data;
  } catch {
    return []; // nunca quebra o checkout
  }
};
```

---

## Hook

```typescript
// hooks/useUpsell.ts

import { useState, useEffect } from 'react';
import { UpsellSugestao } from '../types/upsell';
import { buscarSugestoesUpsell } from '../services/upsellService';

export const useUpsell = (
  clienteId: number,
  produtosIds: number[],
  token: string
) => {
  const [sugestoes, setSugestoes] = useState<UpsellSugestao[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    buscarSugestoesUpsell(clienteId, produtosIds, token)
      .then(setSugestoes)
      .finally(() => setLoading(false));
  }, []);

  return { sugestoes, loading };
};
```

---

## Componente de Card

```tsx
// components/UpsellCard.tsx

import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { UpsellSugestao } from '../types/upsell';

interface Props {
  sugestao: UpsellSugestao;
  adicionado: boolean;
  onPress: (sugestao: UpsellSugestao) => void;
}

export const UpsellCard = ({ sugestao, adicionado, onPress }: Props) => (
  <TouchableOpacity
    style={[styles.card, adicionado && styles.cardAtivo]}
    onPress={() => onPress(sugestao)}
    activeOpacity={0.8}
  >
    <Text style={styles.nome} numberOfLines={1}>{sugestao.nome}</Text>
    <Text style={styles.motivo} numberOfLines={2}>{sugestao.motivo}</Text>
    <View style={styles.rodape}>
      <Text style={styles.preco}>R$ {sugestao.preco.toFixed(2)}</Text>
      <Text style={[styles.botao, adicionado && styles.botaoAtivo]}>
        {adicionado ? '✓ Adicionado' : '+ Adicionar'}
      </Text>
    </View>
  </TouchableOpacity>
);

const styles = StyleSheet.create({
  card: {
    width: 160,
    padding: 12,
    marginRight: 10,
    borderRadius: 10,
    borderWidth: 1.5,
    borderColor: '#e0e0e0',
    backgroundColor: '#fff',
  },
  cardAtivo: {
    borderColor: '#E63946',
    backgroundColor: '#fff5f5',
  },
  nome: {
    fontWeight: 'bold',
    fontSize: 14,
    color: '#1a1a1a',
  },
  motivo: {
    fontSize: 11,
    color: '#777',
    marginVertical: 6,
    lineHeight: 15,
  },
  rodape: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 4,
  },
  preco: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#E63946',
  },
  botao: {
    fontSize: 11,
    color: '#E63946',
    fontWeight: '600',
  },
  botaoAtivo: {
    color: '#2a9d8f',
  },
});
```

---

## Componente de Secao Upsell

```tsx
// components/UpsellSecao.tsx

import React from 'react';
import {
  View, Text, FlatList, ActivityIndicator, StyleSheet
} from 'react-native';
import { UpsellSugestao } from '../types/upsell';
import { UpsellCard } from './UpsellCard';

interface Props {
  sugestoes: UpsellSugestao[];
  loading: boolean;
  adicionados: number[];
  onAdicionar: (sugestao: UpsellSugestao) => void;
}

export const UpsellSecao = ({ sugestoes, loading, adicionados, onAdicionar }: Props) => {
  if (!loading && sugestoes.length === 0) return null;

  return (
    <View style={styles.container}>
      <Text style={styles.titulo}>Que tal adicionar?</Text>
      <Text style={styles.subtitulo}>Sugerido especialmente para voce</Text>

      {loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="small" color="#E63946" />
          <Text style={styles.loadingTexto}>Preparando sugestoes...</Text>
        </View>
      ) : (
        <FlatList
          data={sugestoes}
          horizontal
          showsHorizontalScrollIndicator={false}
          keyExtractor={item => String(item.id)}
          renderItem={({ item }) => (
            <UpsellCard
              sugestao={item}
              adicionado={adicionados.includes(item.id)}
              onPress={onAdicionar}
            />
          )}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginVertical: 16,
  },
  titulo: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1a1a1a',
    marginBottom: 2,
  },
  subtitulo: {
    fontSize: 12,
    color: '#999',
    marginBottom: 12,
  },
  loadingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingVertical: 12,
  },
  loadingTexto: {
    fontSize: 13,
    color: '#999',
  },
});
```

---

## Tela de Checkout completa

```tsx
// screens/CheckoutScreen.tsx

import React, { useState } from 'react';
import {
  View, Text, ScrollView, TouchableOpacity, StyleSheet, Alert
} from 'react-native';
import { useUpsell } from '../hooks/useUpsell';
import { UpsellSecao } from '../components/UpsellSecao';
import { UpsellSugestao } from '../types/upsell';

interface ItemCarrinho {
  produtoId: number;
  nome: string;
  preco: number;
  quantidade: number;
  tamanhoPizza: string;
  bordaRecheada: string;
}

interface Props {
  route: {
    params: {
      carrinho: ItemCarrinho[];
      clienteId: number;
      enderecoId: number;
      token: string;
    };
  };
  navigation: any;
}

export default function CheckoutScreen({ route, navigation }: Props) {
  const { carrinho, clienteId, enderecoId, token } = route.params;

  const produtosIds = carrinho.map(i => i.produtoId);
  const { sugestoes, loading } = useUpsell(clienteId, produtosIds, token);

  const [extras, setExtras] = useState<UpsellSugestao[]>([]);

  const toggleExtra = (sugestao: UpsellSugestao) => {
    setExtras(prev =>
      prev.find(i => i.id === sugestao.id)
        ? prev.filter(i => i.id !== sugestao.id)
        : [...prev, sugestao]
    );
  };

  const calcularTotal = () => {
    const totalCarrinho = carrinho.reduce(
      (acc, item) => acc + item.preco * item.quantidade, 0
    );
    const totalExtras = extras.reduce((acc, item) => acc + item.preco, 0);
    return (totalCarrinho + totalExtras).toFixed(2);
  };

  const confirmarPedido = async () => {
    // Monta os itens do carrinho original
    const itensCarrinho = carrinho.map(item => ({
      produtosIds: [item.produtoId],
      quantidade: item.quantidade,
      tamanhoPizza: item.tamanhoPizza,
      bordaRecheada: item.bordaRecheada,
    }));

    // Adiciona os extras aceitos do upsell
    const itensExtras = extras.map(extra => ({
      produtosIds: [extra.id],
      quantidade: 1,
      tamanhoPizza: 'GRANDE',
      bordaRecheada: 'NORMAL',
    }));

    const body = {
      clienteId,
      enderecoId,
      tipoEntrega: 'DELIVERY',
      itens: [...itensCarrinho, ...itensExtras],
    };

    try {
      const response = await fetch('http://10.0.2.2:8080/api/pedidos', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(body),
      });

      const data = await response.json();

      if (response.ok) {
        navigation.navigate('Confirmacao', { pedido: data });
      } else {
        Alert.alert('Erro', data.erro || 'Nao foi possivel realizar o pedido.');
      }
    } catch {
      Alert.alert('Erro', 'Sem conexao com o servidor.');
    }
  };

  return (
    <ScrollView style={styles.container}>

      {/* Resumo do carrinho */}
      <Text style={styles.secaoTitulo}>Seu pedido</Text>
      {carrinho.map(item => (
        <View key={item.produtoId} style={styles.itemLinha}>
          <Text style={styles.itemNome}>{item.quantidade}x {item.nome}</Text>
          <Text style={styles.itemPreco}>R$ {(item.preco * item.quantidade).toFixed(2)}</Text>
        </View>
      ))}

      {/* Extras aceitos do upsell */}
      {extras.map(extra => (
        <View key={extra.id} style={styles.itemLinha}>
          <Text style={[styles.itemNome, styles.itemExtra]}>1x {extra.nome} (adicionado)</Text>
          <Text style={styles.itemPreco}>R$ {extra.preco.toFixed(2)}</Text>
        </View>
      ))}

      {/* Secao de upsell da IA */}
      <UpsellSecao
        sugestoes={sugestoes}
        loading={loading}
        adicionados={extras.map(e => e.id)}
        onAdicionar={toggleExtra}
      />

      {/* Total e botao */}
      <View style={styles.totalContainer}>
        <Text style={styles.totalTexto}>Total</Text>
        <Text style={styles.totalValor}>R$ {calcularTotal()}</Text>
      </View>

      <TouchableOpacity style={styles.botao} onPress={confirmarPedido}>
        <Text style={styles.botaoTexto}>Confirmar Pedido</Text>
      </TouchableOpacity>

    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff', padding: 16 },
  secaoTitulo: { fontSize: 18, fontWeight: 'bold', marginBottom: 12 },
  itemLinha: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 6,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  itemNome: { fontSize: 14, color: '#333' },
  itemExtra: { color: '#2a9d8f' },
  itemPreco: { fontSize: 14, fontWeight: '600' },
  totalContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 16,
    marginTop: 8,
    borderTopWidth: 2,
    borderTopColor: '#1a1a1a',
  },
  totalTexto: { fontSize: 18, fontWeight: 'bold' },
  totalValor: { fontSize: 18, fontWeight: 'bold', color: '#E63946' },
  botao: {
    backgroundColor: '#E63946',
    padding: 16,
    borderRadius: 10,
    alignItems: 'center',
    marginBottom: 32,
  },
  botaoTexto: { color: '#fff', fontSize: 16, fontWeight: 'bold' },
});
```

---

## Estrutura de arquivos sugerida

```
src/
├── types/
│   └── upsell.ts              (interfaces TypeScript)
├── services/
│   └── upsellService.ts       (chamada à API)
├── hooks/
│   └── useUpsell.ts           (hook com estado de loading)
├── components/
│   ├── UpsellCard.tsx         (card individual de sugestao)
│   └── UpsellSecao.tsx        (secao completa com lista horizontal)
└── screens/
    └── CheckoutScreen.tsx     (tela de checkout com tudo integrado)
```

---

## Comportamentos esperados

| Situacao | O que acontece |
|----------|---------------|
| IA gera sugestoes | `motivo` e personalizado, ex: *"Voce costuma pedir com bebida!"* |
| IA falha / Ollama offline | Fallback automatico: `motivo` generico, ex: *"Aproveite para adicionar!"* |
| Cliente sem historico | IA sugere com base nas combinacoes populares da pizzaria |
| Todos os produtos ja no carrinho | Retorna `[]`, secao nao aparece na tela |
| Erro de rede no front | `buscarSugestoesUpsell` retorna `[]`, secao nao aparece |

---

## Pontos de atencao

**Nao bloqueie o checkout pela IA.** O `useUpsell` e async — o usuario pode confirmar o pedido mesmo com o loading em andamento. Se as sugestoes chegarem tarde, simplesmente nao aparecem.

**Preco vem sempre do banco.** O campo `preco` na resposta e sempre o valor real cadastrado no banco de dados. Nunca confie em preco calculado no front — use o returno da API.

**`tamanhoPizza` dos extras.** Quando o usuario aceitar uma sugestao de pizza do upsell, pergunte o tamanho antes de adicionar ao pedido. Para bebidas e sobremesas, `GRANDE` e `NORMAL` sao os valores padrao adequados.
