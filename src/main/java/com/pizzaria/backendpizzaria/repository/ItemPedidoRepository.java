package com.pizzaria.backendpizzaria.repository;

import com.pizzaria.backendpizzaria.domain.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    /**
     * Busca os IDs dos produtos mais pedidos em conjunto com os produtos do carrinho atual.
     * Usada para enriquecer o contexto da IA com combinações reais do banco.
     *
     * Exemplo: se o carrinho tem pizza de frango, retorna os produtos que mais aparecem
     * nos mesmos pedidos que contêm pizza de frango.
     */
    @Query(value = """
            SELECT ip2.produto_id
            FROM item_pedido ip1
            JOIN item_pedido ip2
                ON ip1.pedido_id = ip2.pedido_id
                AND ip1.produto_id != ip2.produto_id
            JOIN estoque_produtos p
                ON ip2.produto_id = p.id
            WHERE ip1.produto_id IN (:produtoIds)
              AND p.quantidade_estoque > 0
            GROUP BY ip2.produto_id
            ORDER BY COUNT(*) DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Object> findTopCombinacoesIds(@Param("produtoIds") List<Long> produtoIds);
}
