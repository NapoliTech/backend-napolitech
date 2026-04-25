package com.pizzaria.backendpizzaria.service.Upsell;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaria.backendpizzaria.domain.DTO.Upsell.UpsellSugestaoDTO;
import com.pizzaria.backendpizzaria.domain.Enum.CategoriaProduto;
import com.pizzaria.backendpizzaria.domain.ItemPedido;
import com.pizzaria.backendpizzaria.domain.Pedido;
import com.pizzaria.backendpizzaria.domain.Produto;
import com.pizzaria.backendpizzaria.domain.Usuario;
import com.pizzaria.backendpizzaria.repository.ItemPedidoRepository;
import com.pizzaria.backendpizzaria.repository.PedidoRepository;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UpsellService {

    private static final Logger log = LoggerFactory.getLogger(UpsellService.class);

    private final ChatClient chatClient;
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public UpsellService(ChatClient.Builder chatClientBuilder,
                         PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         ItemPedidoRepository itemPedidoRepository,
                         UsuarioRepository usuarioRepository,
                         ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<UpsellSugestaoDTO> gerarSugestoes(Long clienteId, List<Long> produtosIdsNoCarrinho) {
        List<Long> carrinhoIds = produtosIdsNoCarrinho != null ? produtosIdsNoCarrinho : List.of();

        // 1. Busca todos os produtos do cardápio
        List<Produto> todosProdutos = produtoRepository.findAll();

        // 2. Filtra produtos disponíveis para sugerir (não no carrinho e com estoque)
        List<Produto> produtosSugeriveis = todosProdutos.stream()
                .filter(p -> !carrinhoIds.contains(p.getId()) && p.getQuantidadeEstoque() > 0)
                .toList();

        if (produtosSugeriveis.isEmpty()) {
            return List.of();
        }

        // 3. Busca histórico do cliente (últimos 5 pedidos com itens)
        List<Pedido> historico = pedidoRepository.findByClienteIdUsuario(clienteId)
                .stream()
                .sorted(Comparator.comparing(Pedido::getDataPedido).reversed())
                .limit(5)
                .toList();

        // 4. Busca nome do cliente
        String nomeCliente = usuarioRepository.findById(clienteId)
                .map(Usuario::getNome)
                .orElse("cliente");

        // 5. Busca combinações populares com os itens do carrinho (somente se carrinho não vazio)
        List<Produto> combinacoesPopulares = List.of();
        if (!carrinhoIds.isEmpty()) {
            List<Object> rawIds = itemPedidoRepository.findTopCombinacoesIds(carrinhoIds);
            Set<Long> idsNoCarrinho = new HashSet<>(carrinhoIds);
            combinacoesPopulares = rawIds.stream()
                    .map(o -> ((Number) o).longValue())
                    .filter(id -> !idsNoCarrinho.contains(id))
                    .map(id -> todosProdutos.stream().filter(p -> p.getId().equals(id)).findFirst())
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .limit(3)
                    .toList();
        }

        // 6. Monta o prompt e chama a IA via Ollama
        try {
            String prompt = buildPrompt(nomeCliente, historico, carrinhoIds, todosProdutos, produtosSugeriveis, combinacoesPopulares);
            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            List<UpsellSugestaoDTO> sugestoes = parseAiResponse(aiResponse, produtosSugeriveis);
            if (!sugestoes.isEmpty()) {
                return sugestoes;
            }
        } catch (Exception e) {
            log.warn("Falha na chamada à IA para upsell (clienteId={}): {}. Usando fallback.", clienteId, e.getMessage());
        }

        // 7. Fallback: retorna sugestões baseadas em combinações populares ou categorias complementares
        return gerarFallback(produtosSugeriveis, combinacoesPopulares, todosProdutos, carrinhoIds);
    }

    private String buildPrompt(String nomeCliente, List<Pedido> historico,
                                List<Long> carrinhoIds, List<Produto> todosProdutos,
                                List<Produto> produtosSugeriveis, List<Produto> combinacoesPopulares) {

        String carrinhoDesc = todosProdutos.stream()
                .filter(p -> carrinhoIds.contains(p.getId()))
                .map(p -> String.format("%s (R$%.2f)", p.getNome(), p.getPreco()))
                .collect(Collectors.joining(", "));
        if (carrinhoDesc.isBlank()) carrinhoDesc = "Carrinho vazio";

        String historicoDesc = historico.isEmpty() ? "Nenhum pedido anterior encontrado" :
                historico.stream()
                        .flatMap(p -> p.getItens().stream())
                        .map(ItemPedido::getProduto)
                        .filter(Objects::nonNull)
                        .map(Produto::getNome)
                        .distinct()
                        .limit(10)
                        .collect(Collectors.joining(", "));

        String combinacoesDesc = combinacoesPopulares.isEmpty()
                ? "Dados insuficientes para combinações"
                : combinacoesPopulares.stream()
                        .map(p -> String.format("%s (R$%.2f)", p.getNome(), p.getPreco()))
                        .collect(Collectors.joining(", "));

        String disponiveisDesc = produtosSugeriveis.stream()
                .map(p -> String.format("[id:%d] %s - R$%.2f (%s)",
                        p.getId(), p.getNome(), p.getPreco(), p.getCategoriaProduto()))
                .collect(Collectors.joining("\n"));

        return """
                Você é um assistente de vendas de uma pizzaria chamada Napolitech. Analise os dados abaixo e sugira até 3 itens de upsell personalizados para exibir no checkout.

                CLIENTE: %s

                CARRINHO ATUAL:
                %s

                HISTÓRICO DE PEDIDOS DO CLIENTE (itens já pedidos antes):
                %s

                COMBINAÇÕES POPULARES COM OS ITENS DO CARRINHO (baseado em pedidos reais da pizzaria):
                %s

                PRODUTOS DISPONÍVEIS PARA SUGERIR (use APENAS estes IDs):
                %s

                REGRAS:
                - Sugira no máximo 3 produtos usando SOMENTE os IDs da lista acima
                - Priorize combinações populares que já aparecem juntas em pedidos reais
                - Para carrinho com pizzas, prefira bebidas e sobremesas como complemento
                - O motivo deve ser curto, pessoal e convincente (máximo 60 caracteres)
                - Não repita produtos que já estão no carrinho

                Retorne APENAS um array JSON válido, sem texto adicional, sem markdown, no formato:
                [{"id": 123, "motivo": "motivo personalizado aqui"}]
                """.formatted(nomeCliente, carrinhoDesc, historicoDesc, combinacoesDesc, disponiveisDesc);
    }

    private List<UpsellSugestaoDTO> parseAiResponse(String aiResponse, List<Produto> produtosSugeriveis) {
        try {
            String json = extractJson(aiResponse);
            JsonNode nodes = objectMapper.readTree(json);

            Map<Long, Produto> disponiveisPorId = produtosSugeriveis.stream()
                    .collect(Collectors.toMap(Produto::getId, p -> p));
            List<UpsellSugestaoDTO> resultado = new ArrayList<>();
            for (JsonNode node : nodes) {
                if (!node.has("id") || !node.has("motivo")) continue;

                long id = node.get("id").asLong();
                String motivo = node.get("motivo").asText().trim();
                if (motivo.length() > 60) {
                    motivo = motivo.substring(0, 60);
                }

                Produto produto = disponiveisPorId.get(id);
                if (produto != null) {
                    resultado.add(new UpsellSugestaoDTO(
                            produto.getId(),
                            produto.getNome(),
                            produto.getPreco(),
                            produto.getCategoriaProduto(),
                            motivo
                    ));
                }
            }
            return resultado;
        } catch (Exception e) {
            log.warn("Erro ao parsear resposta da IA: {}", e.getMessage());
            return List.of();
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "[]";
    }

    private List<UpsellSugestaoDTO> gerarFallback(List<Produto> produtosSugeriveis,
                                                   List<Produto> combinacoesPopulares,
                                                   List<Produto> todosProdutos,
                                                   List<Long> carrinhoIds) {
        if (!combinacoesPopulares.isEmpty()) {
            return combinacoesPopulares.stream()
                    .limit(3)
                    .map(p -> new UpsellSugestaoDTO(p.getId(), p.getNome(), p.getPreco(),
                            p.getCategoriaProduto(), "Muito pedido junto com seu pedido!"))
                    .toList();
        }

        boolean temPizzaNoCarrinho = todosProdutos.stream()
                .filter(p -> carrinhoIds.contains(p.getId()))
                .anyMatch(p -> p.getCategoriaProduto() == CategoriaProduto.PIZZA
                        || p.getCategoriaProduto() == CategoriaProduto.PIZZA_DOCE);

        List<Produto> candidatos = new ArrayList<>();
        if (temPizzaNoCarrinho) {
            produtosSugeriveis.stream()
                    .filter(p -> p.getCategoriaProduto() == CategoriaProduto.BEBIDAS)
                    .limit(2)
                    .forEach(candidatos::add);
            produtosSugeriveis.stream()
                    .filter(p -> p.getCategoriaProduto() == CategoriaProduto.SOBREMESA)
                    .limit(1)
                    .forEach(candidatos::add);
        }

        if (candidatos.size() < 3) {
            produtosSugeriveis.stream()
                    .filter(p -> !candidatos.contains(p))
                    .limit(3 - candidatos.size())
                    .forEach(candidatos::add);
        }

        return candidatos.stream()
                .map(p -> new UpsellSugestaoDTO(p.getId(), p.getNome(), p.getPreco(),
                        p.getCategoriaProduto(), "Aproveite para adicionar ao seu pedido!"))
                .toList();
    }
}
