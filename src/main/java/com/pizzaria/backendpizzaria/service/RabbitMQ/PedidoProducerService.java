package com.pizzaria.backendpizzaria.service.RabbitMQ;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaria.backendpizzaria.config.RabbitMQConfig;
import com.pizzaria.backendpizzaria.domain.Pedido;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PedidoProducerService {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;
    private final ObjectMapper objectMapper;

    public PedidoProducerService(RabbitTemplate rabbitTemplate,
                                 ObjectMapper objectMapper,
                                 RabbitMQConfig rabbitMQConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.rabbitMQConfig = rabbitMQConfig;
    }

    public void enviarPedido(Pedido pedido) {
        try {
            String json = objectMapper.writeValueAsString(pedido);
            rabbitTemplate.convertAndSend(
                    rabbitMQConfig.getExchangeName(),
                    rabbitMQConfig.getRoutingKeyPedidoCriado(),
                    json
            );
            System.out.println("Pedido enviado -> " + json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao converter pedido em JSON", e);
        }
    }
}
