package com.pizzaria.backendpizzaria.service.RabbitMQ;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    private static final String FILA_PEDIDOS = "pedidos.queue";

    @RabbitListener(queues = FILA_PEDIDOS)
    public void consumir(@Payload String mensagem) {
        try {
            System.out.println("Pedido recebido: " + mensagem);

        } catch (Exception e) {
            System.out.println("Erro ao processar mensagem: " + e.getMessage());
        }
    }

}
