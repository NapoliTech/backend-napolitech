package com.pizzaria.backendpizzaria.service.RabbitMQ;

import com.pizzaria.backendpizzaria.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);

    @RabbitListener(queues = RabbitMQConfig.FILA_PEDIDOS)
    public void consumir(@Payload String mensagem) {
        try {
            log.info("Pedido recebido: {}", mensagem);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem", e);
            throw new AmqpRejectAndDontRequeueException("Falha ao processar pedido", e);
        }
    }
}
