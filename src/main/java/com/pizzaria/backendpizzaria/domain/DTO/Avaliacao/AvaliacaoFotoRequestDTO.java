package com.pizzaria.backendpizzaria.domain.DTO.Avaliacao;

import jakarta.validation.constraints.NotBlank;

public class AvaliacaoFotoRequestDTO {

    @NotBlank(message = "Dados da imagem são obrigatórios")
    private String dadosImagem; // base64

    @NotBlank(message = "Nome do arquivo é obrigatório")
    private String nomeArquivo;

    public String getDadosImagem() { return dadosImagem; }
    public void setDadosImagem(String dadosImagem) { this.dadosImagem = dadosImagem; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }
}
