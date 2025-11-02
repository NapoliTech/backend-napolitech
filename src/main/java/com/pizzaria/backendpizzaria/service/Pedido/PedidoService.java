package com.pizzaria.backendpizzaria.service.Pedido;

import com.pizzaria.backendpizzaria.domain.*;
import com.pizzaria.backendpizzaria.domain.DTO.Pedido.ItemPedidoDTO;
import com.pizzaria.backendpizzaria.domain.DTO.Pedido.PedidoDTO;
import com.pizzaria.backendpizzaria.domain.Enum.BordaRecheada;
import com.pizzaria.backendpizzaria.domain.Enum.StatusPedido;
import com.pizzaria.backendpizzaria.domain.Enum.TamanhoPizza;
import com.pizzaria.backendpizzaria.infra.exception.ValidationException;
import com.pizzaria.backendpizzaria.repository.EnderecoRepository;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;
import com.pizzaria.backendpizzaria.repository.PedidoRepository;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import com.pizzaria.backendpizzaria.service.RabbitMQ.PedidoProducerService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    private final ProdutoRepository produtoRepository;

    private final UsuarioRepository clienteRepository;

    private final PedidoValidacao pedidoValidacao;

    private final PedidoListagem pedidoListagem;

    private final PedidoProducerService pedidoProducerService;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository, UsuarioRepository clienteRepository, PedidoValidacao pedidoValidacao, PedidoListagem pedidoListagem, PedidoProducerService pedidoProducerService) {
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
        cliente.setPedidos(+1L);
        pedidoValidacao.validarEnderecoCliente(cliente);

        Pedido pedido = inicializarPedido(pedidoDTO, cliente);
        List<ItemPedido> itens = processarItensPedido(pedidoDTO, pedido);

        atualizarPedidoComItens(pedido, itens);

        pedidoRepository.save(pedido);
        pedidoProducerService.enviarPedido(pedido);

        return pedido;
    }

    private Pedido inicializarPedido(PedidoDTO pedidoDTO, Usuario cliente) {
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

            if (itemDTO.getTamanhoPizza() == TamanhoPizza.MEIO_A_MEIO && produtos.size() != 2) {
                throw new ValidationException("Pizzas MEIO_A_MEIO devem conter exatamente 2 sabores.");
            }

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

    private ItemPedido criarItemPedido(ItemPedidoDTO itemDTO, Pedido pedido, Produto produto) {
        ItemPedido item = new ItemPedido();
        item.setQuantidade(itemDTO.getQuantidade());
        item.setTamanhoPizza(itemDTO.getTamanhoPizza());
        item.setProduto(produto);
        item.setPedido(pedido);

        double precoBase = calcularPrecoBase(produto, itemDTO.getTamanhoPizza());
        item.setPrecoTotal(precoBase * itemDTO.getQuantidade());

        BordaRecheada borda = itemDTO.getBordaRecheada();
        if (borda != null) {
            item.setBordaRecheada(borda);
            item.setPrecoTotal(item.getPrecoTotal() + borda.getValorAdicional() * itemDTO.getQuantidade());
        }

        return item;
    }

    private double calcularPrecoBase(Produto produto, TamanhoPizza tamanhoPizza) {
        double preco = produto.getPreco();
        if (tamanhoPizza == TamanhoPizza.MEIO_A_MEIO) {
            return preco / 2;
        } else if (tamanhoPizza == TamanhoPizza.BROTO) {
            return preco / 2;
        } else if (tamanhoPizza == TamanhoPizza.TREM) {
            return preco * 3;
        }
        return preco;
    }

    private void atualizarEstoqueProduto(Produto produto, int quantidade) {
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
        produtoRepository.save(produto);
    }

    private void atualizarPedidoComItens(Pedido pedido, List<ItemPedido> itens) {
        pedido.setItens(itens);
    }

    public Pedido atualizarStatusPedido(Long id, String statusPedido) {
        Pedido pedido = pedidoRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new RuntimeException("Pedido com ID " + id + " não encontrado"));

        try {
            StatusPedido status = StatusPedido.valueOf(statusPedido.toUpperCase());
            pedido.setStatusPedido(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido: " + statusPedido);
        }

        return pedidoRepository.save(pedido);
    }



}
