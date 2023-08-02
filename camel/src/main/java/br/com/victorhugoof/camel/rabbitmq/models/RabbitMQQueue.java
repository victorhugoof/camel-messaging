package br.com.victorhugoof.camel.rabbitmq.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RabbitMQQueue {
    private final String name;

    @Builder.Default
    private final boolean durable = true;

    @Builder.Default
    private final boolean exclusive = false;

    @Builder.Default
    private final boolean autoDelete = false;

    @Builder.Default
    private final int prefetchCount = 1;
}
