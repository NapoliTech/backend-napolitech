package com.pizzaria.backendpizzaria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacao_fotos")
public class AvaliacaoFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "avaliacao_id", nullable = false)
    @JsonBackReference
    private Avaliacao avaliacao;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String dadosImagem;

    @Column(nullable = false)
    private String nomeArquivo;

    @Column(nullable = false)
    private LocalDateTime dataUpload;

    public AvaliacaoFoto() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Avaliacao getAvaliacao() { return avaliacao; }
    public void setAvaliacao(Avaliacao avaliacao) { this.avaliacao = avaliacao; }

    public String getDadosImagem() { return dadosImagem; }
    public void setDadosImagem(String dadosImagem) { this.dadosImagem = dadosImagem; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
}
