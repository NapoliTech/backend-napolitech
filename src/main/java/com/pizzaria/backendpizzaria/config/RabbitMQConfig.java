package com.pizzaria.backendpizzaria.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final ConnectionFactory connectionFactory;

    public RabbitMQConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Value("${BROKER_EXCHANGE_NAME}")
    private String exchangeName;

    @Value("${BROKER_QUEUE_NAME}")
    private String queueName;

    @Bean
    public FanoutExchange exchange() {
        return new FanoutExchange(exchangeName);
    }

    @Bean
    public Queue queue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding binding(Queue queue, FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    public String getExchangeName() {
        return exchangeName;
    }

}