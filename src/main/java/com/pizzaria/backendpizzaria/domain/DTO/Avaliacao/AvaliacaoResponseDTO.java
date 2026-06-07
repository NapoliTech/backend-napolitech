package com.pizzaria.backendpizzaria.domain.DTO.Avaliacao;

import java.time.LocalDateTime;
import java.util.List;

public class AvaliacaoResponseDTO {

    private Long id;
    private Long usuarioId;
    private String nomeUsuario;
    private Long pedidoId;
    private Integer nota;
    private String comentario;
    private Double latitude;
    private Double longitude;
    private LocalDateTime dataAvaliacao;
    private List<AvaliacaoFotoResponseDTO> fotos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public Integer getNota() { return nota; }
    public void setNota(Integer nota) { this.nota = nota; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDateTime dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }

    public List<AvaliacaoFotoResponseDTO> getFotos() { return fotos; }
    public void setFotos(List<AvaliacaoFotoResponseDTO> fotos) { this.fotos = fotos; }
}
