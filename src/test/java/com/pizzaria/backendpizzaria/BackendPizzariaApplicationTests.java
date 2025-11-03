package com.pizzaria.backendpizzaria;

import com.pizzaria.backendpizzaria.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("teste")
@Import(BackendPizzariaApplicationTests.RabbitMQTestConfig.class)
class BackendPizzariaApplicationTests {

    @TestConfiguration
    static class RabbitMQTestConfig {

        @Bean
        public RabbitTemplate classTestrabbitTemplate() {
            return Mockito.mock(RabbitTemplate.class); // Mocka envio de mensagens
        }

        @Bean
        public SimpleMessageListenerContainer simpleMessageListenerContainer() {
            return Mockito.mock(SimpleMessageListenerContainer.class); // Mocka listeners
        }
    }

    @Test
    void contextLoads() {
    }

}
