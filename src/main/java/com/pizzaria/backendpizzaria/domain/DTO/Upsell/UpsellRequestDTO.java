package com.pizzaria.backendpizzaria.domain.DTO.Upsell;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados do carrinho atual para geração de sugestões de upsell.")
public class UpsellRequestDTO {

    @Schema(description = "IDs dos produtos que estão no carrinho do cliente.", example = "[1, 3]", required = true)
    private List<Long> produtosIds;

    public List<Long> getProdutosIds() {
        return produtosIds;
    }

    public void setProdutosIds(List<Long> produtosIds) {
        this.produtosIds = produtosIds;
    }
}
