package com.pizzaria.backendpizzaria.service.Pedido;

import com.pizzaria.backendpizzaria.domain.*;
import com.pizzaria.backendpizzaria.domain.DTO.Pedido.ItemPedidoDTO;
import com.pizzaria.backendpizzaria.domain.DTO.Pedido.PedidoDTO;
import com.pizzaria.backendpizzaria.domain.Enum.BordaRecheada;
import com.pizzaria.backendpizzaria.domain.Enum.StatusPedido;
import com.pizzaria.backendpizzaria.domain.Enum.TamanhoPizza;
import com.pizzaria.backendpizzaria.domain.Enum.TipoEntrega;
import com.pizzaria.backendpizzaria.infra.exception.ValidationException;
import com.pizzaria.backendpizzaria.repository.EnderecoRepository;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;
import com.pizzaria.backendpizzaria.repository.PedidoRepository;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import com.pizzaria.backendpizzaria.service.RabbitMQ.PedidoProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
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
    private ItemPedidoDTO itemPedidoDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cliente = new Usuario();
        cliente.setIdUsuario(1L);
        cliente.setNome("Cliente Teste");
        Endereco endereco = new Endereco();
        cliente.setEndereco(endereco);

        produto = new Produto();
        produto.setId(1L);
        produto.setPreco(50.0);
        produto.setQuantidadeEstoque(10);

        itemPedidoDTO = new ItemPedidoDTO(
                Collections.singletonList(1),
                2,
                TamanhoPizza.GRANDE,
                null
        );

        pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(1L);
        pedidoDTO.setItens(Collections.singletonList(itemPedidoDTO));
        pedidoDTO.setTipoEntrega(TipoEntrega.DELIVERY);

        Usuario cliente = new Usuario();
        cliente.setIdUsuario(1L); // ou outro valor válido
        cliente.setPedidos(0L); // inicialização mínima

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
    }

