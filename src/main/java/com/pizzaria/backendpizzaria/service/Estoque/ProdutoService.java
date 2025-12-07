package com.pizzaria.backendpizzaria.service.Estoque;

import com.pizzaria.backendpizzaria.domain.DTO.Pedido.ProdutoDTO;
import com.pizzaria.backendpizzaria.domain.Enum.CategoriaProduto;
import com.pizzaria.backendpizzaria.domain.Produto;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Transactional
    @CachePut(cacheNames = "produtoPorId", key = "#result.id")
    @CacheEvict(cacheNames = "listaProdutos", allEntries = true)
    public Produto cadastrarProduto(ProdutoDTO produtoDTO) {
        Produto produto = new Produto();
        produto.setNome(produtoDTO.getNome());
        produto.setPreco(produtoDTO.getPreco());
        produto.setQuantidadeEstoque(produtoDTO.getQuantidade());
        produto.setIngredientes(produtoDTO.getIngredientes());

        if (produtoDTO.getCategoriaProduto() == null) {
            throw new RuntimeException("Categoria inválida: null");
        }

        try {
            produto.setCategoriaProduto(CategoriaProduto.valueOf(produtoDTO.getCategoriaProduto().toString().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Categoria inválida: " + produtoDTO.getCategoriaProduto());
        }

        return produtoRepository.save(produto);
    }

    @Cacheable(cacheNames = "produtoPorId", key = "#id")
    public Optional<Produto> listarProdutoPorId(Integer id) {
        return produtoRepository.findById(id);
    }

    @Cacheable(cacheNames = "listaProdutos", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<Produto> listarProdutos(Pageable pageable) {
        return produtoRepository.findAll(pageable);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "produtoPorId", key = "#id"),
            @CacheEvict(cacheNames = "listaProdutos", allEntries = true)
    })
    @Transactional
    public boolean deletarProduto(Integer id) {
        Optional<Produto> produtoOptional = produtoRepository.findById(id);

        if (produtoOptional.isPresent()) {
            produtoRepository.delete(produtoOptional.get());
            return true;
        }

        return false;
    }
}