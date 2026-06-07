package com.pizzaria.backendpizzaria.domain.DTO.Avaliacao;

import java.util.Map;

public class AvaliacaoDashboardDTO {

    private Double mediaGeral;
    private Long totalAvaliacoes;
    private Long totalFotos;
    private Map<Integer, Long> distribuicaoEstrelas;

    public Double getMediaGeral() { return mediaGeral; }
    public void setMediaGeral(Double mediaGeral) { this.mediaGeral = mediaGeral; }

    public Long getTotalAvaliacoes() { return totalAvaliacoes; }
    public void setTotalAvaliacoes(Long totalAvaliacoes) { this.totalAvaliacoes = totalAvaliacoes; }

    public Long getTotalFotos() { return totalFotos; }
    public void setTotalFotos(Long totalFotos) { this.totalFotos = totalFotos; }

    public Map<Integer, Long> getDistribuicaoEstrelas() { return distribuicaoEstrelas; }
    public void setDistribuicaoEstrelas(Map<Integer, Long> distribuicaoEstrelas) { this.distribuicaoEstrelas = distribuicaoEstrelas; }
}
