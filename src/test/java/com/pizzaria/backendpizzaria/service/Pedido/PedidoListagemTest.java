package com.pizzaria.backendpizzaria.service.Pedido;

import com.pizzaria.backendpizzaria.domain.Pedido;
import com.pizzaria.backendpizzaria.domain.Produto;
import com.pizzaria.backendpizzaria.domain.Usuario;
import com.pizzaria.backendpizzaria.infra.exception.ValidationException;
import com.pizzaria.backendpizzaria.repository.PedidoRepository;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoListagemTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private UsuarioRepository clienteRepository;

    @InjectMocks
    private PedidoListagem pedidoListagem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve listar pedido por id com sucesso")
    void listarPedidoPorId_sucesso() {
        Pedido pedido = new Pedido();
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        Optional<Pedido> result = pedidoListagem.listarPedidoPorId(1L);

        assertTrue(result.isPresent());
        assertEquals(pedido, result.get());
    }

    @Test
    @DisplayName("Deve retornar Optional.empty ao não encontrar pedido por id")
    void listarPedidoPorId_naoEncontrado() {
        when(pedidoRepository.findById(1)).thenReturn(Optional.empty());

        Optional<Pedido> result = pedidoListagem.listarPedidoPorId(1L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve listar todos os pedidos")
    void listarPedidos_sucesso() {
        Pedido pedido1 = new Pedido();
        Pedido pedido2 = new Pedido();

        List<Pedido> listaPedidos = Arrays.asList(pedido1, pedido2);
        Page<Pedido> paginaPedidos = new PageImpl<>(listaPedidos);

        when(pedidoRepository.findAll(any(Pageable.class))).thenReturn(paginaPedidos);

        Page<Pedido> result = pedidoListagem.listarPedidos(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().contains(pedido1));
        assertTrue(result.getContent().contains(pedido2));
    }

    @Test
    @DisplayName("Deve buscar produtos por ids com sucesso")
    void buscarProdutos_sucesso() {
        Produto produto1 = new Produto();
        Produto produto2 = new Produto();
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto1));
        when(produtoRepository.findById(2)).thenReturn(Optional.of(produto2));

        List<Produto> result = pedidoListagem.buscarProdutos(Arrays.asList(1, 2));

        assertEquals(2, result.size());
        assertTrue(result.contains(produto1));
        assertTrue(result.contains(produto2));
    }

    @Test
    @DisplayName("Deve lançar exceção ao não encontrar produto")
    void buscarProdutos_produtoNaoEncontrado() {
        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(ValidationException.class, () -> pedidoListagem.buscarProdutos(Collections.singletonList(1)));
        assertTrue(ex.getMessage().contains("Produto com ID 1 não encontrado"));
    }

    @Test
    @DisplayName("Deve buscar cliente por id com sucesso")
    void buscarCliente_sucesso() {
        Usuario usuario = new Usuario();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario result = pedidoListagem.buscarCliente(1L);

        assertEquals(usuario, result);
    }

    @Test
    @DisplayName("Deve lançar exceção ao não encontrar cliente")
    void buscarCliente_naoEncontrado() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoListagem.buscarCliente(1L));
        assertTrue(ex.getMessage().contains("Cliente com ID 1 não encontrado"));
    }
}