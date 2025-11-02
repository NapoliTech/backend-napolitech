package com.pizzaria.backendpizzaria.service.Estoque;

import com.pizzaria.backendpizzaria.domain.DTO.Pedido.ProdutoDTO;
import com.pizzaria.backendpizzaria.domain.Enum.CategoriaProduto;
import com.pizzaria.backendpizzaria.domain.Produto;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private ProdutoDTO produtoDTO;
    private Produto produto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        produtoDTO = new ProdutoDTO();
        produtoDTO.setNome("Pizza Calabresa");
        produtoDTO.setPreco(50.0);
        produtoDTO.setQuantidade(10);
        produtoDTO.setIngredientes(String.valueOf(Arrays.asList("Calabresa", "Queijo")));
        produtoDTO.setCategoriaProduto(CategoriaProduto.PIZZA);

        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Pizza Calabresa");
        produto.setPreco(50.0);
        produto.setQuantidadeEstoque(10);
        produto.setIngredientes(String.valueOf(Arrays.asList("Calabresa", "Queijo")));
        produto.setCategoriaProduto(CategoriaProduto.PIZZA);
    }

    @Test
    void cadastrarProduto_sucesso() {
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> {
            Produto p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Produto salvo = produtoService.cadastrarProduto(produtoDTO);

        assertNotNull(salvo);
        assertEquals(produtoDTO.getNome(), salvo.getNome());
        assertEquals(produtoDTO.getPreco(), salvo.getPreco());
        assertEquals(produtoDTO.getQuantidade(), salvo.getQuantidadeEstoque());
        assertEquals(produtoDTO.getIngredientes(), salvo.getIngredientes());
        assertEquals(CategoriaProduto.PIZZA, salvo.getCategoriaProduto());
    }

    @Test
    void cadastrarProduto_categoriaInvalida() {
        produtoDTO.setCategoriaProduto(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> produtoService.cadastrarProduto(produtoDTO));
        assertTrue(ex.getMessage().contains("Categoria inválida"));
    }

    @Test
    void listarProdutoPorId_sucesso() {
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        Optional<Produto> result = produtoService.listarProdutoPorId(1);

        assertTrue(result.isPresent());
        assertEquals(produto, result.get());
    }

    @Test
    void listarProdutoPorId_naoEncontrado() {
        when(produtoRepository.findById(2)).thenReturn(Optional.empty());

        Optional<Produto> result = produtoService.listarProdutoPorId(2);

        assertFalse(result.isPresent());
    }

    @Test
    void listarProdutos_sucesso() {
        Produto produto = new Produto();
        List<Produto> listaProdutos = Arrays.asList(produto);
        Page<Produto> paginaProdutos = new PageImpl<>(listaProdutos);

        when(produtoRepository.findAll(any(Pageable.class))).thenReturn(paginaProdutos);

        Page<Produto> result = produtoService.listarProdutos(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().contains(produto));
    }

    @Test
    void deletarProduto_sucesso() {
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        doNothing().when(produtoRepository).delete(produto);

        boolean result = produtoService.deletarProduto(1);

        assertTrue(result);
        verify(produtoRepository, times(1)).delete(produto);
    }

    @Test
    void deletarProduto_naoEncontrado() {
        when(produtoRepository.findById(2)).thenReturn(Optional.empty());

        boolean result = produtoService.deletarProduto(2);

        assertFalse(result);
        verify(produtoRepository, never()).delete(any());
    }
}