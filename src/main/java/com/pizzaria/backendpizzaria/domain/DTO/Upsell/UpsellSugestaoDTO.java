package com.pizzaria.backendpizzaria.domain.DTO.Upsell;

import com.pizzaria.backendpizzaria.domain.Enum.CategoriaProduto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sugestão de upsell gerada pela IA para exibir no checkout.")
public class UpsellSugestaoDTO {

    @Schema(description = "ID do produto sugerido.", example = "5")
    private Long id;

    @Schema(description = "Nome do produto.", example = "Coca-Cola 2L")
    private String nome;

    @Schema(description = "Preço do produto.", example = "12.90")
    private Double preco;

    @Schema(description = "Categoria do produto.", example = "BEBIDAS")
    private CategoriaProduto categoriaProduto;

    @Schema(description = "Motivo personalizado da sugestão gerado pela IA.", example = "Combina muito com pizzas grandes!")
    private String motivo;

    public UpsellSugestaoDTO() {}

    public UpsellSugestaoDTO(Long id, String nome, Double preco, CategoriaProduto categoriaProduto, String motivo) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.categoriaProduto = categoriaProduto;
        this.motivo = motivo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    public CategoriaProduto getCategoriaProduto() { return categoriaProduto; }
    public void setCategoriaProduto(CategoriaProduto categoriaProduto) { this.categoriaProduto = categoriaProduto; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
