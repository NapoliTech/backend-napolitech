package com.pizzaria.backendpizzaria.service.Pedido;

import com.pizzaria.backendpizzaria.domain.Pedido;
import com.pizzaria.backendpizzaria.domain.Produto;
import com.pizzaria.backendpizzaria.domain.Usuario;
import com.pizzaria.backendpizzaria.infra.exception.ValidationException;
import com.pizzaria.backendpizzaria.repository.PedidoRepository;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoListagem {

    private final PedidoRepository pedidoRepository;

    private final ProdutoRepository produtoRepository;

    private final UsuarioRepository clienteRepository;

    public PedidoListagem(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository, UsuarioRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
    }

    public Optional<Pedido> listarPedidoPorId(Long id) {
        return pedidoRepository.findById(Math.toIntExact(id));
    }

    public Page<Pedido> listarPedidos(Pageable pageable) {
        return pedidoRepository.findAll(pageable);
    }

    public List<Produto> buscarProdutos(List<Integer> produtosIds) {
        return produtosIds.stream()
                .map(produtoId -> produtoRepository.findById(produtoId)
                        .orElseThrow(() -> new ValidationException("Produto com ID " + produtoId + " não encontrado")))
                .collect(Collectors.toList());
    }

    public Usuario buscarCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente com ID " + clienteId + " não encontrado"));
    }

}
