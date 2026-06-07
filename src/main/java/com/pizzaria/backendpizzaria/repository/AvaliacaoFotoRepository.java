package com.pizzaria.backendpizzaria.repository;

import com.pizzaria.backendpizzaria.domain.AvaliacaoFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AvaliacaoFotoRepository extends JpaRepository<AvaliacaoFoto, Long> {

    long countByAvaliacao_Id(Long avaliacaoId);

    @Query("SELECT COUNT(f) FROM AvaliacaoFoto f")
    Long contarTotal();
}
