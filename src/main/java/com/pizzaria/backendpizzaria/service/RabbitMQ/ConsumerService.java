package com.pizzaria.backendpizzaria.service.RabbitMQ;

import com.pizzaria.backendpizzaria.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    @RabbitListener(queues = RabbitMQConfig.FILA_PEDIDOS)
    public void consumir(@Payload String mensagem) {
        try {
            System.out.println("Pedido recebido: " + mensagem);
        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem: " + e.getMessage());
        }
    }
}
