package com.pizzaria.backendpizzaria.service.Pedido;

import com.pizzaria.backendpizzaria.domain.*;
import com.pizzaria.backendpizzaria.domain.DTO.Pedido.ItemPedidoDTO;
import com.pizzaria.backendpizzaria.domain.DTO.Pedido.PedidoDTO;
import com.pizzaria.backendpizzaria.domain.Enum.BordaRecheada;
import com.pizzaria.backendpizzaria.domain.Enum.StatusPedido;
import com.pizzaria.backendpizzaria.domain.Enum.TamanhoPizza;
import com.pizzaria.backendpizzaria.infra.exception.ValidationException;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;
import com.pizzaria.backendpizzaria.repository.PedidoRepository;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import com.pizzaria.backendpizzaria.service.RabbitMQ.PedidoProducerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository clienteRepository;
    private final PedidoValidacao pedidoValidacao;
    private final PedidoListagem pedidoListagem;
    private final PedidoProducerService pedidoProducerService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         UsuarioRepository clienteRepository,
                         PedidoValidacao pedidoValidacao,
                         PedidoListagem pedidoListagem,
                         PedidoProducerService pedidoProducerService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoValidacao = pedidoValidacao;
        this.pedidoListagem = pedidoListagem;
        this.pedidoProducerService = pedidoProducerService;
    }

    @Transactional
    public Pedido criarPedido(PedidoDTO pedidoDTO) {
        pedidoValidacao.validarPedidoDTO(pedidoDTO);

        Usuario cliente = pedidoListagem.buscarCliente(pedidoDTO.getClienteId());
        incrementarPedidosCliente(cliente);
        pedidoValidacao.validarEnderecoCliente(cliente);

        Pedido pedido = construirPedidoInicial(pedidoDTO, cliente);

        List<ItemPedido> itens = processarItensPedido(pedidoDTO, pedido);

        pedido.setItens(itens);
        pedidoRepository.save(pedido);

        pedidoProducerService.enviarPedido(pedido);

        return pedido;
    }

    private void incrementarPedidosCliente(Usuario cliente) {
        Long atuais = cliente.getPedidos();
        if (atuais == null) {
            cliente.setPedidos(1L);
        } else {
            cliente.setPedidos(atuais + 1L);
        }
    }

    private Pedido construirPedidoInicial(PedidoDTO pedidoDTO, Usuario cliente) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setObservacao(pedidoDTO.getObservacao());
        pedido.setEndereco(cliente.getEndereco());
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatusPedido(StatusPedido.RECEBIDO);
        pedido.setNomeCliente(cliente.getNome());
        pedido.setTipoEntrega(pedidoDTO.getTipoEntrega());
        return pedido;
    }

    private List<ItemPedido> processarItensPedido(PedidoDTO pedidoDTO, Pedido pedido) {
        List<ItemPedido> itens = new ArrayList<>();
        double valorTotal = 0.0;

        for (ItemPedidoDTO itemDTO : pedidoDTO.getItens()) {
            pedidoValidacao.validarItemPedidoDTO(itemDTO);

            List<Produto> produtos = pedidoListagem.buscarProdutos(itemDTO.getProduto());
            pedidoValidacao.verificarTamanhoPizza(itemDTO.getTamanhoPizza());

            validarMeioAMeio(itemDTO, produtos);

            for (Produto produto : produtos) {
                ItemPedido item = criarItemPedido(itemDTO, pedido, produto);
                atualizarEstoqueProduto(produto, item.getQuantidade());
                itens.add(item);
                valorTotal += item.getPrecoTotal();
            }
        }

        pedido.setPrecoTotal(valorTotal);
        return itens;
    }

    private void validarMeioAMeio(ItemPedidoDTO itemDTO, List<Produto> produtos) {
        if (TamanhoPizza.MEIO_A_MEIO.equals(itemDTO.getTamanhoPizza())) {
            int quantidadeSabores = (produtos == null) ? 0 : produtos.size();
            if (quantidadeSabores != 2) {
                throw new ValidationException("Pizzas MEIO_A_MEIO devem conter exatamente 2 sabores.");
            }
        }
    }


    private ItemPedido criarItemPedido(ItemPedidoDTO itemDTO, Pedido pedido, Produto produto) {
        ItemPedido item = new ItemPedido();
        item.setQuantidade(itemDTO.getQuantidade());
        item.setTamanhoPizza(itemDTO.getTamanhoPizza());
        item.setProduto(produto);
        item.setPedido(pedido);

        double precoBase = calcularPrecoBase(produto.getPreco(), itemDTO.getTamanhoPizza());
        double precoTotal = precoBase * itemDTO.getQuantidade();

        BordaRecheada borda = itemDTO.getBordaRecheada();
        if (borda != null) {
            item.setBordaRecheada(borda);
            precoTotal += borda.getValorAdicional() * itemDTO.getQuantidade();
        }

        item.setPrecoTotal(precoTotal);
        return item;
    }

    private double calcularPrecoBase(Double precoProduto, TamanhoPizza tamanhoPizza) {
        if (precoProduto == null) return 0.0;

        if (TamanhoPizza.MEIO_A_MEIO.equals(tamanhoPizza) || TamanhoPizza.BROTO.equals(tamanhoPizza)) {
            return precoProduto / 2.0;
        } else if (TamanhoPizza.TREM.equals(tamanhoPizza)) {
            return precoProduto * 3.0;
        }
        return precoProduto;
    }

    private void atualizarEstoqueProduto(Produto produto, int quantidade) {
        Integer estoqueAtual = produto.getQuantidadeEstoque();
        int novoEstoque = (estoqueAtual == null ? 0 : estoqueAtual) - quantidade;
        produto.setQuantidadeEstoque(novoEstoque);
        produtoRepository.save(produto);
    }

    public Pedido atualizarStatusPedido(Long id, String statusPedido) {
        int idComoInt = Math.toIntExact(id);
        Pedido pedido = pedidoRepository.findById(idComoInt)
                .orElseThrow(() -> new RuntimeException("Pedido com ID " + id + " não encontrado"));

        StatusPedido novoStatus;
        try {
            novoStatus = StatusPedido.valueOf(statusPedido.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido: " + statusPedido);
        }

        pedido.setStatusPedido(novoStatus);
        return pedidoRepository.save(pedido);
    }
}
