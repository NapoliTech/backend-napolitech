package com.pizzaria.backendpizzaria.service.Pedido;

import com.pizzaria.backendpizzaria.domain.*;
import com.pizzaria.backendpizzaria.domain.DTO.Pedido.ItemPedidoDTO;
import com.pizzaria.backendpizzaria.domain.DTO.Pedido.PedidoDTO;
import com.pizzaria.backendpizzaria.domain.Enum.*;
import com.pizzaria.backendpizzaria.infra.exception.ValidationException;
import com.pizzaria.backendpizzaria.repository.PedidoRepository;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import com.pizzaria.backendpizzaria.service.RabbitMQ.PedidoProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UsuarioRepository clienteRepository;

    @Mock
    private PedidoValidacao pedidoValidacao;

    @Mock
    private PedidoListagem pedidoListagem;

    @Mock
    private PedidoProducerService pedidoProducerService;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario cliente;
    private Produto produto;
    private PedidoDTO pedidoDTO;
    private ItemPedidoDTO itemDTO;
    private Pedido pedidoSalvo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cliente = new Usuario();
        cliente.setIdUsuario(1L);
        cliente.setNome("João da Pizza");
        cliente.setPedidos(5L);
        cliente.setEndereco(new Endereco());

        produto = new Produto();
        produto.setId(10L);
        produto.setNome("Calabresa");
        produto.setPreco(50.0);
        produto.setQuantidadeEstoque(20);

        itemDTO = new ItemPedidoDTO();
        itemDTO.setProduto(Collections.singletonList(10));
        itemDTO.setQuantidade(2);
        itemDTO.setTamanhoPizza(TamanhoPizza.GRANDE);
        itemDTO.setBordaRecheada(BordaRecheada.CATUPIRY);

        pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(1L);
        pedidoDTO.setItens(Collections.singletonList(itemDTO));
        pedidoDTO.setObservacao("Sem cebola");
        pedidoDTO.setTipoEntrega(TipoEntrega.DELIVERY);

        pedidoSalvo = new Pedido();
        pedidoSalvo.setId(100L);
        pedidoSalvo.setCliente(cliente);
        pedidoSalvo.setStatusPedido(StatusPedido.RECEBIDO);
    }

    @Test
    @DisplayName("Deve criar um pedido com sucesso e enviar para o RabbitMQ")
    void testCriarPedidoComSucesso() {
        when(pedidoListagem.buscarCliente(1L)).thenReturn(cliente);
        when(pedidoListagem.buscarProdutos(itemDTO.getProduto())).thenReturn(Collections.singletonList(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        Pedido resultado = pedidoService.criarPedido(pedidoDTO);

        assertNotNull(resultado);
        assertEquals(StatusPedido.RECEBIDO, resultado.getStatusPedido());
        assertEquals(cliente, resultado.getCliente());
        assertNotNull(resultado.getItens());
        assertEquals(1, resultado.getItens().size());
        assertTrue(resultado.getPrecoTotal() > 0);

        verify(pedidoValidacao).validarPedidoDTO(pedidoDTO);
        verify(pedidoValidacao).validarItemPedidoDTO(itemDTO);
        verify(pedidoValidacao).verificarTamanhoPizza(itemDTO.getTamanhoPizza());
        verify(pedidoValidacao).validarEnderecoCliente(cliente);
        verify(pedidoListagem).buscarCliente(1L);
        verify(pedidoListagem).buscarProdutos(itemDTO.getProduto());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(produtoRepository, atLeastOnce()).save(any(Produto.class));
        verify(pedidoProducerService).enviarPedido(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se pizza MEIO_A_MEIO não tiver exatamente 2 sabores")
    void testCriarPedidoMeioAMeioInvalido() {
        itemDTO.setTamanhoPizza(TamanhoPizza.MEIO_A_MEIO);
        when(pedidoListagem.buscarCliente(1L)).thenReturn(cliente);
        when(pedidoListagem.buscarProdutos(itemDTO.getProduto()))
                .thenReturn(Collections.singletonList(new Produto()));

        when(pedidoRepository.save(any())).thenReturn(pedidoSalvo);

        assertThrows(ValidationException.class, () -> pedidoService.criarPedido(pedidoDTO));

        verify(pedidoValidacao).validarPedidoDTO(pedidoDTO);
        verify(pedidoValidacao).validarItemPedidoDTO(itemDTO);
        verify(pedidoValidacao).verificarTamanhoPizza(TamanhoPizza.MEIO_A_MEIO);
    }

    @Test
    @DisplayName("Deve atualizar o status de um pedido com sucesso")
    void testAtualizarStatusPedidoComSucesso() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStatusPedido(StatusPedido.RECEBIDO);

        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido atualizado = pedidoService.atualizarStatusPedido(1L, "ENCERRADO");

        assertEquals(StatusPedido.ENCERRADO, atualizado.getStatusPedido());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    @DisplayName("Deve lançar exceção se o status informado for inválido")
    void testAtualizarStatusPedidoInvalido() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStatusPedido(StatusPedido.RECEBIDO);

        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.atualizarStatusPedido(1L, "INEXISTENTE"));

        assertTrue(ex.getMessage().contains("Status inválido"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção se tentar atualizar status de pedido inexistente")
    void testAtualizarStatusPedidoNaoEncontrado() {
        when(pedidoRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.atualizarStatusPedido(999L, "RECEBIDO"));

        assertTrue(ex.getMessage().contains("Pedido com ID 999 não encontrado"));
    }
}
