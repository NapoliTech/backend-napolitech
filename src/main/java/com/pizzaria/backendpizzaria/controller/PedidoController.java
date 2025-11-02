package com.pizzaria.backendpizzaria.controller;

import com.pizzaria.backendpizzaria.domain.DTO.Login.ClienteResumoDTO;
import com.pizzaria.backendpizzaria.domain.DTO.Pedido.AtualizarStatusPedidoDTO;
import com.pizzaria.backendpizzaria.domain.DTO.Pedido.PedidoDTO;
import com.pizzaria.backendpizzaria.domain.Pedido;
import com.pizzaria.backendpizzaria.domain.Produto;
import com.pizzaria.backendpizzaria.service.Pedido.PedidoListagem;
import com.pizzaria.backendpizzaria.service.Pedido.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para gerenciamento de pedidos.")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoListagem pedidoListagem;

    public PedidoController(PedidoService pedidoService, PedidoListagem pedidoListagem) {
        this.pedidoService = pedidoService;
        this.pedidoListagem = pedidoListagem;
    }

    @Operation(summary = "Criar um novo pedido", description = "Registra um novo pedido no sistema.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> criarPedido(
            @Parameter(description = "Dados do pedido a ser criado.") @RequestBody PedidoDTO pedidoDTO) {
        try {
            Pedido pedidoCriado = pedidoService.criarPedido(pedidoDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("pedidoId", pedidoCriado.getId());
            response.put("dataPedido", pedidoCriado.getDataPedido());
            response.put("valorTotal", pedidoCriado.getPrecoTotal());
            response.put("itens", pedidoCriado.getItens());
            response.put("nomeCliente", pedidoCriado.getCliente().getNome());
            response.put("telefone", pedidoCriado.getCliente().getTelefone());
            response.put("tipoEntrega", pedidoCriado.getTipoEntrega());
            response.put("endereco", pedidoCriado.getCliente().getEndereco());
            response.put("observacao", pedidoCriado.getObservacao());
            response.put("status", pedidoCriado.getStatusPedido());
            response.put("endereco", pedidoCriado.getEndereco());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Erro ao criar pedido: " + e.getMessage()));
        }
    }

    @Operation(summary = "Atualizar status do pedido", description = "Atualiza o status de um pedido existente.")
    @PutMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatusPedido(
            @Parameter(description = "ID do pedido a ser atualizado.", example = "1") @PathVariable Long id,
            @Parameter(description = "Novo status do pedido.") @RequestBody AtualizarStatusPedidoDTO statusPedido) {
        Pedido pedidoAtualizado = pedidoService.atualizarStatusPedido(id, statusPedido.getStatus());
        return new ResponseEntity<>(pedidoAtualizado, HttpStatus.OK);
    }

    @Operation(summary = "Buscar pedido por ID", description = "Retorna os detalhes de um pedido pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> listarPedidoPorId(
            @Parameter(description = "ID do pedido a ser buscado.", example = "1") @PathVariable("id") Long id) {
        Optional<Pedido> pedidoOptional = pedidoListagem.listarPedidoPorId(id);

        if (pedidoOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Pedido não encontrado"));
        }

        Pedido pedido = pedidoOptional.get();

        ClienteResumoDTO clienteResumo = new ClienteResumoDTO(
                pedido.getCliente().getNome(),
                pedido.getCliente().getEmail(),
                pedido.getCliente().getTelefone(),
                Math.toIntExact(pedido.getCliente().getPedidos())
        );

        Map<String, Object> response = new HashMap<>();
        response.put("id", pedido.getId());
        response.put("cliente", clienteResumo);
        response.put("endereco", pedido.getEndereco());
        response.put("nomeCliente", pedido.getNomeCliente());
        response.put("statusPedido", pedido.getStatusPedido());
        response.put("precoTotal", pedido.getPrecoTotal());
        response.put("observacao", pedido.getObservacao());
        response.put("tipoEntrega", pedido.getTipoEntrega());
        response.put("itens", pedido.getItens());
        response.put("dataPedido", pedido.getDataPedido());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Listar pedidos", description = "Retorna uma lista paginada de pedidos")
    @GetMapping
    public ResponseEntity<Page<Pedido>> listarPedidos(
            @Parameter(description = "Configuração de paginação e ordenação")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Pedido> pedidos = pedidoListagem.listarPedidos(pageable);
        return ResponseEntity.ok(pedidos);
    }
}
