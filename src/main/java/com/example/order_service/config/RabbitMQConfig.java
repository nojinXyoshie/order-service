package com.example.order_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final String paymentRequestQueue;
    private final String paymentStatusQueue;

    public RabbitMQConfig(@Value("${messaging.payment.request.queue}") String paymentRequestQueue,
                          @Value("${messaging.payment.status.queue}") String paymentStatusQueue) {
        this.paymentRequestQueue = paymentRequestQueue;
        this.paymentStatusQueue = paymentStatusQueue;
    }

    @Bean
    public Queue paymentRequestQueue() {
        return new Queue(paymentRequestQueue, true);
    }

    @Bean
    public Queue paymentStatusQueue() {
        return new Queue(paymentStatusQueue, true);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
