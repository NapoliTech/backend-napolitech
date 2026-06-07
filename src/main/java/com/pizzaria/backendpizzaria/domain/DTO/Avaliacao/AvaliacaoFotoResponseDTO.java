package com.pizzaria.backendpizzaria.domain.DTO.Avaliacao;

import java.time.LocalDateTime;

public class AvaliacaoFotoResponseDTO {

    private Long id;
    private String dadosImagem;
    private String nomeArquivo;
    private LocalDateTime dataUpload;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDadosImagem() { return dadosImagem; }
    public void setDadosImagem(String dadosImagem) { this.dadosImagem = dadosImagem; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
}
