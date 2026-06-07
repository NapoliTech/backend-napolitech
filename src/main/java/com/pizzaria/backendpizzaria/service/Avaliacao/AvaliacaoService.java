package com.pizzaria.backendpizzaria.service.Avaliacao;

import com.pizzaria.backendpizzaria.domain.Avaliacao;
import com.pizzaria.backendpizzaria.domain.AvaliacaoFoto;
import com.pizzaria.backendpizzaria.domain.Pedido;
import com.pizzaria.backendpizzaria.domain.Usuario;
import com.pizzaria.backendpizzaria.domain.DTO.Avaliacao.*;
import com.pizzaria.backendpizzaria.infra.exception.ValidationException;
import com.pizzaria.backendpizzaria.repository.AvaliacaoFotoRepository;
import com.pizzaria.backendpizzaria.repository.AvaliacaoRepository;
import com.pizzaria.backendpizzaria.repository.PedidoRepository;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AvaliacaoService {

    private static final int MAX_FOTOS_POR_AVALIACAO = 10;

    private final AvaliacaoRepository avaliacaoRepository;
    private final AvaliacaoFotoRepository fotoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                            AvaliacaoFotoRepository fotoRepository,
                            UsuarioRepository usuarioRepository,
                            PedidoRepository pedidoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.fotoRepository = fotoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public AvaliacaoResponseDTO criar(Long usuarioId, AvaliacaoRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ValidationException("Usuário não encontrado"));

        Pedido pedido = pedidoRepository.findById(dto.getPedidoId().intValue())
                .orElseThrow(() -> new ValidationException("Pedido não encontrado"));

        if (!pedido.getCliente().getIdUsuario().equals(usuarioId)) {
            throw new ValidationException("Este pedido não pertence ao usuário");
        }

        if (avaliacaoRepository.existsByPedido_Id(dto.getPedidoId())) {
            throw new ValidationException("Este pedido já foi avaliado");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setPedido(pedido);
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(dto.getComentario().trim());
        avaliacao.setLatitude(dto.getLatitude());
        avaliacao.setLongitude(dto.getLongitude());
        avaliacao.setDataAvaliacao(LocalDateTime.now());

        return toDTO(avaliacaoRepository.save(avaliacao));
    }

    public List<Long> pedidosAvaliados(Long usuarioId) {
        return avaliacaoRepository.findPedidosAvaliados(usuarioId);
    }

    public AvaliacaoResponseDTO buscarPorId(Long id) {
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Avaliação não encontrada"));
        return toDTO(avaliacao);
    }

    public Page<AvaliacaoResponseDTO> listarMinhas(Long usuarioId, Pageable pageable) {
        return avaliacaoRepository
                .findByUsuario_IdUsuarioOrderByDataAvaliacaoDesc(usuarioId, pageable)
                .map(this::toDTO);
    }

    public Page<AvaliacaoResponseDTO> listarTodas(Pageable pageable) {
        return avaliacaoRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional
    public AvaliacaoFotoResponseDTO adicionarFoto(Long avaliacaoId, Long usuarioId, AvaliacaoFotoRequestDTO dto) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new ValidationException("Avaliação não encontrada"));

        if (!avaliacao.getUsuario().getIdUsuario().equals(usuarioId)) {
            throw new ValidationException("Sem permissão para adicionar foto nesta avaliação");
        }

        long totalFotos = fotoRepository.countByAvaliacao_Id(avaliacaoId);
        if (totalFotos >= MAX_FOTOS_POR_AVALIACAO) {
            throw new ValidationException("Limite de " + MAX_FOTOS_POR_AVALIACAO + " fotos por avaliação atingido");
        }

        AvaliacaoFoto foto = new AvaliacaoFoto();
        foto.setAvaliacao(avaliacao);
        foto.setDadosImagem(dto.getDadosImagem());
        foto.setNomeArquivo(dto.getNomeArquivo());
        foto.setDataUpload(LocalDateTime.now());

        return toFotoDTO(fotoRepository.save(foto));
    }

    public AvaliacaoDashboardDTO dashboard() {
        Double media = avaliacaoRepository.calcularMediaGeral().orElse(0.0);
        Long total = avaliacaoRepository.contarTotal();
        Long totalFotos = fotoRepository.contarTotal();
        List<Object[]> distribuicaoRaw = avaliacaoRepository.distribuicaoPorNota();

        Map<Integer, Long> distribuicao = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribuicao.put(i, 0L);
        for (Object[] row : distribuicaoRaw) {
            distribuicao.put((Integer) row[0], (Long) row[1]);
        }

        AvaliacaoDashboardDTO dto = new AvaliacaoDashboardDTO();
        dto.setMediaGeral(media != null ? Math.round(media * 100.0) / 100.0 : 0.0);
        dto.setTotalAvaliacoes(total);
        dto.setTotalFotos(totalFotos);
        dto.setDistribuicaoEstrelas(distribuicao);
        return dto;
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private AvaliacaoResponseDTO toDTO(Avaliacao a) {
        AvaliacaoResponseDTO dto = new AvaliacaoResponseDTO();
        dto.setId(a.getId());
        dto.setUsuarioId(a.getUsuario().getIdUsuario());
        dto.setNomeUsuario(a.getUsuario().getNome());
        if (a.getPedido() != null) dto.setPedidoId(a.getPedido().getId());
        dto.setNota(a.getNota());
        dto.setComentario(a.getComentario());
        dto.setLatitude(a.getLatitude());
        dto.setLongitude(a.getLongitude());
        dto.setDataAvaliacao(a.getDataAvaliacao());
        dto.setFotos(a.getFotos().stream().map(this::toFotoDTO).collect(Collectors.toList()));
        return dto;
    }

    private AvaliacaoFotoResponseDTO toFotoDTO(AvaliacaoFoto f) {
        AvaliacaoFotoResponseDTO dto = new AvaliacaoFotoResponseDTO();
        dto.setId(f.getId());
        dto.setDadosImagem(f.getDadosImagem());
        dto.setNomeArquivo(f.getNomeArquivo());
        dto.setDataUpload(f.getDataUpload());
        return dto;
    }
}
