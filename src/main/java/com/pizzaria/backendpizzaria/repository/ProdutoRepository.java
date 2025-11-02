package com.pizzaria.backendpizzaria.repository;

import com.pizzaria.backendpizzaria.domain.Pedido;
import com.pizzaria.backendpizzaria.domain.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    Page<Produto> findAll(Pageable pageable);
}
