package com.pizzaria.backendpizzaria.controller;

import com.pizzaria.backendpizzaria.config.JwtUtil;
import com.pizzaria.backendpizzaria.domain.DTO.Avaliacao.*;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import com.pizzaria.backendpizzaria.service.Avaliacao.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avaliacoes")
@Tag(name = "Avaliações", description = "Endpoints para avaliação da loja e experiência do cliente")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoController(AvaliacaoService avaliacaoService,
                               JwtUtil jwtUtil,
                               UsuarioRepository usuarioRepository) {
        this.avaliacaoService = avaliacaoService;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Criar avaliação", description = "Registra uma nova avaliação do usuário autenticado")
    @PostMapping
    public ResponseEntity<?> criar(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AvaliacaoRequestDTO dto) {
        try {
            Long usuarioId = extrairUsuarioId(authHeader);
            AvaliacaoResponseDTO response = avaliacaoService.criar(usuarioId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @Operation(summary = "Buscar avaliação por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(avaliacaoService.buscarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
        }
    }

    @Operation(summary = "Listar minhas avaliações", description = "Retorna avaliações do usuário autenticado paginadas")
    @GetMapping("/minhas")
    public ResponseEntity<?> listarMinhas(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long usuarioId = extrairUsuarioId(authHeader);
            Page<AvaliacaoResponseDTO> resultado = avaliacaoService.listarMinhas(
                    usuarioId,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataAvaliacao"))
            );
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @Operation(summary = "Listar todas as avaliações (admin)")
    @GetMapping
    public ResponseEntity<Page<AvaliacaoResponseDTO>> listarTodas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(avaliacaoService.listarTodas(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataAvaliacao"))
        ));
    }

    @Operation(summary = "Adicionar foto à avaliação", description = "Envia uma foto em base64 para uma avaliação existente")
    @PostMapping("/{id}/fotos")
    public ResponseEntity<?> adicionarFoto(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AvaliacaoFotoRequestDTO dto) {
        try {
            Long usuarioId = extrairUsuarioId(authHeader);
            AvaliacaoFotoResponseDTO response = avaliacaoService.adicionarFoto(id, usuarioId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @Operation(summary = "IDs dos pedidos já avaliados pelo usuário autenticado")
    @GetMapping("/pedidos-avaliados")
    public ResponseEntity<?> pedidosAvaliados(@RequestHeader("Authorization") String authHeader) {
        try {
            Long usuarioId = extrairUsuarioId(authHeader);
            List<Long> ids = avaliacaoService.pedidosAvaliados(usuarioId);
            return ResponseEntity.ok(ids);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @Operation(summary = "Dashboard de avaliações", description = "Retorna KPIs: média, total, distribuição por estrelas")
    @GetMapping("/dashboard")
    public ResponseEntity<AvaliacaoDashboardDTO> dashboard() {
        return ResponseEntity.ok(avaliacaoService.dashboard());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Long extrairUsuarioId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);
        return usuarioRepository.findByEmail(email)
                .map(u -> u.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
