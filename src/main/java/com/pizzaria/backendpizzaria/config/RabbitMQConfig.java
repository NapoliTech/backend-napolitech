package com.pizzaria.backendpizzaria.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_PEDIDOS = "pedidos.queue";
    public static final String EXCHANGE_PEDIDOS = "pedidos.exchange";
    public static final String ROUTING_KEY_PEDIDO_CRIADO = "pedidos.v1.pedido-criado";
    public static final String FILA_PEDIDOS_DLQ = "pedidos.queue.dlq";
    public static final String EXCHANGE_PEDIDOS_DLQ = "pedidos.exchange.dlq";
    public static final String ROUTING_KEY_PEDIDO_CRIADO_DLQ = "pedidos.v1.pedido-criado.dlq";

    // Cria fila durável
    @Bean
    public Queue pedidosQueue() {
        return QueueBuilder.durable(FILA_PEDIDOS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_PEDIDOS_DLQ)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_PEDIDO_CRIADO_DLQ)
                .build();
    }

    // Cria exchange do tipo direct
    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(EXCHANGE_PEDIDOS);
    }

    // DLQ exchange
    @Bean
    public DirectExchange pedidosDeadLetterExchange() {
        return new DirectExchange(EXCHANGE_PEDIDOS_DLQ);
    }

    // DLQ queue
    @Bean
    public Queue pedidosDeadLetterQueue() {
        return QueueBuilder.durable(FILA_PEDIDOS_DLQ).build();
    }

    // Faz o binding da fila com a exchange usando a routing key
    @Bean
    public Binding bindingPedidos() {
        return BindingBuilder.bind(pedidosQueue())
                .to(pedidosExchange())
                .with(ROUTING_KEY_PEDIDO_CRIADO);
    }

    @Bean
    public Binding bindingPedidosDlq() {
        return BindingBuilder.bind(pedidosDeadLetterQueue())
                .to(pedidosDeadLetterExchange())
                .with(ROUTING_KEY_PEDIDO_CRIADO_DLQ);
    }

    // Métodos auxiliares para acesso no ProducerService
    public String getExchangeName() {
        return EXCHANGE_PEDIDOS;
    }

    public String getRoutingKeyPedidoCriado() {
        return ROUTING_KEY_PEDIDO_CRIADO;
    }
}
