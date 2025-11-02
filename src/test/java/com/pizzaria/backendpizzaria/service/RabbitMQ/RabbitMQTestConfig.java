package com.pizzaria.backendpizzaria.service.RabbitMQ;

import com.rabbitmq.client.ConnectionFactory;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
class RabbitMQTestConfig {

    @Bean
    @Primary
    public RabbitTemplate testRabbitTemplate() {
        return Mockito.mock(RabbitTemplate.class);
    }

    @Bean
    @Primary
    public SimpleMessageListenerContainer simpleMessageListenerContainer() {
        return Mockito.mock(SimpleMessageListenerContainer.class);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return Mockito.mock(ConnectionFactory.class);
    }
}