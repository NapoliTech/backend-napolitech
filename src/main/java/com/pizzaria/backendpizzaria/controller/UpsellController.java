package com.pizzaria.backendpizzaria.controller;

import com.pizzaria.backendpizzaria.domain.DTO.Upsell.UpsellRequestDTO;
import com.pizzaria.backendpizzaria.domain.DTO.Upsell.UpsellSugestaoDTO;
import com.pizzaria.backendpizzaria.service.Upsell.UpsellService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upsell")
@Tag(name = "Upsell", description = "Sugestões personalizadas de upsell geradas por IA para o checkout.")
public class UpsellController {

    private final UpsellService upsellService;

    public UpsellController(UpsellService upsellService) {
        this.upsellService = upsellService;
    }

    @Operation(
            summary = "Gerar sugestões de upsell",
            description = "Retorna até 3 sugestões personalizadas de produtos para exibir no checkout, " +
                    "baseadas no histórico do cliente e nos itens do carrinho atual. " +
                    "Geradas pelo modelo Claude (Anthropic) com fallback automático em caso de falha."
    )
    @PostMapping("/{clienteId}")
    public ResponseEntity<?> gerarSugestoes(
            @Parameter(description = "ID do cliente logado.", example = "1")
            @PathVariable Long clienteId,
            @Parameter(description = "IDs dos produtos no carrinho atual.")
            @RequestBody UpsellRequestDTO request) {
        try {
            List<UpsellSugestaoDTO> sugestoes = upsellService.gerarSugestoes(
                    clienteId,
                    request.getProdutosIds()
            );
            return ResponseEntity.ok(sugestoes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao gerar sugestões: " + e.getMessage()));
        }
    }
}
