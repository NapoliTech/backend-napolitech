package com.pizzaria.backendpizzaria.service.Estoque;

import com.pizzaria.backendpizzaria.domain.DTO.Pedido.ProdutoDTO;
import com.pizzaria.backendpizzaria.domain.Enum.CategoriaProduto;
import com.pizzaria.backendpizzaria.domain.Produto;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Transactional
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

    public Optional<Produto> listarProdutoPorId(Integer id) {
        return produtoRepository.findById(id);
    }

    public Page<Produto> listarProdutos(Pageable pageable) {
        return produtoRepository.findAll(pageable);
    }

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