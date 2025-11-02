package com.pizzaria.backendpizzaria.controller;

import com.pizzaria.backendpizzaria.domain.DTO.Pedido.ProdutoDTO;
import com.pizzaria.backendpizzaria.domain.Pedido;
import com.pizzaria.backendpizzaria.domain.Produto;
import com.pizzaria.backendpizzaria.service.Estoque.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos.")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }


    @Operation(summary = "Cadastrar um novo produto", description = "Registra um novo produto no sistema.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> cadastrarProduto(
            @Parameter(description = "Dados do produto a ser cadastrado.") @RequestBody ProdutoDTO produtoDTO) {
        try {
            Produto produtoCriado = produtoService.cadastrarProduto(produtoDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("produtoId", produtoCriado.getId());
            response.put("nome", produtoCriado.getNome());
            response.put("preco", produtoCriado.getPreco());
            response.put("quantidade", produtoCriado.getQuantidadeEstoque());
            response.put("ingredientes", produtoCriado.getIngredientes());
            response.put("categoriaProduto", produtoCriado.getCategoriaProduto());
            response.put("mensagem", "Produto cadastrado com sucesso!");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Erro ao cadastrar produto: " + e.getMessage()));
        }
    }

    @Operation(summary = "Buscar produto por ID", description = "Retorna os detalhes de um produto pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> listarProdutosPorId(
            @Parameter(description = "ID do produto a ser buscado.", example = "1") @PathVariable("id") Integer id) {
        Optional<Produto> produto = produtoService.listarProdutoPorId(id);
        Map<String, Object> response = new HashMap<>();
        response.put("produto", produto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista de todos os produtos cadastrados.")
    @GetMapping
    public ResponseEntity<Page<Produto>> listarPedidos(
            @Parameter(description = "Configuração de paginação e ordenação")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Produto> produtos = produtoService.listarProdutos(pageable);
        return ResponseEntity.ok(produtos);
    }

    @Operation(summary = "Deletar produto", description = "Remove um produto pelo ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletarProduto(
            @Parameter(description = "ID do produto a ser deletado.", example = "1") @PathVariable Integer id) {
        boolean deletado = produtoService.deletarProduto(id);

        if (!deletado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Produto não encontrado!"));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("mensagem", "Produto deletado com sucesso!"));
    }
}