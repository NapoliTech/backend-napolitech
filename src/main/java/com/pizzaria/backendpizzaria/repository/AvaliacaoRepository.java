package com.pizzaria.backendpizzaria.repository;

import com.pizzaria.backendpizzaria.domain.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Page<Avaliacao> findByUsuario_IdUsuarioOrderByDataAvaliacaoDesc(Long usuarioId, Pageable pageable);

    List<Avaliacao> findTop5ByOrderByDataAvaliacaoDesc();

    boolean existsByPedido_Id(Long pedidoId);

    Optional<Avaliacao> findByPedido_Id(Long pedidoId);

    @Query("SELECT a.pedido.id FROM Avaliacao a WHERE a.usuario.idUsuario = :usuarioId")
    List<Long> findPedidosAvaliados(@Param("usuarioId") Long usuarioId);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a")
    Optional<Double> calcularMediaGeral();

    @Query("SELECT COUNT(a) FROM Avaliacao a")
    Long contarTotal();

    @Query("SELECT a.nota, COUNT(a) FROM Avaliacao a GROUP BY a.nota ORDER BY a.nota")
    List<Object[]> distribuicaoPorNota();
}
