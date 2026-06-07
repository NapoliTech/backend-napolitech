package com.pizzaria.backendpizzaria.domain.DTO.Avaliacao;

import jakarta.validation.constraints.*;

public class AvaliacaoRequestDTO {

    @NotNull(message = "Nota é obrigatória")
    @Min(value = 1, message = "Nota mínima é 1")
    @Max(value = 5, message = "Nota máxima é 5")
    private Integer nota;

    @NotBlank(message = "Comentário é obrigatório")
    @Size(min = 3, message = "Comentário deve ter no mínimo 3 caracteres")
    @Size(max = 1000, message = "Comentário deve ter no máximo 1000 caracteres")
    private String comentario;

    @NotNull(message = "ID do pedido é obrigatório")
    private Long pedidoId;

    private Double latitude;
    private Double longitude;

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
}
