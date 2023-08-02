package br.com.victorhugoof.camel.rabbitmq.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RabbitMQProducer {
    private final String exchange;
    private final String routingKey;

    @Builder.Default
    private final long requestTimeout = 20000;
}