//    @Test
//    void criarPedido_sucesso_comMensageria() {
//        doNothing().when(pedidoValidacao).validarPedidoDTO(any());
//        doNothing().when(pedidoValidacao).validarEnderecoCliente(any());
//        doNothing().when(pedidoValidacao).validarItemPedidoDTO(any());
//        doNothing().when(pedidoValidacao).verificarTamanhoPizza(any());
//
//        // Mocks de listagem
//        when(pedidoListagem.buscarCliente(anyLong())).thenReturn(cliente);
//        when(pedidoListagem.buscarProdutos(any())).thenReturn(Collections.singletonList(produto));
//
//        // Mocks de repositórios
//        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//        when(produtoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        // Mocks de mensageria
//        doNothing().when(pedidoProducerService).enviarPedido(any());
//
//        // Executa método
//        Pedido pedido = pedidoService.criarPedido(pedidoDTO);
//
//        // Verificações
//        assertNotNull(pedido);
//        assertEquals(1, pedido.getItens().size());
//        assertEquals(100.0, pedido.getPrecoTotal());
//        verify(pedidoProducerService, times(1)).enviarPedido(pedido); // verificando que RabbitMQ foi chamado
//        verify(pedidoRepository, times(1)).save(any());
//        verify(produtoRepository, times(1)).save(any());
//    }
//
//    @Test
//    @DisplayName("Deve criar pedido com sucesso")
//    void criarPedido_sucesso() {
//        // Arrange
//        doNothing().when(pedidoValidacao).validarPedidoDTO(any());
//        when(pedidoListagem.buscarCliente(cliente.getIdUsuario())).thenReturn(cliente);
//        doNothing().when(pedidoValidacao).validarEnderecoCliente(cliente);
//        doNothing().when(pedidoValidacao).validarItemPedidoDTO(any());
//        when(pedidoListagem.buscarProdutos(any())).thenReturn(Collections.singletonList(produto));
//        doNothing().when(pedidoValidacao).verificarTamanhoPizza(any());
//
//        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
//        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));
//        doNothing().when(pedidoProducerService).enviarPedido(any(Pedido.class));
//
//        // Act
//        Pedido pedido = pedidoService.criarPedido(pedidoDTO);
//
//        // Assert
//        assertNotNull(pedido);
//        assertEquals(StatusPedido.RECEBIDO, pedido.getStatusPedido());
//        assertEquals(1, pedido.getItens().size());
//        assertEquals(100.0, pedido.getPrecoTotal());
//
//        // Verifica interações
//        verify(produtoRepository, times(1)).save(any(Produto.class));
//        verify(pedidoRepository, times(1)).save(any(Pedido.class));
//        verify(pedidoProducerService, times(1)).enviarPedido(any(Pedido.class));
//    }
//
//    @Test
//    @DisplayName("Deve lançar exceção se pizza MEIO_A_MEIO não tem 2 sabores")
//    void criarPedido_meioAMeioComSaboresErrados() {
//        itemPedidoDTO.setTamanhoPizza(TamanhoPizza.MEIO_A_MEIO);
//        pedidoDTO.setItens(Collections.singletonList(itemPedidoDTO));
//        doNothing().when(pedidoValidacao).validarPedidoDTO(any());
//        when(pedidoListagem.buscarCliente(1L)).thenReturn(cliente);
//        doNothing().when(pedidoValidacao).validarEnderecoCliente(cliente);
//        doNothing().when(pedidoValidacao).validarItemPedidoDTO(any());
//        when(pedidoListagem.buscarProdutos(any())).thenReturn(Collections.singletonList(produto));
//        doNothing().when(pedidoValidacao).verificarTamanhoPizza(any());
//
//        ValidationException ex = assertThrows(ValidationException.class, () -> pedidoService.criarPedido(pedidoDTO));
//        assertTrue(ex.getMessage().contains("Pizzas MEIO_A_MEIO devem conter exatamente 2 sabores."));
//    }
//
//    @Test
//    @DisplayName("Deve calcular preço com borda recheada")
//    void criarPedido_comBordaRecheada() {
//        itemPedidoDTO.setBordaRecheada(BordaRecheada.CHEDDAR);
//        pedidoDTO.setItens(Collections.singletonList(itemPedidoDTO));
//
//        doNothing().when(pedidoValidacao).validarPedidoDTO(any());
//        when(pedidoListagem.buscarCliente(cliente.getIdUsuario())).thenReturn(cliente);
//        doNothing().when(pedidoValidacao).validarEnderecoCliente(cliente);
//        doNothing().when(pedidoValidacao).validarItemPedidoDTO(any());
//        when(pedidoListagem.buscarProdutos(any())).thenReturn(Collections.singletonList(produto));
//        doNothing().when(pedidoValidacao).verificarTamanhoPizza(any());
//
//        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
//        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));
//        doNothing().when(pedidoProducerService).enviarPedido(any(Pedido.class));
//
//        // Act
//        Pedido pedido = pedidoService.criarPedido(pedidoDTO);
//
//        // Assert
//        double precoEsperado = produto.getPreco() * itemPedidoDTO.getQuantidade()
//                + BordaRecheada.CHEDDAR.getValorAdicional() * itemPedidoDTO.getQuantidade();
//        assertEquals(precoEsperado, pedido.getPrecoTotal());
//
//        // Verifica interações
//        verify(produtoRepository, times(1)).save(any(Produto.class));
//        verify(pedidoRepository, times(1)).save(any(Pedido.class));
//        verify(pedidoProducerService, times(1)).enviarPedido(any(Pedido.class));
//    }

//    @Test
//    @DisplayName("Deve atualizar status do pedido com sucesso")
//    void atualizarStatusPedido_sucesso() {
//        Pedido pedido = new Pedido();
//        pedido.setStatusPedido(StatusPedido.RECEBIDO);
//        when(pedidoRepository.findById(anyInt())).thenReturn(Optional.of(pedido));
//        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        Pedido atualizado = pedidoService.atualizarStatusPedido(1L, "ENCERRADO");
//
//        assertEquals(StatusPedido.ENCERRADO, atualizado.getStatusPedido());
//    }

//    @Test
//    @DisplayName("Deve lançar exceção se pedido não encontrado ao atualizar status")
//    void atualizarStatusPedido_pedidoNaoEncontrado() {
//        when(pedidoRepository.findById(anyInt())).thenReturn(Optional.empty());
//
//        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoService.atualizarStatusPedido(1L, "PRONTO"));
//        assertTrue(ex.getMessage().contains("Pedido com ID 1 não encontrado"));
//    }
//
//    @Test
//    @DisplayName("Deve lançar exceção se status inválido")
//    void atualizarStatusPedido_statusInvalido() {
//        Pedido pedido = new Pedido();
//        pedido.setStatusPedido(StatusPedido.RECEBIDO);
//        when(pedidoRepository.findById(anyInt())).thenReturn(Optional.of(pedido));
//
//        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoService.atualizarStatusPedido(1L, "INVALIDO"));
//        assertTrue(ex.getMessage().contains("Status inválido"));
//    }
}